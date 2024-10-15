package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class friend {
    public static Setting<Double> minFriendPlaceRatio(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("MinFriendPlaceRatio")
            .description(" ")
            .defaultValue(2)
            .min(0)
            .sliderRange(0, 5)
            .build()
        );
    }
    public static Setting<Double> maxFriendPlace(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("MaxFriendDmg")
            .description("Max friend damage for exploding a crystal.")
            .defaultValue(12)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> maxFriendExp(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("MaxFriendDmg")
            .description("Max friend damage for exploding a crystal.")
            .defaultValue(12)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> minFriendExpRatio(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("FriendBreakRatio")
            .description("Min friend damage ratio for exploding a crystal (enemy / friend).")
            .defaultValue(2)
            .min(0)
            .sliderRange(0, 5)
            .build()
        );
    }
}
