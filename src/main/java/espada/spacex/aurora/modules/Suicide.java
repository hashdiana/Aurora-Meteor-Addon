package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

/**
 * @author OLEPOSSU
 */

public class Suicide extends Modules {
    public Suicide() {
        super(Aurora.AURORA, "Suicide", "Kills yourself. Recommended.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> disableDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("Disable On Death")
        .description("Disables the module on death.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> enableCA = sgGeneral.add(new BoolSetting.Builder()
        .name("Enable Auto Crystal")
        .description("Enables auto crystal when enabled.")
        .defaultValue(true)
        .build()
    );

    @Override
    public void onActivate() {
        if (enableCA.get() && !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AutoCrystal.class)) {
            meteordevelopment.meteorclient.systems.modules.Modules.get().get(AutoCrystal.class).toggle();
        }
    }

    @EventHandler(priority = 6969)
    private void onDeath(OpenScreenEvent event) {
        if (event.screen instanceof DeathScreen && disableDeath.get()) {
            toggle();
            sendDisableMsg("died");
        }
    }
}
