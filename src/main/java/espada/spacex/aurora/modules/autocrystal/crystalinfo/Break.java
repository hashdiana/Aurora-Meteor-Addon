package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.*;

public class Break {
    public static Setting<Boolean> Break(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Break")
            .description("")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> onlyOwn(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("onlyOwn")
            .description("")
            .defaultValue(false)
            .build()
        );
    }
    public static Setting<Integer> existed(SettingGroup group) {
        return group.add(new IntSetting.Builder()
            .name("Existed")
            .description("How many seconds should the crystal exist before attacking.")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 1)
            .build()
        );
    }
    public static Setting<Double> existedTicks(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("existedTicks")
            .description("")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Boolean> instantAttack(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("instantAttack")
            .description("")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Double> expSpeedLimit(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Explode Speed Limit")
            .description("How many times to hit any crystal each second. 0 = no limit")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> expSpeed(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Explode Speed")
            .description("How many times to hit crystal each second.")
            .defaultValue(4)
            .range(0.01, 20)
            .sliderRange(0.01, 20)
            .build()
        );
    }
    public static Setting<Double> minExplode(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("MinBreakDmg")
            .description("Minimum enemy damage for exploding a crystal.")
            .defaultValue(2.5)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> maxExp(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("MaxSelfBreak")
            .description("Max self damage for exploding a crystal.")
            .defaultValue(9)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> minExpRatio(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("minBreakRatio")
            .description("Max self damage ratio for exploding a crystal (enemy / self).")
            .defaultValue(1.1)
            .min(0)
            .sliderRange(0, 5)
            .build()
        );
    }
}
