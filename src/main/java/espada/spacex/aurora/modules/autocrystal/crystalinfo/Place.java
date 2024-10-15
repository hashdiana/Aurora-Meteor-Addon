package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;

public class Place extends Modules {

    public Place(Category category, String name, String description) {
        super(category, name, description);
    }

    public static Setting<Boolean> Place(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Place")
            .description("")
            .defaultValue(true)
            .build()
        );
    }

    public static Setting<Boolean> instantPlace(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("instantPlace")
            .description("")
            .defaultValue(true)
            .build()
        );
    }

    public static Setting<Double> speedLimit(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("speedLimit")
            .description("")
            .defaultValue(0).min(0).sliderRange(0, 20)
            .build()
        );
    }

    public static Setting<Double> placeSpeed(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("placeSpeed")
            .description("")
            .defaultValue(0).min(0).sliderRange(0, 20)
            .build()
        );
    }

    public static Setting<Double> placeDelay(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("placeDelay")
            .description("")
            .defaultValue(0).min(0).sliderRange(0, 1)
            .build()
        );
    }

    public static Setting<Double> placeDelayTicks(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("placeDelayTicks")
            .description("")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }

    public static Setting<Double> MinDmg(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Mindamage")
            .description("Minimum damage to place.")
            .defaultValue(6)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> maxPlace(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("MaxSelfPlace")
            .description("w")
            .defaultValue(9)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> minPlaceRatio(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("minPlaceRatio")
            .description("w")
            .defaultValue(1.4)
            .min(0)
            .sliderRange(0, 5)
            .build()
        );
    }
}






