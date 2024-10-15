package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;

/**
 * @author OLEPOSSU
 */

public class TickShift extends Modules {
    public TickShift() {
        super(Aurora.Extendcombat, "Tick Shift", "Stores packets when standing still and uses them when you start moving.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<SmoothMode> smooth = sgGeneral.add(new EnumSetting.Builder<SmoothMode>()
        .name("Smoothness")
        .description(".")
        .defaultValue(SmoothMode.Exponent)
        .build()
    );
    public final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
        .name("Packets")
        .description("How many packets to store for later use.")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("How many packets to send every movement tick.")
        .defaultValue(2)
        .min(1)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Keybind> Key = sgGeneral.add(new KeybindSetting.Builder()
        .name("TickShiftKey")
        .description(".")
        .defaultValue(Keybind.none())
        .build()
    );

    public int unSent = 0;
    private boolean lastTimer = false;
    private boolean lastMoving = false;
    private final Timer timerModule = meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class);

    @Override
    public void onActivate() {
        super.onActivate();
        unSent = 0;
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        if (lastTimer) {
            lastTimer = false;
            timerModule.setOverride(Timer.OFF);
        }
    }

    @Override
    public String getInfoString() {
        return String.format("%.0f", ((double) unSent / packets.get()) * 100) + "%";
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if (unSent > 0 && lastMoving && Key.get().isPressed()) {
            lastMoving = false;
            lastTimer = true;
            timerModule.setOverride(getTimer());
        } else if (lastTimer) {
            lastTimer = false;
            timerModule.setOverride(Timer.OFF);
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (e.movement.length() > 0 && !(e.movement.length() > 0.0784 && e.movement.length() < 0.0785)) {
            if(!Key.get().isPressed()) return;
            unSent = Math.max(0, unSent - 1);
            lastMoving = true;
        }
    }

    private double getTimer() {
        if (smooth.get() == SmoothMode.Disabled) {
            return timer.get();
        }
        double progress = 1 - (unSent / (float) packets.get());

        if (smooth.get() == SmoothMode.Exponent) {
            progress *= progress * progress * progress * progress;
        }

        return 1 + (timer.get() - 1) * (1 - progress);
    }

    public enum SmoothMode {
        Disabled,
        Normal,
        Exponent
    }
}
