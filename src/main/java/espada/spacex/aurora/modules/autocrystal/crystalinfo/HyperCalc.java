package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class HyperCalc {
    public static Setting<Double> CoolDown(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("coolDown")
            .description("cooldown Autocrystal switch")
            .defaultValue(0.050)
            .min(0)
            .sliderRange(0, 0.050)
            .build()
        );
    }
    public static Setting<Double> slowDamage(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Slow Damage")
            .description("dont go higher than minplacedmg")
            .defaultValue(3)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> slowSpeed(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Slow Speed")
            .description("How many times should the module place per second when damage is under slow damage.")
            .defaultValue(1)
            .min(0)
            .sliderRange(0, 20)
            .build()
        );
    }
    public static Setting<Double> Desyncforce(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("desyncforce")
            .description("faceplace, if Df set 1 = enemy h 3")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 12)
            .build()
        );
    }
    public static Setting<Double> selfCheck(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("selfCheck")
            .description("no kill self, if Sc set 1 = self h 1/2")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 5)
            .build()
        );
    }
}
