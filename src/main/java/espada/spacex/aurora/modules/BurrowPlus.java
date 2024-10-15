package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BurrowPlus extends Modules {
    public BurrowPlus() {
        super(Aurora.AURORA, "BurrowPlus", "Let you clip into block.");

        MeteorClient.EVENT_BUS.subscribe(new Renderer());
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRubberband = settings.createGroup("Rubberband");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("Only On Ground")
        .description("Only burrow on ground.")
        .defaultValue(false)
        .build()
    );
    private final Setting<List<Block>> block = sgGeneral.add(new BlockListSetting.Builder()
        .name("Block To Use")
        .description("Which blocks used for burrow.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.ENDER_CHEST)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("The mode to switch obsidian.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<LagBackMode> lagBackMode = sgGeneral.add(new EnumSetting.Builder<LagBackMode>()
        .name("LagBack Mode")
        .description("")
        .defaultValue(LagBackMode.Troll)
        .build()
    );
    private final Setting<Integer> tryCount = sgGeneral.add(new IntSetting.Builder()
        .name("Try Count")
        .description("How many time to try burrow.")
        .defaultValue(0)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Rubberband--------------------//
    private final Setting<Double> rubberbandOffset = sgRubberband.add(new DoubleSetting.Builder()
        .name("Rubberband Offset")
        .description("Y offset of rubberband packet.")
        .defaultValue(9)
        .sliderRange(-10, 10)
        .visible(() -> lagBackMode.get().equals(LagBackMode.OBS))
        .build()
    );
    private final Setting<Integer> rubberbandPackets = sgRubberband.add(new IntSetting.Builder()
        .name("Rubberband Packets")
        .description("How many offset packets to send.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> lagBackMode.get().equals(LagBackMode.OBS))
        .build()
    );

    //--------------------Attack--------------------//
    private final Setting<Boolean> attack = sgAttack.add(new BoolSetting.Builder()
        .name("Attack")
        .description("Attacks crystals blocking surround.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> attackSpeed = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Speed")
        .description("How many times to attack every second.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minHealth = sgAttack.add(new DoubleSetting.Builder()
        .name("Min Health")
        .description(".")
        .defaultValue(6)
        .visible(attack::get)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> attackSwing = sgRender.add(new BoolSetting.Builder()
        .name("Attack Swing")
        .description("Renders swing animation when placing a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> attackHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Attack Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(attackSwing::get)
        .build()
    );
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of the boxes should be rendered.")
        .defaultValue(ShapeMode.Sides)
        .visible(render::get)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(Aurora.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Lines) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(Aurora.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 20))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Sides) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );

    private int count;
    private long lastAttack = 0;

    private final List<BlockPos> placePositions = new ArrayList<>();
    private final List<Render> renderBlocks = new ArrayList<>();

    public enum SwitchMode {
        Silent,
        InvSwitch,
        PickSilent
    }

    @Override
    public void onActivate() {
        placePositions.clear();
        count = 0;
    }

    @Override
    public void onDeactivate() {
        placePositions.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (onlyOnGround.get() && !mc.player.isOnGround()) return;

        getPlacePos();

        BlockPos burBlock = null;
        for (BlockPos placePosition : placePositions) burBlock = placePosition;
        List<Vec3d> fakeJumpOffsets = getFakeJumpOffset(burBlock, burBlock.getY() >= mc.player.getY() + 0.4);

        if (fakeJumpOffsets.size() != 4) {
            toggle();
            return;
        }

        if (!BlockUtils.canPlace(burBlock, false)) {
            toggle();
            return;
        }

        updateFakeJump(fakeJumpOffsets);

        updatePlace(burBlock);

        updateLagBack();

        ++count;
        if (count >= tryCount.get()) {
            toggle();
        }
    }

    private List<Vec3d> getFakeJumpOffset(BlockPos burBlock, boolean isHeadBurrow) {
        List<Vec3d> offsets = new LinkedList<>();
        if (isHeadBurrow) {
            if (fakeBoxCheckFeet(mc.player, new Vec3d(0.0, 2.5, 0.0))) {
                Vec3d offVec = getTwoBlockFjPos(burBlock);
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.42132, mc.player.getY() + 0.41999998688698, mc.player.getZ() + offVec.z * 0.42132));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.95, mc.player.getY() + 0.7500019, mc.player.getZ() + offVec.z * 0.95));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.03, mc.player.getY() + 0.9999962, mc.player.getZ() + offVec.z * 1.03));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.0933, mc.player.getY() + 1.17000380178814, mc.player.getZ() + offVec.z * 1.0933));
            } else {
                Vec3d offVec = getTwoBlockFjPos(burBlock);
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.42132, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 0.42132));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.95, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 0.95));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.03, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 1.03));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.0933, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 1.0933));
            }
        } else if (fakeBoxCheckFeet(mc.player, new Vec3d(0.0, 2.5, 0.0))) {
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 0.41999998688698, mc.player.getZ()));
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 0.7500019, mc.player.getZ()));
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 0.9999962, mc.player.getZ()));
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 1.17000380178814, mc.player.getZ()));
        } else {
            Vec3d offVec = getTwoBlockFjPos(burBlock);
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.42132, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 0.42132));
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.95, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 0.95));
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.03, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 1.03));
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.0933, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 1.0933));
        }
        return offsets;
    }

    private boolean isAir(Vec3d vec3d) {
        return mc.world.getBlockState(vec3dToBlockPos(vec3d, true)).getBlock().equals(Blocks.AIR);
    }

    private BlockPos vec3dToBlockPos(Vec3d vec3d, boolean Yfloor) {
        if (Yfloor) {
            return BlockPos.ofFloored(Math.floor(vec3d.x), Math.floor(vec3d.y), Math.floor(vec3d.z));
        }
        return BlockPos.ofFloored(Math.floor(vec3d.x), Math.round(vec3d.y), Math.floor(vec3d.z));
    }

    private boolean fakeBoxCheckFeet(PlayerEntity player, Vec3d offset) {
        Vec3d futurePos = player.getPos().add(offset);
        return isAir(futurePos.add(0.3, 0.0, 0.3)) && isAir(futurePos.add(-0.3, 0.0, 0.3)) && isAir(futurePos.add(0.3, 0.0, -0.3)) && isAir(futurePos.add(-0.3, 0.0, 0.3));
    }

    private void updateFakeJump(List<Vec3d> offsets) {
        if (offsets == null) return;

        offsets.forEach(vec -> {
            if (vec != null && !vec.equals(Vec3d.ZERO)) {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, false));
            }
        });
    }

    public Vec3d getTwoBlockFjPos(BlockPos burBlockPos) {
        Vec3d v = new Vec3d(burBlockPos.getX(), burBlockPos.getY(), burBlockPos.getZ()).add(0.5, 0.5, 0.5);
        BlockPos pPos = mc.player.getBlockPos();
        Vec3d s = mc.player.getPos().subtract(v);
        Vec3d off = new Vec3d(0.0, 0.0, 0.0);
        if (Math.abs(s.x) >= Math.abs(s.z) && Math.abs(s.x) > 0.2) {
            if (s.x > 0.0) {
                off = new Vec3d(0.8 - s.x, 0.0, 0.0);
            } else {
                off = new Vec3d(-0.8 - s.x, 0.0, 0.0);
            }
        } else if (Math.abs(s.z) >= Math.abs(s.x) && Math.abs(s.z) > 0.2) {
            if (s.z > 0.0) {
                off = new Vec3d(0.0, 0.0, 0.8 - s.z);
            } else {
                off = new Vec3d(0.0, 0.0, -0.8 - s.z);
            }
        } else if (burBlockPos.equals(pPos)) {
            List<Direction> facList = new ArrayList<>();
            for (Direction f3 : Direction.values()) {
                if (f3 != Direction.UP) {
                    if (f3 != Direction.DOWN) {
                        if (mc.world.isAir(pPos.offset(f3)) && mc.world.isAir(pPos.offset(f3).offset(Direction.UP))) {
                            facList.add(f3);
                        }
                    }
                }
            }
            Vec3d vec3d = mc.player.getPos();
            Vec3d[] offVec1 = new Vec3d[1];
            Vec3d[] offVec2 = new Vec3d[1];
            facList.sort((f1, f2) -> {
                offVec1[0] = vec3d.add(new Vec3d(f1.getOffsetX(), f1.getOffsetY(), f1.getOffsetZ()).multiply(0.5));
                offVec2[0] = vec3d.add(new Vec3d(f2.getOffsetX(), f2.getOffsetY(), f2.getOffsetZ()).multiply(0.5));
                return (int) (Math.sqrt(mc.player.squaredDistanceTo(offVec1[0].x, mc.player.getY(), offVec1[0].z)) - Math.sqrt(mc.player.squaredDistanceTo(offVec2[0].x, mc.player.getY(), offVec2[0].z)));
            });
            if (facList.size() > 0) {
                off = new Vec3d(facList.get(0).getOffsetX(), facList.get(0).getOffsetY(), facList.get(0).getOffsetZ());
            }
        }
        return off;
    }

    private void updateLagBack() {
        switch (lagBackMode.get()) {
            case OBS -> {
                for (int i = 0; i < rubberbandPackets.get(); i++) {
                    sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + rubberbandOffset.get(), mc.player.getZ(), false));
                }
            }
            case Troll -> {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 3.3400880035762786, mc.player.getZ(), false));
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ(), false));
            }
            case Seija -> {
                if (mc.player.getY() >= 3.0) {
                    for (int i = -10; i < 10; ++i) {
                        if (i == -1) {
                            i = 4;
                        }
                        if (mc.world.getBlockState(mc.player.getBlockPos().add(0, i, 0)).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(mc.player.getBlockPos().add(0, i + 1, 0)).getBlock().equals(Blocks.AIR)) {
                            final BlockPos pos = mc.player.getBlockPos().add(0, i, 0);
                            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.getX() + 0.3, pos.getY(), pos.getZ() + 0.3, false));
                            return;
                        }
                    }
                }
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 5.0, mc.player.getZ(), false));
            }
            case Old ->
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 7.0, mc.player.getZ(), false));
        }
    }

    private void getPlacePos() {
        placePositions.clear();

        placePositions.add(BlockPos.ofFloored(mc.player.getPos()));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX() - 0.2, mc.player.getY(), mc.player.getZ())));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() - 0.2)));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX() - 0.2, mc.player.getY(), mc.player.getZ())));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() - 0.2)));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX() - 0.2, mc.player.getY(), mc.player.getZ())));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() + 0.2)));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX() + 0.2, mc.player.getY(), mc.player.getZ())));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() + 0.2)));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX() + 0.2, mc.player.getY(), mc.player.getZ() + 0.2)));
        placePositions.add(BlockPos.ofFloored(new Vec3d(mc.player.getX() - 0.2, mc.player.getY(), mc.player.getZ() - 0.2)));
    }

    private void updatePlace(BlockPos blockPos) {
        FindItemResult item = !switchMode.get().equals(SwitchMode.Silent) ?
            InvUtils.find(itemStack -> block.get().contains(Block.getBlockFromItem(itemStack.getItem())))
            : InvUtils.findInHotbar(itemStack -> block.get().contains(Block.getBlockFromItem(itemStack.getItem())));

        if (!switchMode.get().equals(SwitchMode.Silent) ? !item.found() : !item.isHotbar()) {
            toggle();
            sendDisableMsg("correct blocks not found");
            return;
        }

        updateAttack(blockPos);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.start(blockPos, priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

        switch (switchMode.get()) {
            case Silent -> InvUtils.swap(item.slot(), true);
            case InvSwitch -> BOInvUtils.invSwitch(item.slot());
            case PickSilent -> BOInvUtils.pickSwitch(item.slot());
        }

        placePositions.stream().filter(placePos -> !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(placePos)), entity -> entity instanceof EndCrystalEntity && System.currentTimeMillis() - lastAttack > 100)).forEach(placePos -> placeBlock(placePos, item));

        switch (switchMode.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> BOInvUtils.swapBack();
            case PickSilent -> BOInvUtils.pickSwapBack();
        }

        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.end(Objects.hash(name + "placing"));
    }

    private void placeBlock(BlockPos blockPos, FindItemResult item) {
        place(blockPos, item);
        renderBlocks.add(new Render(blockPos, System.currentTimeMillis()));
    }

    private void place(BlockPos blockPos, FindItemResult findItemResult) {
        if (findItemResult.isOffhand()) {
            place(Hand.OFF_HAND, blockPos, mc.player.getInventory().selectedSlot);
        }
        place(Hand.MAIN_HAND, blockPos, findItemResult.slot());
    }

    private void place(Hand hand, BlockPos blockPos, int slot) {
        if (slot < 0 || slot > 8) return;

        Vec3d hitPos = Vec3d.ofCenter(blockPos);

        BlockPos neighbour;
        Direction side = BlockUtils.getPlaceSide(blockPos);

        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        placeBlock(hand, hitPos, side.getOpposite(), neighbour);
        if (placeSwing.get()) clientSwing(placeHand.get(), hand);
    }

    private Entity getBlocking(BlockPos blockPos) {
        Entity crystal = null;
        double lowest = 1000;

        Loop:
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (mc.player.distanceTo(entity) > 5) continue;
            if (!SettingUtils.inAttackRange(entity.getBoundingBox())) continue;

            if (!Box.from(new BlockBox(blockPos)).intersects(entity.getBoundingBox()))
                continue Loop;

            double dmg = BODamageUtils.crystal(mc.player, mc.player.getBoundingBox(), entity.getPos(), null, false);
            if (dmg < lowest) {
                crystal = entity;
                lowest = dmg;
            }
        }
        return crystal;
    }

    private void updateAttack(BlockPos blockPos) {
        if (!attack.get()) return;
        if (mc.player.getHealth() < minHealth.get()) return;
        if (System.currentTimeMillis() - lastAttack < 1000 / attackSpeed.get()) return;

        Entity blocking = getBlocking(blockPos);

        if (blocking == null) return;
        if (SettingUtils.shouldRotate(RotationType.Attacking) && !Managers.ROTATION.start(blocking.getBoundingBox(), priority - 0.1, RotationType.Attacking, Objects.hash(name + "attacking")))
            return;

        SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
        sendPacket(PlayerInteractEntityC2SPacket.attack(blocking, mc.player.isSneaking()));
        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.Attacking)) Managers.ROTATION.end(Objects.hash(name + "attacking"));
        if (attackSwing.get()) clientSwing(attackHand.get(), Hand.MAIN_HAND);

        lastAttack = System.currentTimeMillis();
    }

    public enum LagBackMode {
        OBS,
        Seija,
        Troll,
        Old
    }

    public record Render(BlockPos blockPos, long time) {
    }

    private class Renderer {
        @EventHandler
        private void onRender(Render3DEvent event) {
            if (!render.get()) return;

            renderBlocks.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderBlocks.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

                event.renderer.box(r.blockPos, RenderUtils.injectAlpha(sideColor.get(), (int) Math.round(sideColor.get().a * progress)), RenderUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
            });
        }
    }
}
