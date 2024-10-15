package espada.spacex.aurora.utils;

import espada.spacex.aurora.mixins.IBlockSettings;
import espada.spacex.aurora.timers.TimerList;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BOBlockUtil {

    private static final TimerList placed = new TimerList();

    @SuppressWarnings("DataFlowIssue")
    public static boolean replaceable(BlockPos block) {
        return ((IBlockSettings) AbstractBlock.Settings.copy(mc.world.getBlockState(block).getBlock())).replaceable();
    }


    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "DataFlowIssue"})
    public static boolean solid(BlockPos block) {
        Block b = mc.world.getBlockState(block).getBlock();
        return !(b instanceof AbstractFireBlock || b instanceof FluidBlock || b instanceof AirBlock);
    }

    public static boolean solid2(BlockPos block) {
        return mc.world.getBlockState(block).isSolid();
    }

    public static boolean isAir(BlockPos block) {
        return MeteorClient.mc.world.getBlockState(block).isAir() || getBlock(block) == Blocks.FIRE;
    }

    public static Block getBlock(BlockPos block) {
        return MeteorClient.mc.world.getBlockState(block).getBlock();
    }


    public static boolean isAir(Vec3d vec3d) {
        return Util.mc.world.getBlockState(vec3toBlockPos(vec3d)).getBlock().equals(Blocks.AIR);
    }

    public static BlockPos vec3toBlockPos(Vec3d vec3d) {
        return new BlockPos((int)Math.floor(vec3d.x), (int)Math.round(vec3d.y), (int)Math.floor(vec3d.z));
    }

    public static double getPushDistance(PlayerEntity player, double x, double z) {
        double d0 = player.getX() - x;
        double d2 = player.getZ() - z;
        return Math.sqrt(d0 * d0 + d2 * d2);
    }


    public static BlockState getState(BlockPos pos) {
        return MeteorClient.mc.world.getBlockState(pos);
    }


    public static boolean fakeBBoxCheckFeet(PlayerEntity player, Vec3d offset) {
        Vec3d futurePos = player.getPos().add(offset);
        return isAir(futurePos.add(0.3, 0.0, 0.3)) && isAir(futurePos.add(-0.3, 0.0, 0.3)) && isAir(futurePos.add(0.3, 0.0, -0.3)) && isAir(futurePos.add(-0.3, 0.0, 0.0)) && isAir(futurePos.add(0.0, 0.0, 0.3)) && isAir(futurePos.add(0.3, 0.0, 0.0)) && isAir(futurePos.add(0.0, 0.0, -0.3));
    }

    public static BlockPos getFlooredPosition(Entity entity) {
        return new BlockPos((int)Math.floor(entity.getX()), (int)Math.round(entity.getY()), (int)Math.floor(entity.getZ()));
    }

    public static boolean cantBlockPlace(BlockPos blockPos) {
        if (MeteorClient.mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(0, 0, -1)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(-1, 0, 0)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() == Blocks.AIR) {
            return true;
        } else {
            return !MeteorClient.mc.world.getBlockState(blockPos).isAir() && getBlock(blockPos) != Blocks.FIRE;
        }
    }

    public static void placeBlock(BlockPos pos, Hand hand, boolean rotate, int priority) {
        if (getBlock(pos) == Blocks.AIR) {
            PlaceData data = SettingUtils.getPlaceDataOR(pos, placed::contains);
            if (data != null && data.valid()) {
                Vec3d hitPos = Vec3d.ofCenter(pos);
                Direction side = BlockUtils.getPlaceSide(pos);
                if (side != null) {
                    hitPos = hitPos.add((double)side.getOffsetX() * 0.5D, (double)side.getOffsetY() * 0.5D, (double)side.getOffsetZ() * 0.5D);
                }

                if (rotate) {
                    Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), priority);
                }

                place(data.pos(), hand, data.pos().toCenterPos(), data.dir());
            }
        }
    }

    public static void sendSequenced(SequencedPacketCreator packetCreator) {
        if (mc.interactionManager == null || mc.world == null || mc.getNetworkHandler() == null) return;

        PendingUpdateManager sequence = mc.world.getPendingUpdateManager().incrementSequence();
        Packet<?> packet = packetCreator.predict(sequence.getSequence());

        mc.getNetworkHandler().sendPacket(packet);

        sequence.close();
    }

    private static void place(BlockPos pos, Hand hand, Vec3d blockHitVec, Direction blockDirection) {
        Vec3d eyes = MeteorClient.mc.player.getEyePos();
        boolean inside = eyes.x > (double)pos.getX() && eyes.x < (double)pos.getX() && eyes.y > (double)pos.getY() && eyes.y < (double)pos.getY() && eyes.z > (double)pos.getZ() && eyes.z < (double)pos.getZ();
        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        sendSequenced((s) -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
    }

    public static int getBlockBreakingSpeed(BlockState block, BlockPos pos, int slot) {
        PlayerEntity player = mc.player;

        float f = (player.getInventory().getStack(slot)).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.get(player.getInventory().getStack(slot)).getOrDefault(Enchantments.EFFICIENCY, 0);
            if (i > 0) {
                f += (float) (i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(player)) {
            f *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2F;
        }

        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k = switch (player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= k;
        }

        if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.isOnGround()) {
            f /= 5.0F;
        }

        float t = block.getHardness(mc.world, pos);
        if (t == -1.0F) {
            return 0;
        } else {
            return (int) Math.ceil(1 / (f / t / 30));
        }
    }

}
