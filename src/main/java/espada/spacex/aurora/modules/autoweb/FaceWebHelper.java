package espada.spacex.aurora.modules.autoweb;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.utils.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class FaceWebHelper extends Modules {
    private final List<Render> renderBlocks = new ArrayList<>();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<Boolean> face = this.sgGeneral.add(new BoolSetting.Builder()
        .name("Face")
        .description("tou.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> surCheck = this.sgGeneral.add(new  IntSetting.Builder()
        .name( "Surround Check")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 5)
        .build());
    private final Setting<Double> minSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("target min speed").description("ddd.").defaultValue(2.0D).range(0.0D, 5.0D).sliderMax(5.0D).build());
    private final Setting<Boolean> onlyGround = this.sgGeneral.add(new  BoolSetting.Builder().name("Only Ground").description("Pauses when you are fffffff.").defaultValue(false).build());
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
    private final Setting<Double> range = this.sgGeneral.add(new DoubleSetting.Builder().name("target-range").description("The maximum distance to target players.").defaultValue(3.5D).range(0.0D, 8.0D).sliderMax(8.0D).build());
    private final Setting<Integer> multiPlace = this.sgGeneral.add(new IntSetting.Builder().name("MultiPlace").defaultValue(5).min(1).sliderRange(1, 5).build());
    private final Setting<Integer> delay = this.sgGeneral.add(new IntSetting.Builder().name("Delay").defaultValue(35).min(0).sliderRange(0, 2000).build());
    private List<PlayerEntity> targets = new ArrayList();

    //--------------------Render--------------------//
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

    private int progress = 0;
    private final Timer timer = new Timer();

    public FaceWebHelper() {
        super(Aurora.AURORA, "FaceWeb", "FaceWeb with autoweb.");
        MeteorClient.EVENT_BUS.subscribe(new Renderer());
    }

    @EventHandler(
        priority = 200
    )
    private void onTickPre(TickEvent.Post event) {
        this.updateTargets();
    }

    public String getInfoString() {
        for(PlayerEntity target : this.targets) {
            if (target != null) {
                return target.getGameProfile().getName();
            }
        }

        return null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.timer.passedMs((long)((Integer)this.delay.get()).intValue())) {
            if (!this.onlyGround.get() || this.mc.player.isOnGround()) {
                if (this.pauseCheck()) {
                    if (InvUtils.find(Items.COBWEB).found()) {
                        this.progress = 0;

                        for(PlayerEntity target : this.targets) {
                            if (this.face.get()) {
                                this.placeWeb(this.getPlaceBlock(target, 1.0D));
                            }

                            if (this.face.get()) {
                                if ((double)target.speed < this.minSpeed.get() || this.surCheck(target)) {
                                    return;
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private static boolean isWeb(BlockPos pos) {
        if(Util.mc.world == null || Util.mc.player == null || pos == null){
            return false;
        }
        return Util.mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB || Util.mc.player.getBlockPos().equals(pos);
    }

    public boolean isInWeb(PlayerEntity player) {
        if (isWeb(this.getPlaceBlock(player, -1.0D))) {
            return true;
        } else {
            return isWeb(this.getPlaceBlock(player, 0.0D)) ? true : isWeb(this.getPlaceBlock(player, 1.0D));
        }
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

    protected BlockPos getPlaceBlock(PlayerEntity player, double y) {
        LinkedHashSet<BlockPos> feetBlock = this.getAllPos(player, y);
        List<BlockPos> collect = feetBlock.stream().filter(BOBlockUtil::isAir).filter((p) -> !BOBlockUtil.cantBlockPlace(p)).limit(1L).toList();
        return collect.size() == 0 ? null : (BlockPos) collect.get(0);
    }

    public LinkedHashSet<BlockPos> getAllPos(PlayerEntity player, double yOff) {
        LinkedHashSet<BlockPos> set = new LinkedHashSet();
        if (player != null) {
            set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(0.0D, yOff, 0.0D)));
            set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(0.2D, yOff, 0.2D)));
            set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(-0.2D, yOff, 0.2D)));
            set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(0.2D, yOff, -0.2D)));
            set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(-0.2D, yOff, -0.2D)));
        }

        return set;
    }

    private void updateTargets() {
        List<PlayerEntity> players = new ArrayList();
        double closestDist = 1000.0D;

        for(int i = 3; i > 0; --i) {
            PlayerEntity closest = null;

            for(PlayerEntity player : this.mc.world.getPlayers()) {
                if (!players.contains(player) && !Friends.get().isFriend(player) && player != this.mc.player && !player.isDead()) {
                    double dist = (double)player.distanceTo(this.mc.player);
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

        this.targets = players;
    }

    private void placeWeb(BlockPos pos) {
        PlaceData data = SettingUtils.getPlaceData(pos);
        if (data.valid()) {
            if (this.progress < this.multiPlace.get()) {
                if (this.mc.world.isAir(pos)) {
                    if (this.mc.world.isAir(pos.up())) {
                        if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
                            if((Managers.BREAK.isMine(pos, true) && CheckMine.get()) || isSelf(pos) || isFriend(pos)) {
                                return;
                            }
                            InvUtils.swap(InvUtils.findInHotbar(Items.COBWEB).slot(), true);
                            renderBlocks.add(new Render(pos, System.currentTimeMillis()));
                            this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                            if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                                Managers.ROTATION.end((long) Objects.hash(new Object[]{this.name + "placing"}));
                            }

                            InvUtils.swapBack();
                            ++this.progress;
                            this.timer.reset();
                        }
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
