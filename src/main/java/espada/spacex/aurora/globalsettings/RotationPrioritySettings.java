package espada.spacex.aurora.globalsettings;

import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;

public class RotationPrioritySettings extends Modules {
    public RotationPrioritySettings() {
        super(Aurora.SETTINGS, "Priority", "The highest value is prioritized if you want cyrstal > web ,so crystal= 9,web = 10 (high+1 or more)");
    }

    private final SettingGroup sgMain = settings.createGroup("Main");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgPlayer = settings.createGroup("Player");

    public final Setting<Integer> autoAnchor = sgMain.add(new IntSetting.Builder()
        .name("MaoAura")
        .description(".9")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> autoCrystal = sgMain.add(new IntSetting.Builder()
        .name("AutoCrystal")
        .description(",9")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> autoWeb = sgMain.add(new IntSetting.Builder()
        .name("AutoWeb")
        .description(".8")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> autoMine = sgMain.add(new IntSetting.Builder()
        .name("AutoMine")
        .description(".0-6")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> autoHoleFillPlus = sgMain.add(new IntSetting.Builder()
        .name("AutoHoleFill")
        .description("10")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );

    //-------------------------------misc
    public final Setting<Integer> autoPearlClip = sgMisc.add(new IntSetting.Builder()
        .name("AutoPearlClip")
        .description("9-15")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> autoTrap = sgMisc.add(new IntSetting.Builder()
        .name("AutoTrapPlus")
        .description("5-12")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> killAura = sgMisc.add(new IntSetting.Builder()
        .name("KillAura")
        .description("1")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> pistonCrystal = sgMisc.add(new IntSetting.Builder()
        .name("PistonCrystal")
        .description("10")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> scaffold = sgMisc.add(new IntSetting.Builder()
        .name("ScaffoldPlus")
        .description("12")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> selfTrap = sgMisc.add(new IntSetting.Builder()
        .name("SelfTrapPlus")
        .description("9")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> surroundPlus = sgMisc.add(new IntSetting.Builder()
        .name("SurroundPlus")
        .description("0-100")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );




    //  ----------------------------------------Player
    public final Setting<Integer> antiAim = sgPlayer.add(new IntSetting.Builder()
        .name("AntiAim")
        .description("12")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
    public final Setting<Integer> antiAFK = sgPlayer.add(new IntSetting.Builder()
        .name("AntiAFK")
        .description("15")
        .defaultValue(0)
        .range(0, 1000)
        .sliderMax(1000)
        .build()
    );
}
