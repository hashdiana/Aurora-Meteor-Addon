package espada.spacex.aurora.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ColorSetting extends Modules {
    public static ColorSetting INSTANCE = new ColorSetting();
    public ColorSetting() {
        super(Aurora.SETTINGS, "ChatColor", "Set Chat Color.");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<SettingColor> Color = sgGeneral.add(new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
        .name("Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 0))
        .build()
    );
}
