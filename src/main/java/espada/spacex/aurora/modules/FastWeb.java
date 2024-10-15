package espada.spacex.aurora.modules;

import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.timer.TimerPlus;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;

public class FastWeb extends Modules {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> onlySneak = sgGeneral.add(new BoolSetting.Builder()
        .name("OnlySneak")
        .defaultValue(true)
        .build()
    );
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .defaultValue(Mode.FAST)
        .build()
    );
    private final Setting<Double> fastSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("FastSpeed")
        .defaultValue(3.0)
        .range(0.0,5.0)
        .sliderRange(0.0,5.0)
        .visible(() -> mode.get() == Mode.FAST )
        .build()
    );
    public FastWeb(){
        super(Aurora.ExtendMove,"FastWeb","Test");
    }

    public static final double OFF = 1;
    private double override = 1;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (BOEntityUtils.isWebbed(mc.player)) {
            if (this.mode.get() == Mode.FAST && mc.options.sneakKey.isPressed() || !this.onlySneak.get()) {
                if(!meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class).Key.get().isPressed() && !meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class).isActive()){
                    meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(Timer.OFF);
                }
                ((IVec3d) event.movement).set(event.movement.x, event.movement.y -(this.fastSpeed.get()) , event.movement.z);
            } else if (this.mode.get() == Mode.STRICT && !mc.player.isOnGround() && mc.options.sneakKey.isPressed() || !this.onlySneak.get()) {
                meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(8);
            } else {
                if(!meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class).Key.get().isPressed() && !meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class).isActive()){
                    meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(Timer.OFF);
                }
            }
        } else {
            if(!meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class).Key.get().isPressed() && !meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class).isActive()){
                meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(Timer.OFF);
            }
        }
    }

    @EventHandler
    public void onLivingEntityMove(LivingEntityMoveEvent event) {
        if (event.entity != mc.player) return;
        if (BOEntityUtils.isWebbed(mc.player) ) {
            if (this.mode.get() == Mode.FAST && mc.options.sneakKey.isPressed() || !this.onlySneak.get()) {
                if(!meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class).Key.get().isPressed() && !meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class).isActive()){
                    meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(Timer.OFF);
                }
                ((IVec3d) event.movement).set(event.movement.x, event.movement.y -(this.fastSpeed.get()) , event.movement.z);
            } else if (this.mode.get() == Mode.STRICT && !mc.player.isOnGround() && mc.options.sneakKey.isPressed() || !this.onlySneak.get()) {
                meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(8);
            } else {
                if(!meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class).Key.get().isPressed() && !meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class).isActive()){
                    meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(Timer.OFF);
                }
            }
        } else {
            if(!meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class).Key.get().isPressed() && !meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class).isActive()){
                meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class).setOverride(Timer.OFF);
            }
        }
    }

    public enum Mode {
        FAST,
        STRICT

    }
}
