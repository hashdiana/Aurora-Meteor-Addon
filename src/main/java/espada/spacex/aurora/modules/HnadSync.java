package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class HnadSync extends Modules {

    private final Timer timer = new Timer();

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();;
    private final Setting<Boolean> delaySync = this.sgGeneral.add(new BoolSetting.Builder().name("DelaySync").defaultValue(false).build());
    private final Setting<Integer> delay  = this.sgGeneral.add(new IntSetting.Builder().name("Delay").defaultValue(35).min(0).sliderRange(0, 2000).build());
    public HnadSync() {
        super(Aurora.Extendcombat, "Hand Sync", "Sync.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.isUsingItem()){
            sync();
        }
        if (this.timer.passedMs((long)((Integer)this.delay.get()).intValue()) && delaySync.get()) {
            sync();
        }
    }

    private void sync(){
        sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
        timer.reset();
    }
}
