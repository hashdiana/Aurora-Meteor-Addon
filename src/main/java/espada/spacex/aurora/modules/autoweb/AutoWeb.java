package espada.spacex.aurora.modules.autoweb;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.modules.maojunqing.MaoJunQingAura;
import espada.spacex.aurora.utils.*;
import espada.spacex.aurora.utils.Timer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.*;

public class AutoWeb extends Modules {
    private final List<Render> renderBlocks = new ArrayList<>();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgPredict = settings.createGroup("Predict");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<Boolean> down = sgGeneral.add(new BoolSetting.Builder()
        .name("Down")
        .description("1")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> feet = sgGeneral.add(new BoolSetting.Builder()
        .name("Feet")
        .description("ji ao.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> face = this.sgGeneral.add(new BoolSetting.Builder()
        .name("Face")
        .description("tou.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> surCheck = this.sgGeneral.add(new IntSetting.Builder()
        .name("PeetPlace Surround Check")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Integer> surCheck2 = this.sgGeneral.add(new IntSetting.Builder()
        .name("FacePlace Surround Check")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Double> minSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("target min speed").description("ddd.").defaultValue(2.0D).range(0.0D, 5.0D).sliderMax(5.0D).build());
    private final Setting<Boolean> onlyGround = this.sgGeneral.add(new BoolSetting.Builder().name("Only Ground").description("Pauses when you are fffffff.").defaultValue(false).build());
    private final Setting<Boolean> pauseEat = this.sgGeneral.add(new BoolSetting.Builder().name("Pause Eat").description("Pauses when you are eating.").defaultValue(true).build());
    private final Setting<Boolean> CheckMine = this.sgGeneral.add(new BoolSetting.Builder()
        .name("CheckMine")
        .description("11")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> CheckSelf = this.sgGeneral.add(new BoolSetting.Builder()
        .name("CheckSelf")
        .description("11")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> CheckFriend = this.sgGeneral.add(new BoolSetting.Builder()
        .name("CheckFriend")
        .description("11")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> OnAnchorPlacePause = sgGeneral.add(new BoolSetting.Builder()
        .name("On AnchorPlace Pause")
        .description("Pause.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> range = this.sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The maximum distance to target players.")
        .defaultValue(3.5D)
        .range(0.0D, 8.0D)
        .sliderMax(8.0D)
        .build()
    );
    private final Setting<Integer> multiPlace = this.sgGeneral.add(new IntSetting.Builder()
        .name("MultiPlace")
        .defaultValue(5)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<Integer> delay = this.sgGeneral.add(new IntSetting.Builder()
        .name("FeetPlace Delay")
        .defaultValue(35)
        .min(0)
        .sliderRange(0, 2000)
        .build()
    );

    private final Setting<Integer> delay2 = this.sgGeneral.add(new IntSetting.Builder()
        .name("FacePlace Delay")
        .defaultValue(35)
        .min(0)
        .sliderRange(0, 2000)
        .build());
    private final Setting<Integer> delay3 = this.sgGeneral.add(new IntSetting.Builder()
        .name("DownPlace Delay")
        .defaultValue(35)
        .min(0)
        .sliderRange(0, 2000)
        .build()
    );
    private List<PlayerEntity> targets = new ArrayList();

    //--------------------Predict--------------------//
    private final Setting<Boolean> prediction = sgPredict.add(new BoolSetting.Builder()
        .name("Prediction")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> smooth = sgPredict.add(new BoolSetting.Builder()
        .name("Smooth")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> tick = sgPredict.add(new IntSetting.Builder()
        .name("Prediction Tick")
        .defaultValue(1)
        .sliderRange(0, 10)
        .visible(prediction::get)
        .build()
    );
    private final Setting<Integer> selfExt = sgPredict.add(new IntSetting.Builder()
        .name("Self Extrapolation")
        .description("How many ticks of movement should be predicted for self damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extrapolation = sgPredict.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("How many ticks of movement should be predicted for enemy damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgPredict.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description("How many earlier ticks should be used in average calculation for extrapolation motion.")
        .defaultValue(2)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description("Renders swing animation when placing a block.")
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
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of the boxes should be rendered.")
        .defaultValue(ShapeMode.Sides)
        .visible(render::get)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(Aurora.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Lines) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(Aurora.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 20))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Sides) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Map<AbstractClientPlayerEntity, Box> extMap = new HashMap<>();
    private int progress = 0;
    private final Timer timer = new Timer();
    private MaoJunQingAura autoAnchor = meteordevelopment.meteorclient.systems.modules.Modules.get().get(MaoJunQingAura.class);

    public AutoWeb() {
        super(Aurora.Extendcombat, "AutoWeb", "Automatically places webs on other players.");
        MeteorClient.EVENT_BUS.subscribe(new Renderer());
    }

    @EventHandler(
        priority = 200
    )
    private void onTickPre(TickEvent.Post event) {
        this.updateTargets();
    }

    public String getInfoString() {
        for (PlayerEntity target : this.targets) {
            if (target != null) {
                return target.getGameProfile().getName();
            }
        }

        return null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.onlyGround.get() || this.mc.player.isOnGround()) {
            if (this.pauseCheck()) {
                if (BOInvUtils.findHotbarBlock(Blocks.COBWEB) != -1) {
                    this.progress = 0;
                    if(isAnchor()) return;
                    for (PlayerEntity target : this.targets) {
                        LinkedHashSet<BlockPos> set = new LinkedHashSet();
                        if (this.down.get() && this.timer.passedMs(this.delay3.get())) {
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0, -1, 0))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, -1, 0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, -1, 0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, -1, -0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(+0.2, -1, -0.2))));
                        }
                        if (this.face.get() && this.timer.passedMs(this.delay2.get())) {
                            if (this.surCheck2(target)) {
                                return;
                            }
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 1, 0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 1, 0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 1, -0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 1, -0.2))));
                        }

                        if (this.feet.get() && this.timer.passedMs(this.delay.get())) {
                            if ((double) target.speed < this.minSpeed.get() || this.surCheck(target)) {
                                return;
                            }
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, 0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, 0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, -0.2))));
                            set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, -0.2))));
                        }
                        List<BlockPos> collect = set.stream().filter(BOBlockUtil::isAir).filter((p) -> !BOBlockUtil.cantBlockPlace(p)).filter((p) -> !(Managers.BREAK.isMine(p, false) && CheckMine.get())).filter((p) -> !isSelf(p)).filter((p) -> !isFriend(p)).limit(1L).toList();
                        this.placeWeb(collect.isEmpty() ? null : collect.get(0));
                    }

                }
            }
        }
    }

    private boolean isAnchor() {
        return OnAnchorPlacePause.get() && autoAnchor.Exploding();
    }

    public boolean surCheck(PlayerEntity player) {
        int n = 0;
        BlockPos pos = player.getBlockPos();
        if (!BOBlockUtil.isAir(pos.add(0, 0, 1))) {
            ++n;
        }

        if (!BOBlockUtil.isAir(pos.add(0, 0, -1))) {
            ++n;
        }

        if (!BOBlockUtil.isAir(pos.add(1, 0, 0))) {
            ++n;
        }

        if (!BOBlockUtil.isAir(pos.add(-1, 0, 0))) {
            ++n;
        }

        return n > this.surCheck.get();
    }

    public boolean surCheck2(PlayerEntity player) {
        int n = 0;
        BlockPos pos = player.getBlockPos();
        if (!BOBlockUtil.isAir(pos.add(0, 0, 1))) {
            ++n;
        }

        if (!BOBlockUtil.isAir(pos.add(0, 0, -1))) {
            ++n;
        }

        if (!BOBlockUtil.isAir(pos.add(1, 0, 0))) {
            ++n;
        }

        if (!BOBlockUtil.isAir(pos.add(-1, 0, 0))) {
            ++n;
        }

        return n > this.surCheck2.get();
    }


    private void updateTargets() {
        List<PlayerEntity> players = new ArrayList();
        double closestDist = 1000.0D;

        for (int i = 3; i > 0; --i) {
            PlayerEntity closest = null;

            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (!players.contains(player) && !Friends.get().isFriend(player) && player != this.mc.player && !player.isDead()) {
                    double dist = player.distanceTo(this.mc.player);
                    if (!(dist > this.range.get()) && !this.surCheck(player) && (closest == null || dist < closestDist)) {
                        closestDist = dist;
                        closest = player;
                    }
                }
            }

            if (closest != null) {
                players.add(closest);
            }
        }
        ExtrapolationUtils.extrapolateMap(extMap, player -> player == mc.player ? selfExt.get() : extrapolation.get(), player -> extSmoothness.get());
        this.targets = players;
    }

    private void placeWeb(BlockPos pos) {
        PlaceData data = SettingUtils.getPlaceData(pos);
        if (data.valid()) {
            if (this.progress < this.multiPlace.get()) {
                if (this.mc.world.isAir(pos) && SettingUtils.inPlaceRange(pos)) {
                    if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), this.priority, RotationType.BlockPlace, Objects.hash(this.name + "placing"))) {
                        int Old = mc.player.getInventory().selectedSlot;
                        BOInvUtils.doSwap(BOInvUtils.findHotbarBlock(Blocks.COBWEB));
                        renderBlocks.add(new Render(pos, System.currentTimeMillis()));
                        BOBlockUtil.placeBlock(pos, Hand.MAIN_HAND, false, 1);
                        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);
                        BOInvUtils.doSwap(Old);
                        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                            Managers.ROTATION.end(Objects.hash(this.name + "placing"));
                        }
                        ++this.progress;
                        this.timer.reset();
                    }
                }
            }
        }
    }


    private boolean isSelf(BlockPos pos) {
        if (!this.CheckSelf.get()) {
            return false;
        }
        for (Entity entity : Util.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity != Util.mc.player) continue;
            return true;
        }
        return false;
    }

    private boolean isFriend(BlockPos pos) {
        if (!this.CheckFriend.get()) {
            return false;
        }
        for (PlayerEntity entity : Util.mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
            if (!Friends.get().isFriend(entity)) continue;
            return true;
        }
        return false;
    }


    private boolean pauseCheck() {
        return !this.pauseEat.get() || !this.mc.player.isUsingItem();
    }


    public record Render(BlockPos blockPos, long time) {
    }
    private class Renderer {
        @EventHandler
        private void onRender(Render3DEvent event) {
            if (!render.get()) return;

            renderBlocks.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderBlocks.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

                event.renderer.box(r.blockPos, RenderUtils.injectAlpha(sideColor.get(), (int) Math.round(sideColor.get().a * progress)), RenderUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
            });
        }
    }
}
