package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;

public class MultiTask extends Modules {

    public MultiTask(Category category, String name, String description) {
        super(category, name, description);
    }

    public static Setting<Boolean> performance(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("performance")
            .description("Pauses when eating")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> preplacecalc(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("PrePlaceCalc")
            .description("")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> preplacepos(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("PrePlacePos")
            .description("")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> preplacedir(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("PrePlaceDir")
            .description("")
            .defaultValue(true)
            .build()
        );
    }
}
