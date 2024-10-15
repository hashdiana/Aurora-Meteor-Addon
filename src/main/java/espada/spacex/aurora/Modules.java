package espada.spacex.aurora;

import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.modules.SwingModifier;
import espada.spacex.aurora.utils.PriorityUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Util;
import espada.spacex.aurora.modules.autocrystal.ListenerPriorty;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;


public class Modules extends Module {
    private final String prefix = String.valueOf(Formatting.BOLD);
    public int priority;

    public Modules(Category category, String name, String description) {
        super(category, name, description);
        this.priority = PriorityUtils.get(this);
    }

    //  Messages
    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
        String msg = prefix + Formatting.BOLD + name + (isActive() ? Formatting.GREEN + " enabled" : Formatting.RED + " disabled");
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public static boolean nullCheck() {
        return Util.mc.player == null || Util.mc.world == null;
    }

    public void sendToggledMsg(String message) {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + Formatting.BOLD + name + (isActive() ? Formatting.GREEN + " enabled " : Formatting.RED + " disabled ");
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendDisableMsg(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + Formatting.BOLD + name + Formatting.RED + " disabled " + Formatting.GRAY + text;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendBOInfo(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.BOLD + name + text;
            sendMessage(Text.of(msg), Objects.hash(name + "-info"));
        }
    }
    public void debug(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.BOLD + name + Formatting.AQUA + text;
            sendMessage(Text.of(msg), 0);
        }
    }

    public void sendMessage(Text text, int id) {
        ((IChatHud) mc.inGameHud.getChatHud()).meteor$add(text, id);
    }

    public void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(packet);
    }

    public void sendSequenced(SequencedPacketCreator packetCreator) {
        if (mc.interactionManager == null || mc.world == null || mc.getNetworkHandler() == null) return;

        PendingUpdateManager sequence = mc.world.getPendingUpdateManager().incrementSequence();
        Packet<?> packet = packetCreator.predict(sequence.getSequence());

        mc.getNetworkHandler().sendPacket(packet);

        sequence.close();
    }

    public void placeBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
                eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
                eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
    }

    public boolean placeBlock(BlockPos blockPos, FindItemResult findItemResult, boolean checkEntities) {
        if (findItemResult.isOffhand()) {
            return place(blockPos, Hand.OFF_HAND, mc.player.getInventory().selectedSlot, checkEntities);
        }
        return place(blockPos, Hand.MAIN_HAND, findItemResult.slot(), checkEntities);
    }

    private boolean place(BlockPos blockPos, Hand hand, int slot, boolean checkEntities) {
        if (slot < 0 || slot > 8) return false;
        if (!BlockUtils.canPlace(blockPos, checkEntities)) return false;

        Vec3d hitPos = blockPos.toCenterPos();

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

        return true;
    }


    public void interactBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
            eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
            eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Interact, hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Interact, hand);
    }

    public void useItem(Hand hand) {
        SettingUtils.swing(SwingState.Pre, SwingType.Using, hand);
        sendSequenced(s -> new PlayerInteractItemC2SPacket(hand, s));
        SettingUtils.swing(SwingState.Post, SwingType.Using, hand);
    }

    public void clientSwing(SwingHand swingHand, Hand realHand) {
        Hand hand = switch (swingHand) {
            case MainHand -> Hand.MAIN_HAND;
            case OffHand -> Hand.OFF_HAND;
            case RealHand -> realHand;
        };

        mc.player.swingHand(hand, true);
        meteordevelopment.meteorclient.systems.modules.Modules.get().get(SwingModifier.class).startSwing(hand);
    }

    public Setting<Boolean> addPauseEat(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Pause Eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
        );
    }
}
