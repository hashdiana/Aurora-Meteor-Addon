package espada.spacex.aurora;

import com.mojang.logging.LogUtils;
import espada.spacex.aurora.commands.BlackoutGit;
import espada.spacex.aurora.commands.Coords;
import espada.spacex.aurora.globalsettings.*;
import espada.spacex.aurora.hud.*;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.*;
import espada.spacex.aurora.modules.automine.AuroraMine;
import espada.spacex.aurora.modules.autoweb.AutoWeb;
import espada.spacex.aurora.modules.autoweb.FaceWebHelper;
import espada.spacex.aurora.modules.maojunqing.MaoJunQingAura;
import espada.spacex.aurora.modules.timer.TimerPlus;
import espada.spacex.aurora.modules.autocrystal.AutoCrystal;
import espada.spacex.aurora.modules_render.CustomWeather;
import espada.spacex.aurora.modules_render.FeetESP;
import espada.spacex.aurora.modules_render.Fog;
import espada.spacex.aurora.modules_render.ForceSneak;
import espada.spacex.aurora.modules_render.HoleEsp.HoleEsp;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;

/**
 * @author OLEPOSSU
 * @author KassuK
 */

public class Aurora extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    public static final Category AURORA = new Category("Aurora", Items.END_CRYSTAL.getDefaultStack());
    public static final Category SETTINGS = new Category("Settings", Items.OBSIDIAN.getDefaultStack());
    public static final Category Extendcombat = new Category("ExtendCombat", Items.OBSIDIAN.getDefaultStack());
    public static final Category ExtendMove = new Category("ExtendMove", Items.OBSIDIAN.getDefaultStack());
    public static final Category EAncillary = new Category("E-Ancillary", Items.OBSIDIAN.getDefaultStack());

    public static final HudGroup HUD_EDIT = new HudGroup("Aurora");
    public static final String NAME = "Aurora";
    public static final String VERSION = "0.1.5";
    public static final String COLOR = "Color is the visual perception of different wavelengths of light as hue, saturation, and brightness";

    @Override
    public void onInitialize() {
        LOG.info("Initializing Aurora");

        initializeModules(Modules.get());

        initializeSettings(Modules.get());

        initializeCommands();

        initializeHud(Hud.get());


        Managers.PLAYER.init();
    }


    private void initializeModules(Modules modules) {
        //--------------main-------------------//
        modules.add(new MaoJunQingAura());
        modules.add(new AntiAim());
        modules.add(new AntiCrawl());
        modules.add(new AutoCraftingTable());
        modules.add(new AutoEz());
        modules.add(new Automation());
        modules.add(new AutoMend());
        modules.add(new AuroraMine());
        modules.add(new AutoPearl());
        initializeAutoPVP(modules);
        modules.add(new AutoTrapPlus());
        modules.add(new BedAuraPlus());
        modules.add(new Blocker());
        modules.add(new BurrowPlus());
        modules.add(new CustomFOV());
        modules.add(new ElytraFlyPlus());
        modules.add(new FlightPlus());
        modules.add(new HoleFillPlus());
        modules.add(new HoleFillRewrite());
        modules.add(new HoleSnap());
        modules.add(new JesusPlus());
        modules.add(new Aura());
        modules.add(new LightsOut());
        modules.add(new OffHandPlus());
        modules.add(new PacketFly());
        modules.add(new PistonCrystal());
        modules.add(new PistonPush());
        modules.add(new PortalGodMode());
        modules.add(new RPC());
        modules.add(new ScaffoldPlus());
        modules.add(new SelfTrapPlus());
        modules.add(new SoundModifier());
        modules.add(new SpeedPlus());
        modules.add(new SprintPlus());
        modules.add(new StepPlus());
        modules.add(new StrictNoSlow());
        modules.add(new Suicide());
        modules.add(new SurroundPlus());
        modules.add(new SwingModifier());
        modules.add(new TickShift());
        modules.add(new WeakAlert());
        modules.add(new BurrowMove());
        modules.add(new PacketEat());
        modules.add(new SkinBlinker());
        modules.add(new BurrowPlus2());
        modules.add(new AntiPiston());
        modules.add(new FaceWebHelper());
        modules.add(new KeyCity());
        modules.add(new HnadSync());
        modules.add(new NewNameTags());
        modules.add(new AntiWeak());
        modules.add(new BlockSelectionPlus());
        modules.add(new TimerPlus());
        modules.add(new FastWeb());
        modules.add(new NewFakePlayer());
        modules.add(new Step());
        modules.add(new MultiTasks());
        modules.add(new Suffix());
        modules.add(new AttackIndicator());
        modules.add(new AutoLoadKit());
        modules.add(new BreakCrystal());
        modules.add(new CevBreaker());
        modules.add(new AutoAnvil());
        modules.add(new MoveUp());
        modules.add(new AutoCrystal());
        modules.add(new AutoWeb());
        modules.add(new Strafe());
        modules.add(new BRotateBypass());
        modules.add(new MCP());
        modules.add(new HoleEsp());
        //---------Render-----------//
        modules.add(new FeetESP());
        modules.add(new Fog());
        modules.add(new ForceSneak());
        modules.add(new MineESP());
        modules.add(new CustomWeather());
        modules.add(new KillEffects());
    }

    private void initializeSettings(Modules modules) {
        modules.add(new AspectRatio());
        modules.add(new FacingSettings());
        modules.add(new RangeSettings());
        modules.add(new RaytraceSettings());
        modules.add(new RotationSettings());
        modules.add(new ServerSettings());
        modules.add(new SwingSettings());
        modules.add(new ColorSetting());
        modules.add(new CrystalESP());
        modules.add(new PlaceRender());
        modules.add(new RotationPrioritySettings());
    }

    private void initializeCommands() {
        Commands.add(new BlackoutGit());
        Commands.add(new Coords());
    }

    private void initializeHud(Hud hud) {
        hud.register(ArmorHudPlus.INFO);
        hud.register(AuroraArray.INFO);
        hud.register(GearHud.INFO);
        hud.register(HudWaterMark.INFO);
        hud.register(Keys.INFO);
        hud.register(TargetHud.INFO);
        hud.register(Welcomer.INFO);
        hud.register(OnTope.INFO);
        hud.register(CatGirl.INFO);
        hud.register(TickShiftHud.INFO);
        hud.register(MineHud.INFO);
        hud.register(PacketHud.INFO);
        hud.register(TimerPlusHud.INFO);
    }

    private void initializeAutoPVP(Modules modules) {
        try {
            Class.forName("baritone.api.BaritoneAPI");
            modules.add(new AutoPvp());
        } catch (ClassNotFoundException ignored) {}
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(AURORA);
        Modules.registerCategory(SETTINGS);
        Modules.registerCategory(Extendcombat);
        Modules.registerCategory(ExtendMove);
        Modules.registerCategory(EAncillary);
    }

    @Override
    public String getPackage() {
        return "espada.spacex.aurora";
    }
}
