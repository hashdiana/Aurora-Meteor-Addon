package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.*;

public class Extrap {
    public static Setting<Integer> placeExtrap(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("placeExtrapolation")
            .description("")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Integer> breakExtrap(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("BreakExtrapolation")
            .description("")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Integer> rangePre(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("RangeExtrapolation")
            .description("")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Integer> Self(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("SelfExtrapolation")
            .description("")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Integer> block(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("BlockEextrapolation")
            .description("")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Integer> PlaceExtrapTick(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("PlaceExtrapTick")
            .description("")
            .defaultValue(2)
            .min(2)
            .sliderRange(2, 20)
            .build()
        );
    }
    public static Setting<Boolean> renderExt(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("renderExt")
            .description("")
            .defaultValue(false)
            .build()
        );
    }
    public static Setting<Boolean> renderSelfExt(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("renderSelfExt")
            .description("")
            .defaultValue(false)
            .build()
        );
    }
}
