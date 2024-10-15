package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class AntiWeak extends Modules {

    private Packet<?> packet = null;
    private final Timer delayTimer = new Timer();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private int lastSlot = -1;
    private final Setting<Integer> delay = this.sgGeneral.add(new IntSetting.Builder().name("Delay").defaultValue(35).min(0).sliderRange(0, 2000).build());
    private final Setting<Boolean> always = this.sgGeneral.add(new BoolSetting.Builder().name("Always").description("an.").defaultValue(true).build());
    private final Setting<Boolean> sync = this.sgGeneral.add(new BoolSetting.Builder().name("Only Burrow").description("an.").defaultValue(true).build());
    private int old = -1;
    private boolean update = false;

    public AntiWeak() {
        super(Aurora.AURORA, "AntiWeak", "test");
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (!Utils.canUpdate()) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (!mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            return;
        }
        if (mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            return;
        }
        if (!this.delayTimer.passedMs(this.delay.get())) {
            return;
        }
        if (event.packet instanceof PlayerInteractEntityC2SPacket) {
            this.packet = event.packet;
            this.doAnti();
            event.setCancelled(true);
        }
        if(update){
            mc.player.networkHandler.sendPacket(this.packet);
            BOInvUtils.doSwap(old);
            this.delayTimer.reset();
        }
    }

    private void doAnti() {
        if (this.packet == null) {
            return;
        }
        int strong = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof SwordItem).slot();
        if (strong == -1) {
            return;
        }
        old = mc.player.getInventory().selectedSlot;
        BOInvUtils.doSwap(strong);
        this.error(this.packet.toString());
        if(mc.player.getInventory().selectedSlot == InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof SwordItem).slot() && !update){
            update = true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Post event) {
        this.update();
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        this.update();
    }


    private void update() {
        if (this.lastSlot == -1 || this.always.get() || !this.sync.get()) {
            return;
        }
        if (!(mc.player.getInventory().getStack(this.lastSlot).getItem() instanceof SwordItem)) {
            this.lastSlot = -1;
        }
    }

    public static enum SwapMode {
        Normal,
        Silent,
        Bypass

    }
}
