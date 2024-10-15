package espada.spacex.aurora.modules.automine;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.globalsettings.SwingSettings;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.RSCombatInfo;
import espada.spacex.aurora.utils.SettingUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.joml.Vector3d;

import java.util.*;

/**
 * @author OLEPOSSU
 */

public class AuroraMine extends Modules {
    public AuroraMine() {
        super(Aurora.Extendcombat, "AuroraMine", "Automatically mines blocks to destroy your enemies.");
    }
    private static AuroraMine INSTANCE = new AuroraMine();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgExplode = settings.createGroup("Explode");
    private final SettingGroup sgCev = settings.createGroup("Cev");
    private final SettingGroup sgAntiSurround = settings.createGroup("Anti Surround");
    private final SettingGroup sgAntiBurrow = settings.createGroup("Anti Burrow");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgText = settings.createGroup("Text");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pause while eating.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseanchor = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Anchor")
        .description("Pause while Achor.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseweb = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Web")
        .description("Pause while web.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseSword = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Sword")
        .description("Doesn't mine while holding sword.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwitchMode> pickAxeSwitchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Pickaxe Switch Mode")
        .description("Method of switching. InvSwitch is used in most clients.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> crystalSwitchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Crystal Switch Mode")
        .description("Method of switching. InvSwitch is used in most clients.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Boolean> autoMine = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Mine")
        .description("Sets target block to the block you clicked.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ListMode> listMode = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("List Mode")
        .description("Selection mode.")
        .defaultValue(ListMode.Blacklist)
        .visible(autoMine::get)
        .build()
    );

    private final Setting<List<Block>> blacklist = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blacklist")
        .description("The blocks you don't want to mine.")
        .defaultValue(
            Blocks.RESPAWN_ANCHOR,
            Blocks.BLUE_BED,
            Blocks.COBWEB
        )
        .visible(() -> autoMine.get() && listMode.get() == ListMode.Blacklist)
        .build()
    );

    private final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder()
        .name("Whitelist")
        .description("The blocks you want to mine.")
        .visible(() -> autoMine.get() && listMode.get() == ListMode.Whitelist)
        .build()
    );
    private final Setting<Boolean> manualMine = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Mine")
        .description("Sets target block to the block you clicked.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> manualInsta = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Instant")
        .description("Uses civ mine when mining manually.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> doubleBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("Double Break")
        .description("Double break.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> silentDouble = sgGeneral.add(new BoolSetting.Builder()
        .name("Silent Double")
        .description("Silent break.")
        .defaultValue(false)
        .visible(doubleBreak::get)
        .build()
    );
    private final Setting<silenttype> doublesilenttype = sgGeneral.add(new EnumSetting.Builder<silenttype>()
        .name("SilentDoubleMode")
        .description("Silent break.")
        .defaultValue(silenttype.UpdateSelectedSlotC2SPacket)
        .visible(doubleBreak::get)
        .build()
    );
    private final Setting<Boolean> manualRemine = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Remine")
        .description("Mines the manually mined block again.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> fastRemine = sgGeneral.add(new BoolSetting.Builder()
        .name("Fast Remine")
        .description("Calculates mining progress from last block broken.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> manualRangeReset = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Range Reset")
        .description("Resets manual mining if out of range.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> resetOnSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On Switch")
        .description("Resets mining when switched held item.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> debug = this.sgGeneral.add((new BoolSetting.Builder()).name("de Bug").description("an.").defaultValue(false).build());

    //--------------------Speed--------------------//
    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Vanilla speed multiplier.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Double> instaDelay = sgSpeed.add(new DoubleSetting.Builder()
        .name("Instant Delay")
        .description("Delay between civ mines.")
        .defaultValue(0.5)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> onGroundCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("On Ground Check")
        .description("Mines 5x slower when not on ground.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> effectCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Effect Check")
        .description("Modifies mining speed depending on haste and mining fatigue.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> waterCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Water Check")
        .description("Mines 5x slower while submerged in water.")
        .defaultValue(true)
        .build()
    );

    //--------------------Explode--------------------//
    private final Setting<Double> explodeSpeed = sgExplode.add(new DoubleSetting.Builder()
        .name("Explode Speed")
        .description("How many times to attack a crystal every second.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Double> explodeTime = sgExplode.add(new DoubleSetting.Builder()
        .name("Explode Time")
        .description("Tries to attack a crystal for this many seconds.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------Cev--------------------//
    private final Setting<Priority> cevPriority = sgCev.add(new EnumSetting.Builder<Priority>()
        .name("Cev Priority")
        .description("Priority of cev.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> instaCev = sgCev.add(new BoolSetting.Builder()
        .name("Instant Cev")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Priority> trapCevPriority = sgCev.add(new EnumSetting.Builder<Priority>()
        .name("Trap Cev Priority")
        .description("Priority of trap cev.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> instaTrapCev = sgCev.add(new BoolSetting.Builder()
        .name("Instant Trap Cev")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Priority> surroundCevPriority = sgCev.add(new EnumSetting.Builder<Priority>()
        .name("Surround Cev Priority")
        .description("Priority of trap cev.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> instaSurroundCev = sgCev.add(new BoolSetting.Builder()
        .name("Instant Surround Cev")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );

    //--------------------Anti-Surround--------------------//
    private final Setting<Priority> surroundMinerPriority = sgAntiSurround.add(new EnumSetting.Builder<Priority>()
        .name("Surround Miner Priority")
        .description("Priority of surround miner.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> instaSurroundMiner = sgAntiSurround.add(new BoolSetting.Builder()
        .name("Instant Surround Miner")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Priority> autoCityPriority = sgAntiSurround.add(new EnumSetting.Builder<Priority>()
        .name("Auto City Priority")
        .description("Priority of anti surround. Places crystal next to enemy's surround block.")
        .defaultValue(Priority.Normal)
        .build()
    );
    private final Setting<Boolean> instaAutoCity = sgAntiSurround.add(new BoolSetting.Builder()
        .name("Instant Auto City")
        .description("Only sends 1 mine start packet for each block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> explodeCrystal = sgAntiSurround.add(new BoolSetting.Builder()
        .name("Explode Crystal")
        .description("Attacks the crystal we placed.")
        .defaultValue(false)
        .build()
    );

    //--------------------Anti-Burrow--------------------//
    private final Setting<Priority> antiBurrowPriority = sgAntiBurrow.add(new EnumSetting.Builder<Priority>()
        .name("Anti Burrow Priority")
        .description("Priority of anti burrow.")
        .defaultValue(Priority.Normal)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> mineStartSwing = sgRender.add(new BoolSetting.Builder()
        .name("Mine Start Swing")
        .description("Renders swing animation when starting mining.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> mineEndSwing = sgRender.add(new BoolSetting.Builder()
        .name("Mine End Swing")
        .description("Renders swing animation when ending mining.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> mineHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Mine Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(() -> mineStartSwing.get() || mineEndSwing.get())
        .build()
    );
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description("Renders swing animation when placing a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Place Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> attackSwing = sgRender.add(new BoolSetting.Builder()
        .name("Attack Swing")
        .description("Renders swing animation when attacking a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> attackHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Attack Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(attackSwing::get)
        .build()
    );
    private final Setting<Double> animationExp = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Exponent")
        .description("3 - 4 look cool.")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description(".")
        .defaultValue(RenderMode.Normal)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineStartColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Start Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 0))
        .build()
    );
    private final Setting<SettingColor> lineEndColor = sgRender.add(new ColorSetting.Builder()
        .name("Line End Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> startColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Start Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 0))
        .build()
    );
    private final Setting<SettingColor> endColor = sgRender.add(new ColorSetting.Builder()
        .name("Side End Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<ShapeMode> shapeModeDouble = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Double Shape Mode")
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineStartColorDouble = sgRender.add(new ColorSetting.Builder()
        .name("Double Line Start Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 0))
        .build()
    );
    private final Setting<SettingColor> lineEndColorDouble = sgRender.add(new ColorSetting.Builder()
        .name("Double Line End Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> startColorDouble = sgRender.add(new ColorSetting.Builder()
        .name("Double Side Start Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 0))
        .build()
    );
    private final Setting<SettingColor> endColorDouble = sgRender.add(new ColorSetting.Builder()
        .name("Double Side End Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );

    //--------------------Text--------------------//
    private final Setting<Boolean> text = sgText.add(new BoolSetting.Builder()
        .name("Text")
        .description("Renders mine progress text in the block overlay.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> shadow = sgText.add(new BoolSetting.Builder()
        .name("Shadow")
        .description("Do text shadow render.")
        .defaultValue(true)
        .visible(text::get)
        .build()
    );
    private final Setting<Double> textScale = sgText.add(new DoubleSetting.Builder()
        .name("Text Scale")
        .description("How big the progress text should be.")
        .defaultValue(1)
        .min(0)
        .sliderMax(4)
        .visible(text::get)
        .build()
    );
    private final Setting<SettingColor> startTextColor = sgText.add(new ColorSetting.Builder()
        .name("Start Text Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(text::get)
        .build()
    );
    private final Setting<SettingColor> endTextColor = sgText.add(new ColorSetting.Builder()
        .name("End Text Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(text::get)
        .build()
    );
    private final Setting<SettingColor> waitColor = sgText.add(new ColorSetting.Builder()
        .name("Waiting Text Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> text.get() && manualInsta.get())
        .build()
    );
    private final Setting<SettingColor> startTextColorDouble = sgText.add(new ColorSetting.Builder()
        .name("Double Start Text Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> text.get() && doubleBreak.get())
        .build()
    );
    private final Setting<SettingColor> endTextColorDouble = sgText.add(new ColorSetting.Builder()
        .name("Double End Text Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> text.get() && doubleBreak.get())
        .build()
    );
    private final Setting<SettingColor> waitColorDouble = sgText.add(new ColorSetting.Builder()
        .name("Double Waiting Text Color")
        .description("COLOR")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> text.get() && doubleBreak.get() && manualInsta.get())
        .build()
    );

    private double minedFor = 0;
    public Target target = null;
    private boolean started = false;
    private BlockPos civPos = null;

    private List<AbstractClientPlayerEntity> enemies = new ArrayList<>();

    private long lastTime = 0;
    private long lastPlace = 0;
    private long lastExplode = 0;
    private long lastCiv = 0;

    private boolean canUpdate;

    private double render = 1;

    private double delta = 0;

    private final Map<BlockPos, Long> explodeAt = new HashMap<>();

    private boolean reset = false;
    private boolean mined = false;

    private int OldSlot = -1;
    private BlockState lastState = null;
    private BlockPos lastPos = null;
    public BlockPos breakPos = null;


    public static AuroraMine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuroraMine();
        }
        return INSTANCE;
    }


    @Override
    public void onActivate() {
        target = null;
        breakPos = null;
        minedFor = 0;
        started = false;
        lastTime = System.currentTimeMillis();
        civPos = null;
        reset = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket && resetOnSwitch.get()) {
            reset = true;
        }
    }

    @Override
    public String getInfoString() {
        return "Main " + ((getMineProgress() * 100 <0) ? 0 :  Math.floor(getMineProgress() * 100)) + " Double " +  (getMineProgressDouble() * 100 < 0 ? 0 : Math.floor(getMineProgressDouble() * 100));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (target != null && target.manual && manualRangeReset.get() && !SettingUtils.inMineRange(target.pos)) {
            minedFor = 0;
            breakPos = null;
            started = false;
            lastTime = System.currentTimeMillis();
            civPos = null;
            reset = false;
            return;
        }

        if (target != null) {
            if (lastState != null && target.pos.equals(lastPos) && target.manual && manualRemine.get() && !fastRemine.get() && !lastState.isSolid() && BOBlockUtil.solid2(target.pos)) {
                started = false;
            }

            lastPos = target.pos;
            lastState = mc.world.getBlockState(target.pos);
        } else {
            lastPos = null;
            lastState = null;
        }

        delta = (System.currentTimeMillis() - lastTime) / 1000d;
        lastTime = System.currentTimeMillis();


        update();
        explodeUpdate();

        if (target == null) return;

        switch (renderMode.get()) {
            case Box -> {
                double progress = MathHelper.clamp(getMineProgress(), 0, 1);

                Color color1 = progress >= 0.95 ? endColor.get() : startColor.get();
                Color color2 = progress >= 0.95 ? lineEndColor.get() : lineStartColor.get();

                event.renderer.box(target.pos, color1, color2, shapeMode.get(), 0);

                // DOUBLE BREAK
                if (doubleBreak.get() && breakPos != null) {
                    double progressD = MathHelper.clamp(getMineProgressDouble(), 0, 1);

                    Color colorD1 = progressD >= 0.95 ? endColorDouble.get() : startColorDouble.get();
                    Color colorD2 = progressD >= 0.95 ? lineEndColorDouble.get() : lineStartColorDouble.get();

                    event.renderer.box(breakPos, colorD1, colorD2, shapeModeDouble.get(), 0);
                }
            }
            case Normal -> {
                int slot = getFastestSlot(target.pos);

                render = MathHelper.clamp(getMineTicks(slot, true) == getMineTicks(slot, false) ? render + delta * 2 : render - delta * 2, -2, 2);

                // Normal Speed
                double p = 1 - MathHelper.clamp(minedFor / getMineTicks(slot, false), 0, 1);
                p = Math.pow(p, animationExp.get());
                p = 1 - p;

                event.renderer.box(getRenderBox(p / 2), getColor(startColor.get(), endColor.get(), p, MathHelper.clamp(render, 0, 1)), getColor(lineStartColor.get(), lineEndColor.get(), p, MathHelper.clamp(render, 0, 1)), shapeMode.get(), 0);

                // Modified Speed
                p = 1 - MathHelper.clamp(minedFor / getMineTicks(slot, true), 0, 1);
                p = Math.pow(p, animationExp.get());
                p = 1 - p;

                event.renderer.box(getRenderBox(p / 2), getColor(startColor.get(), endColor.get(), p, MathHelper.clamp(-render, 0, 1)), getColor(lineStartColor.get(), lineEndColor.get(), p, MathHelper.clamp(-render, 0, 1)), shapeMode.get(), 0);

                // DOUBLE BREAK
                if (doubleBreak.get() && breakPos != null) {
                    int slot2 = getFastestSlot(breakPos);
                    render = MathHelper.clamp(getMineTicksDouble(slot2, true) == getMineTicksDouble(slot2, false) ? render + (delta * 2.0d) : render - (delta * 2.0d), -2.0d, 2.0d);

                    double p22 = 1.0d - Math.pow(1.0d - MathHelper.clamp(this.minedFor / getMineTicksDouble(slot2, false), 0.0d, 1.0d), animationExp.get());
                    event.renderer.box(getRenderBoxDouble(p22 / 2.0d), getColor(startColorDouble.get(), endColorDouble.get(), p22, MathHelper.clamp(render, 0.0d, 1.0d)), getColor(lineStartColorDouble.get(), lineEndColorDouble.get(), p22, MathHelper.clamp(render, 0.0d, 1.0d)), shapeMode.get(), 0);

                    double p23 = 1.0d - Math.pow(1.0d - MathHelper.clamp(this.minedFor / getMineTicksDouble(slot2, true), 0.0d, 1.0d), animationExp.get());
                    event.renderer.box(getRenderBoxDouble(p23 / 2.0d), getColor(startColorDouble.get(), endColor.get(), p23, MathHelper.clamp(-render, 0.0d, 1.0d)), getColor(lineStartColorDouble.get(), lineEndColorDouble.get(), p23, MathHelper.clamp(-render, 0.0d, 1.0d)), shapeMode.get(), 0);
                }
            }
            case Future -> {
                double progress = MathHelper.clamp(getMineProgress(), 0, 1);

                Color color1 = progress >= 0.95 ? endColor.get() : startColor.get();
                Color color2 = progress >= 0.95 ? lineEndColor.get() : lineStartColor.get();

                double min = progress / 2;
                Vec3d vec3d = target.pos.toCenterPos();
                Box box = new Box(vec3d.x - min, vec3d.y - min, vec3d.z - min, vec3d.x + min, vec3d.y + min, vec3d.z + min);

                event.renderer.box(box, color1, color2, shapeMode.get(), 0);

                // DOUBLE BREAK
                if (doubleBreak.get() && breakPos != null) {
                    double progressD = MathHelper.clamp(getMineProgressDouble(), 0, 1);

                    Color colorD1 = progressD >= 0.95 ? endColorDouble.get() : startColorDouble.get();
                    Color colorD2 = progressD >= 0.95 ? lineEndColorDouble.get() : lineStartColorDouble.get();

                    double minD = progress / 2;
                    Vec3d vec3dD = breakPos.toCenterPos();
                    Box boxD = new Box(vec3dD.x - minD, vec3dD.y - minD, vec3dD.z - minD, vec3dD.x + minD, vec3dD.y + minD, vec3dD.z + minD);

                    event.renderer.box(boxD, colorD1, colorD2, shapeModeDouble.get(), 0);
                }
            }
            case Shrink -> {
                int slot = getFastestSlot(target.pos);

                render = MathHelper.clamp(getMineTicks(slot, true) == getMineTicks(slot, false) ? render + delta * 2 : render - delta * 2, -2, 2);

                double mineProgress = MathHelper.clamp(getMineProgress(), 0, 1);
                double progress = 1d - mineProgress;

                double max = ((double) Math.round(progress * 100) / 100);
                double min = 1 - max;

                Vec3d vec3d = new Vec3d(target.pos.getX(), target.pos.getY(), target.pos.getZ());
                Box renderBox = new Box(vec3d.x + min, vec3d.y + min, vec3d.z + min, vec3d.x + max, vec3d.y + max, vec3d.z + max);

                // Normal Speed
                double p = 1 - MathHelper.clamp(minedFor / getMineTicks(slot, false), 0, 1);
                p = Math.pow(p, animationExp.get());
                p = 1 - p;

                event.renderer.box(renderBox, getColor(startColor.get(), endColor.get(), p, MathHelper.clamp(render, 0, 1)), getColor(lineStartColor.get(), lineEndColor.get(), p, MathHelper.clamp(render, 0, 1)), shapeMode.get(), 0);

                // Modified Speed
                p = 1 - MathHelper.clamp(minedFor / getMineTicks(slot, true), 0, 1);
                p = Math.pow(p, animationExp.get());
                p = 1 - p;

                event.renderer.box(renderBox, getColor(startColor.get(), endColor.get(), p, MathHelper.clamp(-render, 0, 1)), getColor(lineStartColor.get(), lineEndColor.get(), p, MathHelper.clamp(-render, 0, 1)), shapeMode.get(), 0);

                // DOUBLE BREAK
                if (doubleBreak.get() && breakPos != null) {
                    int slot2 = getFastestSlot(breakPos);
                    render = MathHelper.clamp(getMineTicksDouble(slot2, true) == getMineTicksDouble(slot2, false) ? render + (delta * 2.0d) : render - (delta * 2.0d), -2.0d, 2.0d);

                    double mineProgressD = MathHelper.clamp(getMineProgressDouble(), 0, 1);
                    double progressD = 1d - mineProgressD;

                    double maxD = ((double) Math.round(progressD * 100) / 100);
                    double minD = 1 - maxD;

                    Vec3d vec3dD = new Vec3d(breakPos.getX(), breakPos.getY(), breakPos.getZ());
                    Box renderBoxD = new Box(vec3dD.x + minD, vec3dD.y + minD, vec3dD.z + minD, vec3dD.x + maxD, vec3dD.y + maxD, vec3dD.z + maxD);

                    double p22 = 1.0d - Math.pow(1.0d - MathHelper.clamp(this.minedFor / getMineTicksDouble(slot2, false), 0.0d, 1.0d), animationExp.get());
                    event.renderer.box(renderBoxD, getColor(startColorDouble.get(), endColorDouble.get(), p22, MathHelper.clamp(render, 0.0d, 1.0d)), getColor(lineStartColorDouble.get(), lineEndColorDouble.get(), p22, MathHelper.clamp(render, 0.0d, 1.0d)), shapeMode.get(), 0);

                    double p23 = 1.0d - Math.pow(1.0d - MathHelper.clamp(this.minedFor / getMineTicksDouble(slot2, true), 0.0d, 1.0d), animationExp.get());
                    event.renderer.box(renderBoxD, getColor(startColorDouble.get(), endColor.get(), p23, MathHelper.clamp(-render, 0.0d, 1.0d)), getColor(lineStartColorDouble.get(), lineEndColorDouble.get(), p23, MathHelper.clamp(-render, 0.0d, 1.0d)), shapeMode.get(), 0);
                }
            }
            case Grow -> {
                int slot = getFastestSlot(target.pos);

                render = MathHelper.clamp(getMineTicks(slot, true) == getMineTicks(slot, false) ? render + delta * 2 : render - delta * 2, -2, 2);

                Box renderBox = new Box(target.pos.getX(), target.pos.getY(), target.pos.getZ(), target.pos.getX() + 1, target.pos.getY() + MathHelper.clamp(getMineProgress(), 0, 1), target.pos.getZ() + 1);

                // Normal Speed
                double p = 1 - MathHelper.clamp(minedFor / getMineTicks(slot, false), 0, 1);
                p = Math.pow(p, animationExp.get());
                p = 1 - p;

                event.renderer.box(renderBox, getColor(startColor.get(), endColor.get(), p, MathHelper.clamp(render, 0, 1)), getColor(lineStartColor.get(), lineEndColor.get(), p, MathHelper.clamp(render, 0, 1)), shapeMode.get(), 0);

                // Modified Speed
                p = 1 - MathHelper.clamp(minedFor / getMineTicks(slot, true), 0, 1);
                p = Math.pow(p, animationExp.get());
                p = 1 - p;

                event.renderer.box(renderBox, getColor(startColor.get(), endColor.get(), p, MathHelper.clamp(-render, 0, 1)), getColor(lineStartColor.get(), lineEndColor.get(), p, MathHelper.clamp(-render, 0, 1)), shapeMode.get(), 0);

                // DOUBLE BREAK
                if (doubleBreak.get() && breakPos != null) {
                    int slot2 = getFastestSlot(breakPos);
                    render = MathHelper.clamp(getMineTicksDouble(slot2, true) == getMineTicksDouble(slot2, false) ? render + (delta * 2.0d) : render - (delta * 2.0d), -2.0d, 2.0d);
                    Box renderBoxDouble = new Box(breakPos.getX(), breakPos.getY(), breakPos.getZ(), breakPos.getX() + 1, breakPos.getY() + MathHelper.clamp(getMineProgressDouble(), 0, 1), breakPos.getZ() + 1);

                    // Normal Speed
                    double p22 = 1.0d - Math.pow(1.0d - MathHelper.clamp(minedFor / getMineTicksDouble(slot2, false), 0.0d, 1.0d), animationExp.get());
                    event.renderer.box(renderBoxDouble, getColor(startColorDouble.get(), endColorDouble.get(), p22, MathHelper.clamp(render, 0.0d, 1.0d)), getColor(lineStartColorDouble.get(), lineEndColorDouble.get(), p22, MathHelper.clamp(render, 0.0d, 1.0d)), shapeMode.get(), 0);

                    // Modified Speed
                    double p23 = 1.0d - Math.pow(1.0d - MathHelper.clamp(minedFor / getMineTicksDouble(slot2, true), 0.0d, 1.0d), animationExp.get());
                    event.renderer.box(renderBoxDouble, getColor(startColorDouble.get(), endColor.get(), p23, MathHelper.clamp(-render, 0.0d, 1.0d)), getColor(lineStartColorDouble.get(), lineEndColorDouble.get(), p23, MathHelper.clamp(-render, 0.0d, 1.0d)), shapeMode.get(), 0);
                }
            }
        }
        if(debug.get()){
            BlockPos pos = target.pos.offset(SettingUtils.getPlaceOnDirection(target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(target.pos));
            Box renderBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            event.renderer.box(renderBox, new Color(255,255,255,70), new Color(255,255,255,0),shapeMode.get(), 0);
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!text.get()  || (target != null && target.manual && manualRangeReset.get() && !SettingUtils.inMineRange(target.pos)) || target == null)
            return;

        Vector3d vec3 = new Vector3d(target.pos.getX() + 0.5, target.pos.getY() + 0.5, target.pos.getZ() + 0.5);

        if (NametagUtils.to2D(vec3, textScale.get())) {
            TextRenderer textRenderer = TextRenderer.get();

            NametagUtils.begin(vec3);
            textRenderer.begin(1, false, true);

            String text = String.valueOf((int) Math.floor(getMineProgress() * 100));
            textRenderer.render(
                isAir(target.pos) ? "Waiting" : text + "%",
                -textRenderer.getWidth(text) / 2.0,
                0.0,
                get2DTextColor(),
                shadow.get()
            );

            textRenderer.end();
            NametagUtils.end();
        }

        if (doubleBreak.get() && breakPos != null) {
            Vector3d vec3Double = new Vector3d(breakPos.getX() + 0.5, breakPos.getY() + 0.5, breakPos.getZ() + 0.5);

            if (NametagUtils.to2D(vec3Double, textScale.get())) {
                TextRenderer textRenderer = TextRenderer.get();

                NametagUtils.begin(vec3Double);
                textRenderer.begin(1, false, true);

                String text = String.valueOf((int) Math.floor(getMineProgressDouble() * 100));
                textRenderer.render(
                    isAir(breakPos) ? "Waiting" : text + "%",
                    -textRenderer.getWidth(text) / 2.0,
                    0.0,
                    get2DTextColorDouble(),
                    shadow.get()
                );

                textRenderer.end();
                NametagUtils.end();
            }
        }
    }

    private Color get2DTextColor() {
        double progress = getMineProgress();
        String text = String.format("%.2f", progress);

        if (manualInsta.get() && text.equals("Infinity")) {
            return waitColor.get();
        }
        return progress >= 0.95 ? endTextColor.get() : startTextColor.get();
    }

    private Color get2DTextColorDouble() {
        double progress = getMineProgressDouble();
        String text = String.format("%.2f", progress);

        if (manualInsta.get() && text.equals("Infinity")) {
            return waitColorDouble.get();
        }
        return progress >= 0.95 ? endTextColorDouble.get() : startTextColorDouble.get();
    }

    private void explodeUpdate() {
        Entity targetCrystal = null;

        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, Long> entry : explodeAt.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > explodeTime.get() * 1000) {
                toRemove.add(entry.getKey());
            }

            EndCrystalEntity crystal = crystalAt(entry.getKey());

            if (crystal != null) {
                targetCrystal = crystal;
                break;
            }
        }
        toRemove.forEach(explodeAt::remove);

        if (targetCrystal != null && !isPaused() && mined && System.currentTimeMillis() - lastExplode > (1000 / explodeSpeed.get())) {
            if (!SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(targetCrystal.getBoundingBox(), priority, RotationType.Attacking, Objects.hash(name + "attacking"))) {

                SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
                sendPacket(PlayerInteractEntityC2SPacket.attack(targetCrystal, mc.player.isSneaking()));
                SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
                if (attackSwing.get()) clientSwing(attackHand.get(), Hand.MAIN_HAND);

                lastExplode = System.currentTimeMillis();

                if (SettingUtils.shouldRotate(RotationType.Attacking))
                    Managers.ROTATION.end(Objects.hash(name + "attacking"));
            }
        }
    }

    public double getMineProgress() {
        if (target == null) return -1;
        return minedFor / getMineTicks(fastestSlot(), true);
    }

    public double getMineProgressDouble() {
        if (breakPos == null) return -1;
        return minedFor / getMineTicksDouble(fastestSlotDouble(), true);
    }

    private void update() {
        if (mc.world == null) return;
        Back();
        if (reset) {
            if (target != null && !target.manual) {
                target = null;
            }
            started = false;
            reset = false;
        }

        enemies = mc.world.getPlayers().stream().filter(player -> player != mc.player && !Friends.get().isFriend(player) && player.distanceTo(mc.player) < 10).toList();

        BlockPos lastPos = target == null || target.pos == null ? null : target.pos;

        if (target != null && target.manual && manualRangeReset.get() && !SettingUtils.inMineRange(target.pos)) {
            minedFor = 0.0d;
            breakPos = null;
            started = false;
            lastTime = System.currentTimeMillis();
            civPos = null;
            reset = false;
        } else if (this.breakPos != null && this.target != null && (mc.world.isAir(breakPos) || this.breakPos.equals(target.pos) || (manualRangeReset.get() && !SettingUtils.inMineRange(this.breakPos)))) {
            breakPos = null;
        }


        if (target == null || !target.manual) target = getTarget();

        if (target == null) return;

        if (target.pos != null && !target.pos.equals(lastPos)) {
            if (started) {
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, target.pos, Direction.DOWN, 0));
            }
            started = false;
        }

        if (!started) {
            boolean rotated = !SettingUtils.startMineRot() || Managers.ROTATION.start(target.pos, priority, RotationType.Mining, Objects.hash(name + "mining"));
            if(BOBlockUtil.getBlock(meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).targetPos()) == Blocks.RESPAWN_ANCHOR && pauseanchor.get()) return;
            if(BOBlockUtil.getBlock(meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).targetPos()) == Blocks.COBWEB && pauseweb.get()) return;

            if (rotated) {
                started = true;
                minedFor = 0;
                civPos = null;

                if (getMineTicks(fastestSlot(), true) == getMineTicks(fastestSlot(), false)) {
                    render = 2;
                } else {
                    render = -2;
                }

                sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target.pos, SettingUtils.getPlaceOnDirection(target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(target.pos), s));
                if (doubleBreak.get()) {
                    sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.target.pos, SettingUtils.getPlaceOnDirection(target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(target.pos), s));
                    sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.target.pos, SettingUtils.getPlaceOnDirection(target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(target.pos), s));
                }
                if(debug.get()){
                    this.error(String.valueOf(SettingUtils.getPlaceOnDirection(target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(target.pos)) + target.pos + "AutoMine 1");
                    this.error(String.valueOf(SettingUtils.getPlaceOnDirection(breakPos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(breakPos)) + breakPos + "AutoMine 2");
                }
                SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);
                mined = false;
                if (mineStartSwing.get()) clientSwing(mineHand.get(), Hand.MAIN_HAND);

                if (SettingUtils.startMineRot()) {
                    Managers.ROTATION.end(Objects.hash(name + "mining"));
                }
            }
        }

        if (!started) return;
        minedFor += delta * 20;

        if (isPaused()) return;
        if (!miningCheck(fastestSlot())) return;
        if (!civCheck()) return;
        if (!crystalCheck()) return;
        if (!BOBlockUtil.solid2(target.pos)) return;

        endMine();
        if (!miningCheckDouble(fastestSlotDouble())) return;
        if (!BOBlockUtil.solid2(breakPos)) return;
        Double();
    }

    private void Double(){
        int slot2 = fastestSlotDouble();
        if(doubleBreak.get()) {
            if (silentDouble.get()) {
                if (breakPos == null || !BOBlockUtil.solid2(breakPos) || (mc.player.isUsingItem() && pauseEat.get())){
                    return;
                }
                boolean rotated2 = !SettingUtils.startMineRot() || Managers.ROTATION.start(breakPos, priority, RotationType.Mining, Objects.hash(name + "mining"));
                if (!(getBlock(breakPos) instanceof AirBlock) && getMineProgressDouble() * 100 > 75) {
                    if(debug.get()){
                        this.error("Silent");
                    }
                    SettingUtils.mineSwing(SwingSettings.MiningSwingState.Double);
                    if(doublesilenttype.get() == silenttype.UpdateSelectedSlotC2SPacket) {
                        sendPacket(new UpdateSelectedSlotC2SPacket(slot2));
                    } else if(doublesilenttype.get() == silenttype.selectedSlot) {
                        if(OldSlot == -1){
                            OldSlot = mc.player.getInventory().selectedSlot;
                        }
                        mc.player.getInventory().selectedSlot = slot2;
                        ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
                    }
                    if(doublesilenttype.get() == silenttype.ClientselectedSwap){
                        int doubleSlot = getFastestSlot(breakPos);
                        if (!(getBlock(breakPos) instanceof AirBlock)) {
                            OldSlot = mc.player.getInventory().selectedSlot;
                            sendPacket(new UpdateSelectedSlotC2SPacket(doubleSlot));
                            canUpdate = true;
                        }
                    }
                    if (SettingUtils.startMineRot()) {
                        Managers.ROTATION.end(Objects.hash(name + "mining"));
                    }
                    canUpdate = true;
                }
            }
        }
    }

    private void Back(){
        if (canUpdate &&  breakPos != null && getBlock(breakPos) instanceof AirBlock) {
            if(debug.get()){
                this.error("Back");
            }
            SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
            if(doublesilenttype.get() == silenttype.UpdateSelectedSlotC2SPacket){
                sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            } else if(doublesilenttype.get() == silenttype.selectedSlot && OldSlot != -1) {
                mc.player.getInventory().selectedSlot = OldSlot;
                ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
                OldSlot = -1;
            }
            canUpdate = false;
        }
        if(Math.floor(getMineProgressDouble() * 100) > 125) {
            if(debug.get()){
                this.error("Back");
            }
            SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
            if(doublesilenttype.get() == silenttype.UpdateSelectedSlotC2SPacket){
                sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            } else
                if(doublesilenttype.get() == silenttype.selectedSlot && OldSlot != -1) {
                mc.player.getInventory().selectedSlot = OldSlot;

                ((IClientPlayerInteractionManager) mc.interactionManager).syncSelected();
                OldSlot = -1;
            }
            if(breakPos != null ) {
                breakPos = null;
            }
            canUpdate = false;
        }

    }

    private boolean isPaused() {
        if (pauseEat.get() && mc.player.isUsingItem()) return true;
        return pauseSword.get() && mc.player.getMainHandStack().getItem() instanceof SwordItem;
    }

    private boolean civCheck() {
        if (civPos == null) return true;
        return !(System.currentTimeMillis() - lastCiv < instaDelay.get() * 1000);
    }

    private void endMine() {
        int slot = fastestSlot();

        boolean switched = miningCheck(Managers.HOLDING.slot);
        boolean swapBack = false;



        if (SettingUtils.shouldRotate(RotationType.Mining) && !Managers.ROTATION.start(target.pos, priority, RotationType.Mining, Objects.hash(name + "mining"))) {
            return;
        }

        if (!switched) {
            switch (pickAxeSwitchMode.get()) {
                case Silent -> {
                    switched = true;
                    InvUtils.swap(slot, true);
                }
                case PickSilent -> {
                    switched = true;
                    BOInvUtils.pickSwitch(slot);
                }
                case InvSwitch -> switched = BOInvUtils.invSwitch(slot);
            }
            swapBack = switched;
        }

        if (!switched) {
            return;
        }

        sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target.pos, SettingUtils.getPlaceOnDirection(target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(target.pos), s));

        mined = true;
        SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
        if (mineEndSwing.get()) clientSwing(mineHand.get(), Hand.MAIN_HAND);

        if (target.civ) {
            civPos = target.pos;
        }

        if (SettingUtils.endMineRot()) {
            Managers.ROTATION.end(Objects.hash(name + "mining"));
        }

        if (swapBack) {
            switch (pickAxeSwitchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.invSwapBack();
            }
        }

        if (target.civ) {
            civPos = target.pos;
            lastCiv = System.currentTimeMillis();
        } else if (target.manual && manualRemine.get()) {
            minedFor = 0;
        } else {
            target = null;
            minedFor = 0;
        }
    }

    private boolean crystalCheck() {
        switch (target.type) {
            case Cev, TrapCev, SurroundCev -> {
                if (crystalAt(target.crystalPos) != null) return true;
                if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(target.crystalPos)).withMaxY(target.crystalPos.getY() + (SettingUtils.cc() ? 1 : 2)), entity -> !entity.isSpectator())) {
                    placeCrystal();
                    return false;
                }
            }
            case AutoCity -> {
                if (crystalAt(target.crystalPos) != null) return true;
                if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(target.crystalPos)).withMaxY(target.crystalPos.getY() + (SettingUtils.cc() ? 1 : 2)), entity -> !entity.isSpectator()))
                    return placeCrystal();
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private int getFastestSlot(BlockPos blockPos) {
        int slot = -1;
        if (mc.player == null || mc.world == null) {
            return -1;
        }
        for (int i = 0; i < (pickAxeSwitchMode.get() == SwitchMode.Silent ? 9 : 35); i++) {
            if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(blockPos)) > mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(blockPos)))) {
                slot = i;
            }
        }
        return slot;
    }

    private EndCrystalEntity crystalAt(BlockPos pos) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal && entity.getBlockPos().equals(pos)) {
                return crystal;
            }
        }
        return null;
    }

    private boolean placeCrystal() {
        if (System.currentTimeMillis() - lastPlace < 250) {
            return false;
        }

        Hand hand = getHand();

        int crystalSlot = InvUtils.find(Items.END_CRYSTAL).slot();
        if (hand == null && crystalSlot < 0) {
            return false;
        }

        Direction dir = SettingUtils.getPlaceOnDirection(target.crystalPos.down());

        if (dir == null) {
            return false;
        }

        boolean rotated = !SettingUtils.shouldRotate(RotationType.Interact) || Managers.ROTATION.start(target.crystalPos.down(), priority, RotationType.Interact, Objects.hash(name + "placing"));

        if (!rotated) {
            return false;
        }

        boolean switched = hand != null;

        if (!switched) {
            switch (crystalSwitchMode.get()) {
                case Silent -> {
                    switched = true;
                    InvUtils.swap(crystalSlot, true);
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(crystalSlot);
                case InvSwitch -> switched = BOInvUtils.invSwitch(crystalSlot);
            }
        }

        if (!switched) {
            return false;
        }

        interactBlock(hand == null ? Hand.MAIN_HAND : hand, target.crystalPos.down().toCenterPos(), dir, target.crystalPos.down());

        if (placeSwing.get()) clientSwing(placeHand.get(), hand == null ? Hand.MAIN_HAND : hand);

        lastPlace = System.currentTimeMillis();

        if (shouldExplode()) {
            addExplode();
        }

        if (SettingUtils.shouldRotate(RotationType.Interact)) Managers.ROTATION.end(Objects.hash(name + "placing"));

        if (hand == null) {
            switch (crystalSwitchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.invSwapBack();
            }
        }

        return true;
    }

    private void addExplode() {
        explodeAt.remove(target.crystalPos);
        explodeAt.put(target.crystalPos, System.currentTimeMillis());
    }

    private boolean shouldExplode() {
        return switch (target.type) {
            case Cev, SurroundCev, TrapCev -> true;
            case SurroundMiner, AntiBurrow, Manual -> false;
            case AutoCity -> explodeCrystal.get();
        };
    }

    private Target getTarget() {
        Target target = null;

        if (!autoMine.get()) {
            return target;
        }

        if (priorityCheck(target, cevPriority.get())) {
            Target t = getCev();
            if (t != null) {
                if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(getBlock(t.pos)))
                    return null;
                if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(getBlock(t.pos)))
                    return null;

                target = t;
            }
        }
        if (priorityCheck(target, trapCevPriority.get())) {
            Target t = getTrapCev();
            if (t != null) {
                if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(getBlock(t.pos)))
                    return null;
                if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(getBlock(t.pos)))
                    return null;

                target = t;
            }
        }
        if (priorityCheck(target, surroundCevPriority.get())) {
            Target t = getSurroundCev();
            if (t != null) {
                if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(getBlock(t.pos)))
                    return null;
                if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(getBlock(t.pos)))
                    return null;

                target = t;
            }
        }
        if (priorityCheck(target, surroundMinerPriority.get())) {
            Target t = getSurroundMiner();
            if (t != null) {
                if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(getBlock(t.pos)))
                    return null;
                if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(getBlock(t.pos)))
                    return null;

                target = t;
            }
        }
        if (priorityCheck(target, autoCityPriority.get())) {
            Target t = getAutoCity();
            if (t != null) {
                if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(getBlock(t.pos)))
                    return null;
                if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(getBlock(t.pos)))
                    return null;

                target = t;
            }
        }
        if (priorityCheck(target, antiBurrowPriority.get())) {
            Target t = getAntiBurrow();
            if (t != null) {
                if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(getBlock(t.pos)))
                    return null;
                if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(getBlock(t.pos)))
                    return null;

                target = t;
            }
        }
        return target;
    }

    private Target getCev() {
        boolean civ = instaCev.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            BlockPos pos = new BlockPos(player.getBlockX(), (int) Math.floor(player.getBoundingBox().maxY) + 1, player.getBlockZ());

            if (!(civ && pos.equals(civPos)) && getBlock(pos) != Blocks.OBSIDIAN) {
                continue;
            }
            if ((civ && pos.equals(civPos)) && !(getBlock(pos) instanceof AirBlock) && getBlock(pos) != Blocks.OBSIDIAN) {
                continue;
            }

            if (getBlock(pos.up()) != Blocks.AIR) {
                continue;
            }
            if (SettingUtils.oldCrystals() && getBlock(pos.up(2)) != Blocks.AIR) {
                continue;
            }

            if (!SettingUtils.inMineRange(pos)) {
                continue;
            }
            if (!SettingUtils.inPlaceRange(pos)) {
                continue;
            }
            if (!SettingUtils.inAttackRange(RSCombatInfo.getCrystalBox(pos.up()))) {
                continue;
            }

            if (blocked(pos.up())) {
                continue;
            }

            double d = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));

            if (distanceCheck(civ, pos, distance, d)) {
                best = new Target(pos, pos.up(), MineType.Cev, cevPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                distance = d;
            }
        }
        return best;
    }

    private Target getTrapCev() {
        boolean civ = instaTrapCev.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos pos = new BlockPos(player.getBlockX(), (int) Math.floor(player.getBoundingBox().maxY), player.getBlockZ()).offset(dir);

                if (!(civ && pos.equals(civPos)) && getBlock(pos) != Blocks.OBSIDIAN) {
                    continue;
                }
                if ((civ && pos.equals(civPos)) && !(getBlock(pos) instanceof AirBlock) && getBlock(pos) != Blocks.OBSIDIAN) {
                    continue;
                }

                if (getBlock(pos.up()) != Blocks.AIR) {
                    continue;
                }
                if (SettingUtils.oldCrystals() && getBlock(pos.up(2)) != Blocks.AIR) {
                    continue;
                }

                if (!SettingUtils.inMineRange(pos)) {
                    continue;
                }
                if (!SettingUtils.inPlaceRange(pos)) {
                    continue;
                }
                if (!SettingUtils.inAttackRange(RSCombatInfo.getCrystalBox(pos.up()))) {
                    continue;
                }

                if (blocked(pos.up())) {
                    continue;
                }

                double d = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, pos.up(), MineType.TrapCev, trapCevPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }

    private Target getSurroundCev() {
        boolean civ = instaSurroundCev.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos pos = getPos(player.getPos()).offset(dir);

                if (!(civ && pos.equals(civPos)) && getBlock(pos) != Blocks.OBSIDIAN) {
                    continue;
                }
                if ((civ && pos.equals(civPos)) && !(getBlock(pos) instanceof AirBlock) && getBlock(pos) != Blocks.OBSIDIAN) {
                    continue;
                }

                if (getBlock(pos.up()) != Blocks.AIR) {
                    continue;
                }
                if (SettingUtils.oldCrystals() && getBlock(pos.up(2)) != Blocks.AIR) {
                    continue;
                }

                if (!SettingUtils.inMineRange(pos)) {
                    continue;
                }
                if (!SettingUtils.inPlaceRange(pos)) {
                    continue;
                }
                if (!SettingUtils.inAttackRange(RSCombatInfo.getCrystalBox(pos.up()))) {
                    continue;
                }

                if (blocked(pos.up())) {
                    continue;
                }

                double d = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, pos.up(), MineType.SurroundCev, surroundCevPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }

    private Target getSurroundMiner() {
        boolean civ = instaSurroundMiner.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos pos = getPos(player.getPos()).offset(dir);

                if (((!civ || !pos.equals(civPos)) && !BOBlockUtil.solid2(pos)) || getBlock(pos) == Blocks.BEDROCK) {
                    continue;
                }

                if (!SettingUtils.inMineRange(pos)) {
                    continue;
                }

                double d = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, null, MineType.SurroundMiner, surroundMinerPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }

    private Target getAutoCity() {
        boolean civ = instaAutoCity.get();
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos pos = getPos(player.getPos()).offset(dir);

                if (((!civ || !pos.equals(civPos)) && !BOBlockUtil.solid2(pos)) || getBlock(pos) == Blocks.BEDROCK) {
                    continue;
                }

                if (getBlock(pos.offset(dir)) != Blocks.AIR) {
                    continue;
                }
                if (SettingUtils.oldCrystals() && getBlock(pos.offset(dir).up()) != Blocks.AIR) {
                    continue;
                }
                if (!crystalBlock(pos.offset(dir).down())) {
                    continue;
                }

                if (!SettingUtils.inMineRange(pos)) {
                    continue;
                }
                if (!SettingUtils.inPlaceRange(pos.offset(dir).down())) {
                    continue;
                }

                if (blocked(pos.offset(dir))) {
                    continue;
                }

                double d = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));

                if (distanceCheck(civ, pos, distance, d)) {
                    best = new Target(pos, pos.offset(dir), MineType.AutoCity, autoCityPriority.get().priority + (civ && pos.equals(civPos) ? 0.1 : 0), civ, false);
                    distance = d;
                }
            }
        }
        return best;
    }

    private Target getAntiBurrow() {
        Target best = null;
        double distance = 1000;
        for (AbstractClientPlayerEntity player : enemies) {
            BlockPos pos = getPos(player.getPos());

            if (!BOBlockUtil.solid2(pos) || getBlock(pos) == Blocks.BEDROCK || getBlock(pos) == Blocks.COBWEB) {
                continue;
            }

            if (!SettingUtils.inMineRange(pos)) {
                continue;
            }

            double d = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));

            if (d < distance) {
                best = new Target(pos, null, MineType.AntiBurrow, antiBurrowPriority.get().priority, false, false);
                distance = d;
            }
        }
        return best;
    }

    private boolean distanceCheck(boolean civ, BlockPos pos, double closest, double distance) {
        if (civ && pos.equals(civPos)) {
            return true;
        }
        if (target != null && pos.equals(target.pos)) {
            return true;
        }

        return distance < closest;
    }

    private boolean priorityCheck(Target current, Priority priority) {
        if (priority.priority < 0) {
            return false;
        }
        if (current == null) {
            return true;
        }

        return priority.priority >= current.priority;
    }

    private void abort(BlockPos pos) {
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP));

        started = false;
    }

    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    private Hand getHand() {
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
            return Hand.OFF_HAND;
        }
        if (Managers.HOLDING.isHolding(Items.END_CRYSTAL)) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    private boolean miningCheck(int slot) {
        if (target == null || target.pos == null) {
            return false;
        }
        return minedFor * speed.get() >= getMineTicks(slot, true);
    }

    private boolean miningCheckDouble(int slot) {
        if (breakPos == null) {
            return false;
        }
        return minedFor * speed.get() >= getMineTicksDouble(slot, true);
    }

    private float getTime(BlockPos pos, int slot, boolean speedMod) {
        BlockState state = mc.world.getBlockState(pos);
        float f = state.getHardness(mc.world, pos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            float i = !state.isToolRequired() || mc.player.getInventory().getStack(slot).isSuitableFor(state) ? 30 : 100;
            return getSpeed(state, slot, speedMod) / f / i;
        }
    }

    private float getMineTicks(int slot, boolean speedMod) {
        return slot == -1 ? slot : (float) (1 / (getTime(target.pos, slot, speedMod) * speed.get()));
    }

    private float getMineTicksDouble(int slot, boolean speedMod) {
        return slot == -1 ? slot : (float) (1 / (getTime(breakPos, slot, speedMod) * speed.get()));
    }

    private float getSpeed(BlockState state, int slot, boolean speedMod) {
        ItemStack stack = mc.player.getInventory().getStack(slot);
        float f = mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
        if (f > 1.0) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) f += (float) (i * i + 1);
        }

        if (!speedMod) return f;


        if (effectCheck.get()) {
            if (StatusEffectUtil.hasHaste(mc.player)) {
                f *= 1.0 + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
            }
            if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                f *= Math.pow(0.3, mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier() + 1);
            }
        }

        if (waterCheck.get() && mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0;
        }

        if (onGroundCheck.get() && !mc.player.isOnGround()) {
            f /= 5.0;
        }

        return f;
    }

    public void onStart(BlockPos pos, Direction direction) {
        if (target != null && target.manual && pos.equals(target.pos)) {
            abort(target.pos);
            civPos = null;
            target = null;
            return;
        }

        if (this.target != null && this.breakPos == null && !mc.world.isAir(target.pos)) {
            this.breakPos = targetPos();
        }

        if (manualMine.get() && getBlock(pos) != Blocks.BEDROCK) {
            started = false;
            target = new Target(pos, null, MineType.Manual, 0, manualInsta.get(), true);
        }
    }

    public void onAbort(BlockPos pos) {
    }

    public void onStop() {
        target = null;
        started = false;
    }

    private int fastestSlot() {
        int slot = -1;
        if (mc.player == null || mc.world == null) {
            return -1;
        }
        for (int i = 0; i < (pickAxeSwitchMode.get() == SwitchMode.Silent ? 9 : 35); i++) {
            if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(target.pos)) > mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(target.pos)))) {
                slot = i;
            }
        }
        return slot;
    }

    private int fastestSlotDouble() {
        int slot = -1;
        if (mc.player == null || mc.world == null) {
            return -1;
        }
        if(breakPos == null){
            return -1;
        }
        for (int i = 0; i < (pickAxeSwitchMode.get() == SwitchMode.Silent ? 9 : 35); i++) {
            if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(breakPos)) > mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(breakPos)))) {
                slot = i;
            }
        }
        return slot;
    }

    private Color getColor(Color start, Color end, double progress, double alphaMulti) {
        return new Color(
            lerp(start.r, end.r, progress, 1),
            lerp(start.g, end.g, progress, 1),
            lerp(start.b, end.b, progress, 1),
            lerp(start.a, end.a, progress, alphaMulti));
    }

    private int lerp(double start, double end, double d, double multi) {
        return (int) Math.round((start + (end - start) * d) * multi);
    }

    private boolean crystalBlock(BlockPos pos) {
        return getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.BEDROCK;
    }

    private Box getRenderBox(double progress) {
        return new Box(target.pos.getX() + 0.5 - progress, target.pos.getY() + 0.5 - progress, target.pos.getZ() + 0.5 - progress, target.pos.getX() + 0.5 + progress, target.pos.getY() + 0.5 + progress, target.pos.getZ() + 0.5 + progress);
    }

    private Box getRenderBoxDouble(double progress) {
        return new Box(breakPos.getX() + 0.5 - progress, breakPos.getY() + 0.5 - progress, breakPos.getZ() + 0.5 - progress, breakPos.getX() + 0.5 + progress, breakPos.getY() + 0.5 + progress, breakPos.getZ() + 0.5 + progress);
    }

    private boolean blocked(BlockPos pos) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + (SettingUtils.cc() ? 1 : 2), pos.getZ() + 1);

        return EntityUtils.intersectsWithEntity(box, entity -> entity instanceof PlayerEntity && !entity.isSpectator());
    }

    public BlockPos targetPos() {
        return target == null ? null : target.pos;
    }

    private BlockPos getPos(Vec3d vec) {
        return new BlockPos((int) Math.floor(vec.x), (int) Math.round(vec.y), (int) Math.floor(vec.z));
    }

    private boolean isAir(BlockPos blockPos) {
        return mc.world.isAir(blockPos) || getBlock(blockPos).equals(Blocks.FIRE);
    }

    public enum ListMode {
        Whitelist,
        Blacklist
    }

    public enum SwitchMode {
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum RenderMode {
        Box,
        Normal,
        Future,
        Shrink,
        Grow,
    }

    public enum Priority {
        Highest(6),
        Higher(5),
        High(4),
        Normal(3),
        Low(2),
        Lower(1),
        Lowest(0),
        Disabled(-1);

        public final int priority;

        Priority(int priority) {
            this.priority = priority;
        }
    }

    public enum MineType {
        Cev,
        TrapCev,
        SurroundCev,
        SurroundMiner,
        AutoCity,
        AntiBurrow,
        Manual
    }

    public enum silenttype {
        UpdateSelectedSlotC2SPacket,
        selectedSlot,
        ClientselectedSwap


    }

    public record Target(BlockPos pos, BlockPos crystalPos, MineType type, double priority, boolean civ,
                          boolean manual) {
    }
}
