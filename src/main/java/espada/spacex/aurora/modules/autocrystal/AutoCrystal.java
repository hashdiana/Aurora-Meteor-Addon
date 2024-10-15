package espada.spacex.aurora.modules.autocrystal;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.mixins.IInteractEntityC2SPacket;
import espada.spacex.aurora.modules.autocrystal.crystalinfo.*;
import espada.spacex.aurora.modules.automine.AuroraMine;
import espada.spacex.aurora.modules.maojunqing.MaoJunQingAura;
import espada.spacex.aurora.modules.PistonCrystal;
import espada.spacex.aurora.modules.Suicide;
import espada.spacex.aurora.timers.TimerList;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.MixinPlugin;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.joml.Vector3d;

import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import static espada.spacex.aurora.modules.autocrystal.abstractpriorit.AirCheck.air;
import static espada.spacex.aurora.modules.autocrystal.abstractpriorit.AliveCheck.isAlive;
import static espada.spacex.aurora.modules.autocrystal.abstractpriorit.HandCheck.getHand;
import static espada.spacex.aurora.modules.autocrystal.abstractpriorit.RangeCheck.inPlaceRange;
import static espada.spacex.aurora.modules.autocrystal.abstractpriorit.CrystalPlaceBlockCheck.crystalBlock;

/**
 * @author OLEPOSSU
 */

public class AutoCrystal extends Modules {

    public AutoCrystal() {
        super(Aurora.Extendcombat, "AutoCrystal", "dobetterbyalexjonny");
    }
    private final SettingGroup sgExtrapolation = settings.createGroup("Predict");
    private final SettingGroup sgHyperid = settings.createGroup("HyperidCalc");
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgExplode = settings.createGroup("Break");
    private final SettingGroup sgSetDead = settings.createGroup("SetDead");
    private final SettingGroup sgobi = settings.createGroup("Obsidian");
    private final SettingGroup sgFriend = settings.createGroup("FriendCheck");
    private final SettingGroup sgMulti = settings.createGroup("MultiCalc");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgAutoMine = settings.createGroup("AutoMine");
    private final SettingGroup sgID = settings.createGroup("ID Predict");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------Misc--------------------//
    private final Setting<Integer> maxtarget = Misc.maxtarget(sgMisc);
    private final Setting<Boolean> pauseEat = Misc.Pause(sgMisc);
    private final Setting<Boolean> smartRot = Misc.smartRot(sgMisc);
    private final Setting<Boolean> ignoreTerrain = Misc.ignoreTerrain(sgMisc);
    private final Setting<Boolean> OnAnchorPlacePause = Misc.OnAnchorPlacePause(sgMisc);
    //--------------------fps--------------------//
    private final Setting<Boolean> performance = MultiTask.performance(sgMulti);
    private final Setting<Boolean> preplacecalc = MultiTask.preplacecalc(sgMulti);
    private final Setting<Boolean> preplacepos = MultiTask.preplacepos(sgMulti);
    private final Setting<Boolean> preplacedir = MultiTask.preplacedir(sgMulti);

    /*
    INFO的分支过于麻烦，浪费时间。
    我需要一步一步来
    */

    //--------------------Place--------------------//
    private final Setting<Boolean> place = Place.Place(sgPlace);
    private final Setting<Boolean> instantPlace = Place.instantPlace(sgPlace);
    private final Setting<Double> speedLimit = Place.speedLimit(sgPlace);
    private final Setting<Double> placeSpeed = Place.placeSpeed(sgPlace);
    private final Setting<AutoCrystalType.DelayMode> placeDelayMode = sgPlace.add(new EnumSetting.Builder<AutoCrystalType.DelayMode>().name("Place Delay Mode").description("Should we count the delay in seconds or ticks.").defaultValue(AutoCrystalType.DelayMode.Seconds).build());
    private final Setting<Double> placeDelay = Place.placeDelay(sgPlace);
    private final Setting<Double> placeDelayTicks = Place.placeDelayTicks(sgPlace);
    private final Setting<Double> MinDmg = Place.MinDmg(sgPlace);
    private final Setting<Double> maxPlace = Place.maxPlace(sgPlace);
    private final Setting<Double> minPlaceRatio = Place.minPlaceRatio(sgPlace);

    //--------------------Break--------------------//
    private final Setting<Boolean> Break = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.Break(sgExplode);
    private final Setting<Boolean> onlyOwn = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.onlyOwn(sgExplode);
    private final Setting<AutoCrystalType.DelayMode> existedMode = sgExplode.add(new EnumSetting.Builder<AutoCrystalType.DelayMode>().name("Existed Mode").description("Should crystal existed times be counted in seconds or ticks.").defaultValue(AutoCrystalType.DelayMode.Ticks).build());
    private final Setting<Integer> existed = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.existed(sgExplode);
    private final Setting<Double> existedTicks = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.existedTicks(sgExplode);
    private final Setting<AutoCrystalType.SequentialMode> sequential = sgExplode.add(new EnumSetting.Builder<AutoCrystalType.SequentialMode>().name("Sequential").description("Doesn't place and attack during the same tick.").defaultValue(AutoCrystalType.SequentialMode.Disabled).build());
    public final Setting<Boolean> instantAttack = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.instantAttack(sgExplode);
    private final Setting<Double> expSpeedLimit = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.expSpeedLimit(sgExplode);
    private final Setting<Double> expSpeed = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.expSpeed(sgExplode);
    private final Setting<Double> minExplode = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.minExplode(sgExplode);
    private final Setting<Double> maxExp = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.maxExp(sgExplode);

    private final Setting<Double> minExpRatio = espada.spacex.aurora.modules.autocrystal.crystalinfo.Break.minExpRatio(sgExplode);
    ///----------------------dead
    private final Setting<Boolean> FastDead = espada.spacex.aurora.modules.autocrystal.crystalinfo.SetDead.FastDead(sgSetDead);
    private final Setting<Boolean> PauseDead = espada.spacex.aurora.modules.autocrystal.crystalinfo.SetDead.PauseDead(sgSetDead);
    //-------------------------obi----------------//
    private final Setting<ObsidianHelper.Mode> obsidian = sgobi.add(new EnumSetting.Builder<ObsidianHelper.Mode>()
        .name("Obsidian")
        .description("It's not done yet. will add in next time")
        .defaultValue(ObsidianHelper.Mode.none)
        .build()
    );
    private final Setting<Double> speed = sgobi.add(new DoubleSetting.Builder()
        .name("Place_Speed")
        .description("w")
        .defaultValue(4)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    //---------------------------friend-------------------------//
    private final Setting<Double> minFriendPlaceRatio = friend.minFriendPlaceRatio(sgFriend);
    private final Setting<Double> maxFriendPlace = friend.maxFriendPlace(sgFriend);
    private final Setting<Double> maxFriendExp = friend.maxFriendExp(sgFriend);
    private final Setting<Double> minFriendExpRatio = friend.minFriendExpRatio(sgFriend);




    //--------------------Desyncforce--------------------//
    private final Setting<AutoCrystalType.SwitchMode> switchMode = sgHyperid.add(new EnumSetting.Builder<AutoCrystalType.SwitchMode>().name("Switch Mode").description("Mode for switching to crystal in main hand.").defaultValue(AutoCrystalType.SwitchMode.Disabled).build());
    private final Setting<Double> CoolDown = HyperCalc.CoolDown(sgHyperid);
    private final Setting<Double> slowDamage = HyperCalc.slowDamage(sgHyperid);
    private final Setting<Double> slowSpeed = HyperCalc.slowSpeed(sgHyperid);
    private final Setting<AutoCrystalType.ExplodeMode> expMode = sgHyperid.add(new EnumSetting.Builder<AutoCrystalType.ExplodeMode>().name("Explode Damage Mode").description("Which things should be checked for exploding.").defaultValue(AutoCrystalType.ExplodeMode.Crystal).build());
    private final Setting<AutoCrystalType.calcMode> calcMode = sgHyperid.add(new EnumSetting.Builder<AutoCrystalType.calcMode>().name("calcMode ").description("3arthh = always safe, meteor = need selfcheck set to selfsafe").defaultValue(AutoCrystalType.calcMode.Normal).build());
    private final Setting<Double> Desyncforce = HyperCalc.Desyncforce(sgHyperid);
    private final Setting<Double> selfCheck = HyperCalc.selfCheck(sgHyperid);
    //--------------------ID-Predict--------------------//
    private final Setting<Boolean> idPredict = IDPreidct.idPredict(sgID);
    private final Setting<Integer> idStartOffset = IDPreidct.idStartOffset(sgID);
    private final Setting<Integer> idOffset = IDPreidct.idOffset(sgID);
    private final Setting<Integer> idPackets = IDPreidct.idPackets(sgID);
    private final Setting<Double> idDelay = IDPreidct.idDelay(sgID);
    private final Setting<Double> idPacketDelay =IDPreidct.idPacketDelay(sgID);

    //--------------------betterPredict--------------------//
    private final Setting<Integer> placeExtrap = Extrap.placeExtrap(sgExtrapolation);
    private final Setting<Integer> BreakExtrap = Extrap.breakExtrap(sgExtrapolation);
    private final Setting<Integer> rangePre = Extrap.rangePre(sgExtrapolation);
    private final Setting<Integer> blockextrap = Extrap.block(sgExtrapolation);
    private final Setting<Integer> Self = Extrap.Self(sgExtrapolation);
    private final Setting<Integer> PlaceExtrapTick = Extrap.PlaceExtrapTick(sgExtrapolation);
    private final Setting<Boolean> renderExt = Extrap.renderExt(sgExtrapolation);
    private final Setting<Boolean> renderSelfExt = Extrap.renderSelfExt(sgExtrapolation);


    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = Render.placeSwing(sgRender);
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>().name("Place-Hand").description("Which hand should be swung.").defaultValue(SwingHand.RealHand).visible(placeSwing::get).build());
    private final Setting<Boolean> attackSwing = Render.attackSwing(sgRender);
    private final Setting<SwingHand> attackHand = sgRender.add(new EnumSetting.Builder<SwingHand>().name("Attack-Hand").description("Which hand should be swung.").defaultValue(SwingHand.RealHand).visible(attackSwing::get).build());
    private final Setting<Boolean> render = Render.Render(sgRender);
    private final Setting<AutoCrystalType.RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<AutoCrystalType.RenderMode>().name("RenderMode").description("What should the render look like.").defaultValue(AutoCrystalType.RenderMode.Smooth).build());
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder().name("Tick-Time").description("ticktime2 by alexjonny testing").defaultValue(0.5).min(0).sliderRange(0, 10).visible(() -> renderMode.get() != AutoCrystalType.RenderMode.Smooth).build());
    private final Setting<AutoCrystalType.FadeMode> fadeMode = sgRender.add(new EnumSetting.Builder<AutoCrystalType.FadeMode>().name("Fade_Mode").description("How long the fading should take.").defaultValue(AutoCrystalType.FadeMode.Normal).visible(() -> renderMode.get().equals(AutoCrystalType.RenderMode.Smooth)).build());
    private final Setting<AutoCrystalType.MotionOutMode> MotionOutFadeMode = sgRender.add(new EnumSetting.Builder<AutoCrystalType.MotionOutMode>().name("MotionOut_FadeMode").description("How long the fading should take.").defaultValue(AutoCrystalType.MotionOutMode.None).visible(() -> renderMode.get().equals(AutoCrystalType.RenderMode.MotionOut)).build());
    private final Setting<AutoCrystalType.EarthFadeMode> earthFadeMode = sgRender.add(new EnumSetting.Builder<AutoCrystalType.EarthFadeMode>().name("Earthhack_FadeMode").description(".").defaultValue(AutoCrystalType.EarthFadeMode.Normal).visible(() -> renderMode.get() == AutoCrystalType.RenderMode.Earthhack).build());
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder().name("Fade_Time").description("fade out.").defaultValue(2).min(0).sliderRange(0, 10).visible(() -> renderMode.get() != AutoCrystalType.RenderMode.Smooth).build());
    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder().name("Animation Move Speed").description("How fast should aurora mode box move.").defaultValue(1).min(0).sliderRange(0, 10).visible(() -> renderMode.get().equals(AutoCrystalType.RenderMode.Smooth) || renderMode.get().equals(AutoCrystalType.RenderMode.MotionOut)).build());
    private final Setting<Double> animationMoveExponent = sgRender.add(new DoubleSetting.Builder().name("Animation Move Exponent").description("Moves faster when longer away from the target.").defaultValue(2).min(0).sliderRange(0, 10).visible(() -> renderMode.get().equals(AutoCrystalType.RenderMode.Smooth) || renderMode.get().equals(AutoCrystalType.RenderMode.MotionOut)).build());
    private final Setting<Double> HyperionExponent = sgRender.add(new DoubleSetting.Builder().name("HyperionExponent").description("Greater than 3 = bug").defaultValue(2).min(0).sliderRange(0, 2).visible(() -> renderMode.get().equals(AutoCrystalType.RenderMode.MotionOut)).build());
    private final Setting<Double> animationExponent = sgRender.add(new DoubleSetting.Builder().name("Animation Exponent").description("How fast should aurora mode box grow.").defaultValue(3).min(0).sliderRange(0, 10).visible(() -> renderMode.get().equals(AutoCrystalType.RenderMode.Smooth)).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("Shape Mode").description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> lineColor = Render.lineColor(sgRender);

    public final Setting<SettingColor> color = Render.color(sgRender);
    private final Setting<Boolean> renderTargetEsp = Render.renderTargetEsp(sgRender);
    private final Setting<SettingColor> color2 = Render.color2(sgRender);
    private final Setting<Boolean> renderDmg = Render.renderDmg(sgRender);
    private final Setting<Double> scale = Render.scale(sgRender);
    private final Setting<Integer> decimal = Render.decimal(sgRender);
    private final Setting<SettingColor> damageColor = Render.damageColor(sgRender);

    //--------------------Automine--------------------//
    public final Setting<Double> autoMineDamage = AutoMine.autoMineDamage(sgAutoMine);
    public final Setting<Boolean> amPlace = AutoMine.amPlace(sgAutoMine);
    public final Setting<Double> amProgress = AutoMine.amProgress(sgAutoMine);
    public final Setting<Boolean> amSpam = AutoMine.amSpam(sgAutoMine);
    private final Setting<AutoCrystalType.AutoMineBrokenMode> amBroken = AutoMine.amBroken(sgAutoMine);
    private final Setting<Boolean> paAttack = AutoMine.paAttack(sgAutoMine);
    private final Setting<Boolean> paPlace = AutoMine.paPlace(sgAutoMine);

    //--------Type

    private long ticksEnabled = 0;
    private double placeTimer = 0;
    private double placeLimitTimer = 0;
    private double delayTimer = 0;
    private int delayTicks = 0;
    public BlockPos placePos = null;
    private Direction placeDir = null;
    private Entity expEntity = null;
    private Box expEntityBB = null;
    private final TimerList<Integer> attackedList = new TimerList<>();
    private final Map<BlockPos, Long> existedList = new HashMap<>();
    private final Map<BlockPos, Long> existedTicksList = new HashMap<>();
    private final Map<BlockPos, Long> own = new HashMap<>();
    private final Map<AbstractClientPlayerEntity, Box> extPos = new HashMap<>();
    private final Map<AbstractClientPlayerEntity, Box> extHitbox = new HashMap<>();
    private Vec3d rangePos = null;
    private final List<Box> blocked = new ArrayList<>();
    private final Map<BlockPos, Double[]> earthMap = new HashMap<>();
    private double attackTimer = 0;
    private double switchTimer = 0;
    private int confirmed = Integer.MIN_VALUE;
    private long lastMillis = System.currentTimeMillis();
    private boolean suicide = false;
    public static boolean placing = false;
    public long lastAttack = 0;

    private Vec3d renderTarget = null;
    private Vec3d renderPos = null;
    private double renderProgress = 0;

    public static AuroraMine autoMine = null;

    public int placed = 0;

    private double cps = 0;
    private final List<Long> explosions = Collections.synchronizedList(new ArrayList<>());

    private final List<Predict> predicts = new ArrayList<>();
    private final List<SetDead> setDeads = new ArrayList<>();
    private MaoJunQingAura autoAnchor = null;
    public PlayerEntity bestTarget = null;
    public final List<PlayerEntity> targets = new ArrayList<>();

    @Override
    public void onActivate() {
        super.onActivate();
        ticksEnabled = 0;
        targets.clear();

        earthMap.clear();
        existedTicksList.clear();
        existedList.clear();
        blocked.clear();
        extPos.clear();
        own.clear();
        renderPos = null;
        renderProgress = 0;
        lastMillis = System.currentTimeMillis();
        attackedList.clear();
        lastAttack = 0;

        predicts.clear();
        setDeads.clear();
        autoAnchor = meteordevelopment.meteorclient.systems.modules.Modules.get().get(MaoJunQingAura.class);
    }
    // add Type in all mode.
    private AutoCrystalType.SwitchMode SwitchMode;
    private AutoCrystalType.calcMode calc;
    private AutoCrystalType.DelayMode DelayMode;
    private AutoCrystalType.RenderMode RenderMode;
    private AutoCrystalType.ExplodeMode ExplodeMode;

    @Override
    public String getInfoString() {
        super.getInfoString();
        return bestTarget != null ? EntityUtils.getName(bestTarget) : null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPost(TickEvent.Post event) {
        delayTicks++;
        ticksEnabled++;
        placed++;

        if (mc.player == null || mc.world == null) return;

        if (autoMine == null) autoMine = meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);

        ListenerExtrapolation.extrapolateMap(extPos, player -> player == mc.player ? Self.get() : placeExtrap.get(), player -> PlaceExtrapTick.get());
        ListenerExtrapolation.extrapolateMap(extHitbox, player -> BreakExtrap.get(), player -> PlaceExtrapTick.get());
        ListenerExtrapolation.extrapolateblock(mc.player);blockextrap.get();

        Box rangeBox = ListenerExtrapolation.extrapolate(mc.player, rangePre.get(), PlaceExtrapTick.get());
        if (rangeBox == null) rangePos = mc.player.getEyePos();
        else rangePos = new Vec3d((rangeBox.minX + rangeBox.maxX) / 2f, rangeBox.minY + mc.player.getEyeHeight(mc.player.getPose()), (rangeBox.minZ + rangeBox.maxZ) / 2f);

        List<BlockPos> toRemove = new ArrayList<>();
        existedList.forEach((key, val) -> {
            if (System.currentTimeMillis() - val >= 5000 + existed.get() * 1000)
                toRemove.add(key);
        });
        toRemove.forEach(existedList::remove);

        toRemove.clear();
        existedTicksList.forEach((key, val) -> {
            if (ticksEnabled - val >= 100 + existedTicks.get())
                toRemove.add(key);
        });
        toRemove.forEach(existedTicksList::remove);

        toRemove.clear();
        own.forEach((key, val) -> {
            if (System.currentTimeMillis() - val >= 5000)
                toRemove.add(key);
        });
        toRemove.forEach(own::remove);

        if (performance.get()) updatePlacement();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        attackedList.update();

        if (autoMine == null) autoMine = meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);

        suicide = meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Suicide.class);
        double delta = (System.currentTimeMillis() - lastMillis) / 1000f;
        lastMillis = System.currentTimeMillis();

        cps = 0;
        synchronized (explosions) {
            explosions.removeIf(time -> {
                double p = (System.currentTimeMillis() - time) / 1000D;

                if (p >= 5) return true;

                double d = p <= 4 ? 1 : 1 - (p - 4);
                cps += d;
                return false;
            });
        }
        cps /= 4.5;

        attackedList.update();
        attackTimer = Math.max(attackTimer - delta, 0);
        placeTimer = Math.max(placeTimer - delta * getSpeed(), 0);
        placeLimitTimer += delta;
        delayTimer += delta;
        switchTimer = Math.max(0, switchTimer - delta);

        update();
        checkDelayed();

        if (renderTargetEsp.get() && bestTarget != null && placePos != null) {
            RenderUtils.drawJello(event.matrices, bestTarget, color2.get());
        }

        //Rendering
        if (render.get()) {
            switch (renderMode.get()) {
                case Smooth -> {
                    if (placePos != null && !isPaused() && holdingCheck() && !isAnchor()) {
                        renderProgress = Math.min(1, renderProgress + delta);
                        renderTarget = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ());
                    } else {
                        renderProgress = Math.max(0, renderProgress - delta);
                    }

                    if (renderTarget != null) {
                        renderPos = smoothMove(renderPos, renderTarget, delta * animationSpeed.get() * 5);
                    }

                    if (renderPos != null) {
                        double r = 0.5 - Math.pow(1 - renderProgress, animationExponent.get()) / 2f;

                        if (r >= 0.001) {
                            double down = -0.5;
                            double up = -0.5;
                            double width = 0.5;

                            switch (fadeMode.get()) {
                                case Up -> {
                                    up = 0;
                                    down = -(r * 2);
                                }
                                case Down -> {
                                    up = -1 + r * 2;
                                    down = -1;
                                }
                                case Normal -> {
                                    up = -0.5 + r;
                                    down = -0.5 - r;
                                    width = r;
                                }
                            }
                            Box box = new Box(renderPos.getX() + 0.5 - width, renderPos.getY() + down, renderPos.getZ() + 0.5 - width,
                                renderPos.getX() + 0.5 + width, renderPos.getY() + up, renderPos.getZ() + 0.5 + width);

                            event.renderer.box(box, new Color(color.get().r, color.get().g, color.get().b, color.get().a), lineColor.get(), shapeMode.get(), 0);
                        }
                    }
                }
                case MotionOut -> {
                    if (placePos != null && !isPaused() && holdingCheck() && !isAnchor()) {
                        renderProgress = Math.min(1, renderProgress + delta);
                        renderTarget = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ());
                        renderProgress = fadeTime.get() + renderTime.get();
                    } else {
                        renderProgress = Math.max(0, renderProgress - delta);
                    }

                    if (renderTarget != null) {
                        renderPos = smoothMove(renderPos, renderTarget, delta * animationSpeed.get() * 5);
                    }

                    if (renderPos != null) {
                        double r = 0.5 - Math.pow(1 - renderProgress, HyperionExponent.get()) / 2f;
                        if (renderProgress > 0 && renderPos != null) {
                            event.renderer.box(new Box(renderPos.getX(), renderPos.getY() - 1, renderPos.getZ(),
                                    renderPos.getX() + 1, renderPos.getY(), renderPos.getZ() + 1),
                                new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, renderProgress / fadeTime.get()))),
                                new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, renderProgress / fadeTime.get()))), shapeMode.get(), 0);
                        }


                        if (r >= 0.001) {
                            double down = -0.5;
                            double up = -0.5;
                            double width = 0.5;
                            switch (MotionOutFadeMode.get()) {
                                case None -> {
                                    return;
                                    }
                                case blockbox -> {
                                    up = -0.8 + r;
                                    down = -0.2 - r;
                                    width = r;
                                }
                            }

                            Box box = new Box(renderPos.getX() + 0.5 - width, renderPos.getY() + down, renderPos.getZ() + 0.5 - width,
                                renderPos.getX() + 0.5 + width, renderPos.getY() + up, renderPos.getZ() + 0.5 + width);

                            event.renderer.box(box, new Color(color.get().r, color.get().g, color.get().b, color.get().a), lineColor.get(), shapeMode.get(), 0);
                        }
                    }
                }
                case Future -> {
                    if (placePos != null && !isPaused() && holdingCheck() && !isAnchor()) {
                        renderPos = new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ());
                        renderProgress = fadeTime.get() + renderTime.get();
                    } else {
                        renderProgress = Math.max(0, renderProgress - delta);
                    }

                    if (renderProgress > 0 && renderPos != null) {
                        event.renderer.box(new Box(renderPos.getX(), renderPos.getY() - 1, renderPos.getZ(),
                                renderPos.getX() + 1, renderPos.getY(), renderPos.getZ() + 1),
                            new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, renderProgress / fadeTime.get()))),
                            new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, renderProgress / fadeTime.get()))), shapeMode.get(), 0);
                    }
                }
                // some shitt bug don't use this RenderMode
                case Earthhack -> {
                    List<BlockPos> toRemove = new ArrayList<>();
                    for (Map.Entry<BlockPos, Double[]> entry : earthMap.entrySet()) {
                        BlockPos pos = entry.getKey();
                        Double[] alpha = entry.getValue();
                        if (alpha[0] <= delta) {
                            toRemove.add(pos);
                        } else {
                            double r = Math.min(1, alpha[0] / alpha[1]) / 2f;
                            double down = -0.5;
                            double up = -0.5;
                            double width = 0.5;

                            switch (earthFadeMode.get()) {
                                case Normal -> {
                                    up = 1;
                                    down = 0;
                                }
                                case Up -> {
                                    up = 1;
                                    down = 1 - (r * 2);
                                }
                                case Down -> {
                                    up = r * 2;
                                    down = 0;
                                }
                                case Shrink -> {
                                    up = 0.5 + r;
                                    down = 0.5 - r;
                                    width = r;
                                }
                            }

                            Box box = new Box(pos.getX() + 0.5 - width, pos.getY() + down, pos.getZ() + 0.5 - width,
                                pos.getX() + 0.5 + width, pos.getY() + up, pos.getZ() + 0.5 + width);

                            event.renderer.box(box,
                                new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, alpha[0] / alpha[1]))),
                                new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, alpha[0] / alpha[1]))), shapeMode.get(), 0);
                            entry.setValue(new Double[]{alpha[0] - delta, alpha[1]});
                        }
                    }
                    toRemove.forEach(earthMap::remove);
                }
            }
        }

        if (mc.player != null) {
            //Render extrapolation
            if (renderExt.get()) {
                extPos.forEach((name, bb) -> {
                    if (renderSelfExt.get() || !name.equals(mc.player))
                        event.renderer.box(bb, color.get(), lineColor.get(), shapeMode.get(), 0);
                });
            }
        }
    }
    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (renderDmg.get() && !isPaused() && !isAnchor()  && holdingCheck() && placePos != null && renderPos != null) {
            renderMode.get();
            Vector3d vec3d = new Vector3d(renderPos.x + 0.5, renderPos.add(0.0, -1.2, 0.0).y + 0.5, renderPos.z + 0.5);
            if (NametagUtils.to2D(vec3d, scale.get(), true)) {
                TextRenderer font = TextRenderer.get();

                NametagUtils.begin(vec3d);
                font.begin(scale.get());

                NumberFormat why = NumberFormat.getNumberInstance();
                why.setMaximumFractionDigits(decimal.get());
                String enemy = why.format(getDmg(placePos.toCenterPos(), false)[0][0]);

                font.render(enemy, -(font.getWidth(enemy) / 2.0), -font.getHeight(), damageColor.get(), false);

                font.end();
                NametagUtils.end();
            }
        }
    }


    private boolean isAnchor() {
        return OnAnchorPlacePause.get() && autoAnchor.Exploding();
    }
    public boolean isPaused() {
        return pauseEat.get() && mc.player.isUsingItem();

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntity(EntityAddedEvent event) {
        confirmed = event.entity.getId();

        if (event.entity.getBlockPos().equals(placePos)) explosions.add(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSend(PacketEvent.Send event) {
        if (mc.player != null && mc.world != null) {
            if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
                switchTimer = CoolDown.get();
            }

            if (event.packet instanceof PlayerInteractBlockC2SPacket packet) {

                if (!(packet.getHand() == Hand.MAIN_HAND ? Managers.HOLDING.isHolding(Items.END_CRYSTAL) : mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL))
                    return;

                if (isOwn(packet.getBlockHitResult().getBlockPos().up())) own.remove(packet.getBlockHitResult().getBlockPos().up());

                own.put(packet.getBlockHitResult().getBlockPos().up(), System.currentTimeMillis());
                blocked.add(OLEPOSSUtils.getCrystalBox(packet.getBlockHitResult().getBlockPos().up()));
                addExisted(packet.getBlockHitResult().getBlockPos().up());
            }
        }
    }

    // Other stuff
    private void update() {
        placing = false;
        expEntity = null;

        Hand hand = getHand(stack -> stack.getItem() == Items.END_CRYSTAL);

        Hand handToUse = hand;
        if (!performance.get()) updatePlacement();

        switch (switchMode.get()) {
            case Simple -> {
                int slot = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
                if (placePos != null && hand == null && slot >= 0) {
                    InvUtils.swap(slot, false);
                    handToUse = Hand.MAIN_HAND;
                }
            }
            case Gapple -> {
                int gapSlot = InvUtils.findInHotbar(OLEPOSSUtils::isGapple).slot();
                if (mc.options.useKey.isPressed() && Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE) && gapSlot >= 0) {
                    if (getHand(OLEPOSSUtils::isGapple) == null)
                        InvUtils.swap(gapSlot, false);
                    handToUse = getHand(itemStack -> itemStack.getItem() == Items.END_CRYSTAL);
                } else if (Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)) {
                    int slot = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
                    if (placePos != null && hand == null && slot >= 0) {
                        InvUtils.swap(slot, false);
                        handToUse = Hand.MAIN_HAND;
                    }
                }
            }
        }

        if (placePos != null && placeDir != null) {
            if (!isPaused()  && !isAnchor() && (!paPlace.get() || !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(PistonCrystal.class))) {
                int silentSlot = InvUtils.find(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).slot();
                int hotbar = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
                if (handToUse != null || (switchMode.get() == SwitchMode.Silent && hotbar >= 0) || ((switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSilent) && silentSlot >= 0)) {
                    placing = true;
                    if (!SettingUtils.shouldRotate(RotationType.Interact) || Managers.ROTATION.start(placePos.down(), smartRot.get() ? new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5) : null, priority, RotationType.Interact, Objects.hash(name + "placing"))) {
                        if (speedCheck() && delayCheck())
                            placeCrystal(placePos.down(), placeDir, handToUse, silentSlot, hotbar);
                    }
                }
            }
        }

        PistonCrystal pa = meteordevelopment.meteorclient.systems.modules.Modules.get().get(PistonCrystal.class);
        double[] value = null;

        if (!isPaused() && !isAnchor() && (hand != null || switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSilent) && Break.get()) {
            for (Entity en : mc.world.getEntities()) {

                if (paAttack.get() && pa.isActive() && en.getBlockPos().equals(pa.crystalPos)) continue;
                if (!(en instanceof EndCrystalEntity)) continue;
                if (switchTimer > 0) continue;

                double[] dmg = getDmg(en.getPos(), true)[0];

                if (!canExplode(en.getPos())) continue;

                if ((expEntity == null || value == null) || ((calcMode.get().equals(calc.HyperCard) && dmg[0] > value[0]) || (calcMode.get().equals(calc.earthhack) && dmg[2] / dmg[0] < value[2] / dmg[0]))) {
                    expEntity = en;
                    value = dmg;
                }
            }
        }

        if (expEntity != null) {
            if (multiTaskCheck() && !isAttacked(expEntity.getId()) && attackDelayCheck() && existedCheck(expEntity.getBlockPos())) {
                if (!SettingUtils.shouldRotate(RotationType.Attacking) || startAttackRot()) {
                    if (SettingUtils.shouldRotate(RotationType.Attacking)) expEntityBB = expEntity.getBoundingBox();
                    explode(expEntity.getId(), expEntity.getPos());
                }
            }
        }

        if (!isAlive(expEntityBB) && SettingUtils.shouldRotate(RotationType.Attacking))
            Managers.ROTATION.end(Objects.hash(name + "attacking"));
    }
    private boolean attackDelayCheck() {
        if (instantAttack.get())
            return expSpeedLimit.get() <= 0 || System.currentTimeMillis() > lastAttack + 1000 / expSpeedLimit.get();
        else
            return System.currentTimeMillis() > lastAttack + 1000 / expSpeed.get();
    }

    private boolean startAttackRot() {
        expEntityBB = expEntity.getBoundingBox();
        return (Managers.ROTATION.start(expEntity.getBoundingBox(), smartRot.get() ? expEntity.getPos() : null, priority + (!isAttacked(expEntity.getId()) && blocksPlacePos(expEntity) ? -0.1 : 0.1), RotationType.Attacking, Objects.hash(name + "attacking")));
    }

    private boolean blocksPlacePos(Entity entity) {
        return placePos != null && entity.getBoundingBox().intersects(new Box(placePos.getX(), placePos.getY(), placePos.getZ(), placePos.getX() + 1, placePos.getY() + (SettingUtils.cc() ? 1 : 2), placePos.getZ() + 1));
    }

    private boolean speedCheck() {

        if (speedLimit.get() > 0 && placeLimitTimer < 1 / speedLimit.get())
            return false;

        if (instantPlace.get() && !shouldSlow() && !isBlocked(placePos))
            return true;

        return placeTimer <= 0;
    }

    private boolean holdingCheck() {
        return switch (switchMode.get()) {
            case Silent -> InvUtils.findInHotbar(Items.END_CRYSTAL).slot() >= 0;
            case PickSilent, InvSilent -> InvUtils.find(Items.END_CRYSTAL).slot() >= 0;
            default -> getHand(itemStack -> itemStack.getItem() == Items.END_CRYSTAL) != null;
        };
    }

    private void updatePlacement() {
        if (!place.get()) {
            if (!preplacecalc.get()) placed = Integer.parseInt(null);
            if (!preplacepos.get()) placePos = null;
            if (!preplacedir.get()) placeDir = null;
            rangePos = null;

            return;
        }
        placePos = getPlacePos();
    }

    private void placeCrystal(BlockPos pos, Direction dir, Hand handToUse, int sl, int hsl) {
        if (pos != null && mc.player != null) {
            if (renderMode.get().equals(RenderMode.Earthhack)) {
                if (!earthMap.containsKey(pos))
                    earthMap.put(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
                else
                    earthMap.replace(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
            }

            blocked.add(new Box(pos.getX() - 0.5, pos.getY() + 1, pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5));

            boolean switched = handToUse == null;
            if (switched) {
                switch (switchMode.get()) {
                    case PickSilent -> BOInvUtils.pickSwitch(sl);
                    case Silent -> InvUtils.swap(hsl, true);
                    case InvSilent -> BOInvUtils.invSwitch(sl);
                }
            }

            addExisted(pos.up());

            if (!isOwn(pos.up())) own.put(pos.up(), System.currentTimeMillis());
            else {
                own.remove(pos.up());
                own.put(pos.up(), System.currentTimeMillis());
            }

            placeLimitTimer = 0;
            placeTimer = 1;
            placed = 0;

            interactBlock(switched ? Hand.MAIN_HAND : handToUse, pos.toCenterPos(), dir, pos);

            if (placeSwing.get()) clientSwing(placeHand.get(), switched ? Hand.MAIN_HAND : handToUse);

            if (SettingUtils.shouldRotate(RotationType.Interact))
                Managers.ROTATION.end(Objects.hash(name + "placing"));

            if (switched) {
                switch (switchMode.get()) {
                    case PickSilent -> BOInvUtils.pickSwapBack();
                    case Silent -> InvUtils.swapBack();
                    case InvSilent -> BOInvUtils.swapBack();
                }
            }
            if (idPredict.get()) {
                int highest = getHighest();

                int id = highest + idStartOffset.get();
                for (int i = 0; i < idPackets.get() * idOffset.get(); i += idOffset.get()) {
                    addPredict(id + i, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), idDelay.get() + idPacketDelay.get() * i);
                }
            }
        }
    }

    private boolean delayCheck() {
        if (placeDelayMode.get() == DelayMode.Seconds)
            return delayTimer >= placeDelay.get();
        return delayTicks >= placeDelayTicks.get();
    }

    private boolean multiTaskCheck() {
        return placed >= sequential.get().ticks;
    }

    private int getHighest() {
        int highest = confirmed;
        for (Entity entity : mc.world.getEntities()) {
            if (entity.getId() > highest) highest = entity.getId();
        }
        if (highest > confirmed) confirmed = highest;
        return highest;
    }

    private boolean isBlocked(BlockPos pos) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        for (Box bb : blocked) {
            if (bb.intersects(box)) return true;
        }
        return false;
    }

    private boolean isAttacked(int id) {
        return attackedList.contains(id);
    }

    private void explode(int id, Vec3d vec) {
        attackEntity(id, OLEPOSSUtils.getCrystalBox(vec), vec);
    }

    private void attackEntity(int id, Box bb, Vec3d vec) {
        if (mc.player != null) {
            lastAttack = System.currentTimeMillis();
            attackedList.add(id, 1 / expSpeed.get());

            delayTimer = 0;
            delayTicks = 0;

            removeExisted(BlockPos.ofFloored(vec));

            SettingUtils.registerAttack(bb);
            PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(mc.player, mc.player.isSneaking());
            ((IInteractEntityC2SPacket) packet).setId(id);

            SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);

            sendPacket(packet);

            SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
            if (attackSwing.get()) clientSwing(attackHand.get(), Hand.MAIN_HAND);

            blocked.clear();
            if (FastDead.get()) {
                Entity entity = mc.world.getEntityById(id);
                if (entity == null) return;
                addSetDead(entity);
            }
        }
    }

    private boolean existedCheck(BlockPos pos) {
        if (existedMode.get() == DelayMode.Seconds)
            return !existedList.containsKey(pos) || System.currentTimeMillis() > existedList.get(pos) + existed.get() * 1000;
        else
            return !existedTicksList.containsKey(pos) || ticksEnabled >= existedTicksList.get(pos) + existedTicks.get();
    }

    private void addExisted(BlockPos pos) {
        if (existedMode.get() == DelayMode.Seconds) {
            if (!existedList.containsKey(pos)) existedList.put(pos, System.currentTimeMillis());
        } else {
            if (!existedTicksList.containsKey(pos)) existedTicksList.put(pos, ticksEnabled);
        }
    }

    private void removeExisted(BlockPos pos) {
        if (existedMode.get() == DelayMode.Seconds) existedList.remove(pos);
        else existedTicksList.remove(pos);
    }

    private boolean canExplode(Vec3d vec) {
        if (onlyOwn.get() && !isOwn(vec)) return false;
        if (!inExplodeRange(vec)) return false;

        double[][] result = getDmg(vec, true);
        return explodeDamageCheck(result[0], result[1], isOwn(vec));
    }

    private boolean canExplodePlacing(Vec3d vec) {
        if (onlyOwn.get() && !isOwn(vec)) return false;
        if (!inExplodeRangePlacing(vec)) return false;

        double[][] result = getDmg(vec, false);
        return explodeDamageCheck(result[0], result[1], isOwn(vec));
    }



    private void setEntityDead(Entity en) {
        mc.world.removeEntity(en.getId(), Entity.RemovalReason.KILLED);
    }

    private BlockPos getPlacePos() {

        int r = (int) Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));
        //Used in placement calculation
        BlockPos bestPos = null;
        Direction bestDir = null;
        double[] highest = null;

        BlockPos pPos = BlockPos.ofFloored(mc.player.getEyePos());

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = pPos.add(x, y, z);
                    // Checks if crystal can be placed
                    if (!air(pos) || !(!SettingUtils.oldCrystals() || air(pos.up())) || !crystalBlock(pos.down()) || blockBroken(pos.down())) continue;

                    // Checks if there is possible placing direction
                    Direction dir = SettingUtils.getPlaceOnDirection(pos.down());
                    if (dir == null) continue;

                    // Checks if the placement is in range
                    if (!inPlaceRange(pos.down()) || !inExplodeRangePlacing(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) continue;

                    // Calculates damages and healths
                    double[][] result = getDmg(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), false);

                    // Checks if damages are valid
                    if (!placeDamageCheck(result[0], result[1], highest)) continue;

                    // Checks if placement is blocked by other entities (other than players)
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + (SettingUtils.cc() ? 1 : 2), pos.getZ() + 1);

                    if (BOEntityUtils.intersectsWithEntity(box, this::validForIntersect, extHitbox)) continue;

                    // Sets best pos to calculated one
                    bestDir = dir;
                    bestPos = pos;
                    highest = result[0];
                }
            }
        }

        placeDir = bestDir;
        return bestPos;
    }

    private boolean placeDamageCheck(double[] dmg, double[] health, double[] highest) {
        //  0 = enemy, 1 = friend, 2 = self

        //  Dmg Check
        if (highest != null) {
            if (calcMode.get().equals(calc.Normal) && dmg[2] / dmg[0] > highest[0]) return false;
            if (calcMode.get().equals(calc.HyperCard) && dmg[0] < highest[0]) return false;
            if (calcMode.get().equals(calc.earthhack) && dmg[2] / dmg[0] > highest[2] / highest[0]) return false;
        }


        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        if (playerHP >= 0 && dmg[2] * selfCheck.get() >= playerHP) return false;
        if (health[0] >= 0 && dmg[0] * Desyncforce.get() >= health[0]) return true;

        //  Min Damage
        if (dmg[0] < MinDmg.get()) return false;

        //  Max Damage
        if (dmg[1] > maxFriendPlace.get()) return false;

        if (dmg[1] >= 0 && dmg[0] / dmg[1] < minFriendPlaceRatio.get()) return false;
        if (dmg[2] > maxPlace.get()) return false;
        return dmg[2] < 0 || dmg[0] / dmg[2] >= minPlaceRatio.get();
    }
    private boolean explodeDamageCheck(double[] dmg, double[] health, boolean own) {
        //enemy
        boolean checkOwn = expMode.get() == ExplodeMode.Crystal
            || expMode.get() == ExplodeMode.Calc;

        //self+enemy
        boolean checkDmg = expMode.get() == ExplodeMode.Crystal
            || (expMode.get() == ExplodeMode.Calc && !own);

        //  0 = enemy, 1(friend), 2 = self

        double playerHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (checkOwn) {
            if (playerHP >= 0 && dmg[0] * Desyncforce.get() >= playerHP) return true;
        }

        if (checkDmg) {
            //health = 3 ---- Desyncforce = set(1)
            if (health[0] >= 0 && dmg[0] * Desyncforce.get() >= health[0]) return true;
            if (dmg[0] < minExplode.get()) return false;
             //0 emeny 1 friend 2 self
            if (dmg[1] >= 0 && dmg[0] / dmg[1] < minFriendExpRatio.get()) return false;
                /*slef*/
            if (dmg[2] >= 0 && dmg[0] / dmg[2] < minExpRatio.get()) return false;
        }

        if (checkOwn) {
            if (dmg[1] > maxFriendExp.get()) return false;
            return dmg[2] <= maxExp.get();
        }
        return true;
    }

    private boolean isOwn(Vec3d vec) {
        return isOwn(BlockPos.ofFloored(vec));
    }

    private boolean isOwn(BlockPos pos) {
        for (Map.Entry<BlockPos, Long> entry : own.entrySet()) {
            if (entry.getKey().equals(pos)) return true;
        }
        return false;
    }

    private double[][] getDmg(Vec3d vec, boolean attack) {
        double self = BODamageUtils.crystal(mc.player, extPos.containsKey(mc.player) ? extPos.get(mc.player) : mc.player.getBoundingBox(), vec, ignorePos(attack), ignoreTerrain.get());

        if (suicide) return new double[][]{new double[]{self, -1, -1}, new double[]{20, 20}};

        double highestEnemy = -1;
        double highestFriend = -1;
        double enemyHP = -1;
        double friendHP = -1;
        for (Map.Entry<AbstractClientPlayerEntity, Box> entry : extPos.entrySet()) {
            AbstractClientPlayerEntity player = entry.getKey();
            Box box = entry.getValue();
            if (player.getHealth() <= 0 || player == mc.player) continue;

            double dmg = BODamageUtils.crystal(player, box, vec, ignorePos(attack), ignoreTerrain.get());
            if (BlockPos.ofFloored(vec).down().equals(autoMine.targetPos()))
                dmg *= autoMineDamage.get();
            double hp = player.getHealth() + player.getAbsorptionAmount();

            //  friend
            if (Friends.get().isFriend(player)) {
                if (dmg > highestFriend) {
                    highestFriend = dmg;
                    friendHP = hp;
                }
            }
            //  enemy
            else if (dmg > highestEnemy) {
                highestEnemy = dmg;
                enemyHP = hp;
                bestTarget = player;
            }
        }

        return new double[][]{new double[]{highestEnemy, highestFriend, self}, new double[]{enemyHP, friendHP}};
    }



    private boolean inExplodeRangePlacing(Vec3d vec) {
        return SettingUtils.inAttackRange(new Box(vec.getX() - 1, vec.getY(), vec.getZ() - 1, vec.getX() + 1, vec.getY() + 2, vec.getZ() + 1), rangePos != null ? rangePos : null);
    }

    private boolean inExplodeRange(Vec3d vec) {
        return SettingUtils.inAttackRange(new Box(vec.getX() - 1, vec.getY(), vec.getZ() - 1, vec.getX() + 1, vec.getY() + 2, vec.getZ() + 1));
    }

    private double getSpeed() {
        return shouldSlow() ? slowSpeed.get() : placeSpeed.get();
    }

    private boolean shouldSlow() {
        return placePos != null && getDmg(new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5), false)[0][0] <= slowDamage.get();
    }

    private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
        if (current == null) return target;

        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);

        double x = (absX + Math.pow(absX, animationMoveExponent.get() - 1)) * delta;
        double y = (absX + Math.pow(absY, animationMoveExponent.get() - 1)) * delta;
        double z = (absX + Math.pow(absZ, animationMoveExponent.get() - 1)) * delta;

        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x),
            current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y),
            current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    private boolean validForIntersect(Entity entity) {
        if (entity instanceof EndCrystalEntity && canExplodePlacing(entity.getPos()))
            return false;

        return !(entity instanceof PlayerEntity) || !entity.isSpectator();
    }

    private BlockPos ignorePos(boolean attack) {
        if (!amPlace.get()) return null;
        if (!amSpam.get() && attack) return null;
        if (autoMine == null || !autoMine.isActive()) return null;
        if (autoMine.targetPos() == null) return null;

        return autoMine.getMineProgress() > amProgress.get() ? autoMine.targetPos() : null;
    }

    private boolean blockBroken(BlockPos pos) {
        if (!amPlace.get()) return false;

        if (autoMine == null || !autoMine.isActive()) return false;
        if (autoMine.targetPos() == null) return false;
        if (!autoMine.targetPos().equals(pos)) return false;

        double progress = autoMine.getMineProgress();

        if (progress >= 1 && !amBroken.get().broken) return true;
        if (progress >= amProgress.get() && !amBroken.get().near) return true;
        return progress < amProgress.get() && !amBroken.get().normal;
    }

    private void addPredict(int id, Vec3d pos, double delay) {
        predicts.add(new Predict(id, pos, Math.round(System.currentTimeMillis() + delay * 1000)));
    }

    private void addSetDead(Entity entity) {
        setDeads.add(new SetDead(entity, Math.round(System.currentTimeMillis())));
    }

    private void checkDelayed() {
        List<Predict> toRemove = new ArrayList<>();
        for (Predict p : predicts) {
            if (System.currentTimeMillis() >= p.time) {
                explode(p.id, p.pos);
                toRemove.add(p);
            }
        }
        toRemove.forEach(predicts::remove);

        List<SetDead> toRemove2 = new ArrayList<>();
        for (SetDead p : setDeads) {
            if (!PauseDead.get())
            if (System.currentTimeMillis() >= p.time) {
                setEntityDead(p.entity);
                toRemove2.add(p);
            }
        }
        toRemove2.forEach(setDeads::remove);
    }
    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (MixinPlugin.isSodiumPresent) return theme.label("WARING:maxselfdmg fails when deyncforce is set higher than 1. ");
        return null;
    }

    private record Predict(int id, Vec3d pos, long time) {
    }

    private record SetDead(Entity entity, long time) {
    }
}
