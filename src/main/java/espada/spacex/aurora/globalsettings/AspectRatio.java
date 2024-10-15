package espada.spacex.aurora.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class AspectRatio extends Modules {

    public AspectRatio() {
            super(Aurora.SETTINGS, "AspectRatio", "");
    }

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Double> ratio = sgGeneral.add(new DoubleSetting.Builder()
        .name("Ratio")
        .defaultValue(1.78f)
        .sliderRange(0.1f,5f)
        .range(0.1f,5f)
        .build()
    );
}
