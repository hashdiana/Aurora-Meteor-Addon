package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;

public class Misc extends Modules {
    public Misc(Category category, String name, String description) {
        super(category, name, description);
    }

    public static Setting<Boolean> Pause(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Pause Eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
        );
    }
    public static Setting<Boolean> smartRot(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Smart Rotations")
            .description("")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> ignoreTerrain(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("ignoreTerrain")
            .description("")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> OnAnchorPlacePause(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("OnAnchorPlacePause")
            .description("")
            .defaultValue(false)
            .build()
        );
    }
    public static Setting<Integer> maxtarget (SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("MaxTarget")
            .description("")
            .defaultValue(3).min(0).sliderRange(0, 6)
            .build()
        );
    }
}
