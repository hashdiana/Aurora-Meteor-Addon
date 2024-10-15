package espada.spacex.aurora.modules.autocrystal.crystalinfo;

import espada.spacex.aurora.modules.autocrystal.AutoCrystalType;
import meteordevelopment.meteorclient.settings.*;

public class AutoMine {
    public static Setting<Double> autoMineDamage(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Auto Mine Damage")
            .description("Prioritizes placing on automine target block.")
            .defaultValue(1.1)
            .min(1)
            .sliderRange(1, 5)
            .build()
        );
    }
    public static Setting<Boolean> amPlace(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Auto Mine Place")
            .description("Ignores automine block before if actually breaks.")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Double> amProgress(SettingGroup group) {
        return group.add(new DoubleSetting.Builder()
            .name("Auto Mine Progress")
            .description("Ignores the block after it has reached this progress.")
            .defaultValue(0.95)
            .range(0, 1)
            .sliderRange(0, 1)
            .build()
        );
    }
    public static Setting<Boolean> amSpam(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Auto Mine Spam")
            .description("Spams crystals before the block breaks.")
            .defaultValue(false)
            .build()
        );
    }
    public static Setting <AutoCrystalType.AutoMineBrokenMode> amBroken(SettingGroup group) {
        return group.add(new EnumSetting.Builder<AutoCrystalType.AutoMineBrokenMode>()
            .name("Auto Mine Broken")
            .description("Doesn't place on automine block.")
            .defaultValue(AutoCrystalType.AutoMineBrokenMode.Near)
            .build()
        );
    }
    public static Setting<Boolean> paAttack(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Piston Crystal Attack")
            .description("Doesn't attack the crystal placed by piston crystal.")
            .defaultValue(true)
            .build()
        );
    }
    public static Setting<Boolean> paPlace(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Piston Crystal Placing")
            .description("Doesn't place crystals when piston crystal is enabled.")
            .defaultValue(true)
            .build()
        );
    }

}
