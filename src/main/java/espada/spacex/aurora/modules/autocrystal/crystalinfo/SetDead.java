package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class SetDead {
    public static Setting<Boolean> FastDead(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("FastDead")
            .description("SetDead")
            .defaultValue(false)
            .build()
        );
    }
    public static Setting<Boolean> PauseDead(SettingGroup group){
        return  group.add(new BoolSetting.Builder()
            .name("PauseDead")
            .defaultValue(false)
            .description("fastdead")
            .build()
        );
    }
}
