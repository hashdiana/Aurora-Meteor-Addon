package espada.spacex.aurora.modules;

import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.*;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.*;
import java.util.function.Predicate;

public class BurrowPlus2 extends Modules {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final List<Render> renderBlocks = new ArrayList<>();

    //--------------------General--------------------//
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("The mode to switch obsidian.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<LagBackMode> lagBackMode = sgGeneral.add(new EnumSetting.Builder<LagBackMode>()
        .name("LagBack Mode")
        .description("")
        .defaultValue(LagBackMode.XIN)
        .build()
    );


    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Block To Use")
        .description("Which blocks used for burrow.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.ENDER_CHEST)
        .build()
    );



    private final Setting<Boolean> multiPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("Multi Place")
        .description("bypass2?.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> lagBack = sgGeneral.add(new BoolSetting.Builder()
        .name("Lag Back")
        .description("bypass2.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> fillHead = sgGeneral.add(new BoolSetting.Builder()
        .name("Fill Head")
        .description("MaoJunQing")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> AntiWebLag = sgGeneral.add(new BoolSetting.Builder()
        .name("AntiWebLag")
        .description("Pause when player is stuck by cobweb.")
        .defaultValue(false)
        .build()
    );

    //--------------------Attack--------------------//
    private final Setting<Double> attackSpeed = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Speed")
        .description("How many times to attack every second.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
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
    private long lastAttack;
    private final Predicate<ItemStack> predicate;

    public BurrowPlus2() {
        super(Aurora.Extendcombat, "BlockLag", "Places a block inside your feet.");
        MeteorClient.EVENT_BUS.subscribe(new Renderer());
        this.lastAttack = 0L;
        this.predicate = (itemStack) -> {
            Item patt5513$temp = itemStack.getItem();
            if (patt5513$temp instanceof BlockItem block) {
                return ((List)this.blocks.get()).contains(block.getBlock());
            } else {
                return false;
            }
        };
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (AntiWebLag.get() && BOEntityUtils.isWebbed(mc.player)) return;
        if (this.mc.player != null && this.mc.world != null) {
            if (this.mc.player.isOnGround()) {
                BlockPos selfPos = this.getFillBlock();
                if (selfPos == null) {
                    this.toggle();
                    this.sendToggledMsg();
                } else {
                    PlaceData data = SettingUtils.getPlaceData(selfPos);
                    if (data.valid()) {
                        boolean headFillMode = (double)selfPos.getY() > this.mc.player.getY();
                        List<Vec3d> fakeJumpOffsets = this.getFakeJumpOffset(selfPos, headFillMode);
                        if (fakeJumpOffsets.size() != 4) {
                            this.toggle();
                        } else {
                            Hand hand = this.predicate.test(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : (this.predicate.test(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null);
                            boolean blocksPresent = hand != null;
                            if (!blocksPresent) {
                                switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                                    case 1:
                                    case 2:
                                        blocksPresent = InvUtils.findInHotbar(this.predicate).found();
                                        break;
                                    case 3:
                                    case 4:
                                        blocksPresent = InvUtils.find(this.predicate).found();
                                }
                            }

                            if (blocksPresent) {
                                this.attackCrystal(selfPos);
                                if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
                                    boolean switched = hand != null;
                                    if (!switched) {

                                        switched = switch (((SwitchMode) this.switchMode.get()).ordinal()) {
                                            case 1, 2 ->
                                                    InvUtils.swap(InvUtils.findInHotbar(this.predicate).slot(), true);
                                            case 3 -> BOInvUtils.pickSwitch(InvUtils.find(this.predicate).slot());
                                            case 4 -> BOInvUtils.invSwitch(InvUtils.find(this.predicate).slot());
                                            default -> throw new IncompatibleClassChangeError();
                                        };
                                    }

                                    if (switched) {
                                        this.doFakeJump(fakeJumpOffsets);
                                        if ((Boolean) this.multiPlace.get()) {
                                            this.multiPlace(headFillMode);
                                        } else {
                                            this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                                        }


                                        if ((Boolean) this.placeSwing.get()) {
                                            this.clientSwing((SwingHand) this.placeHand.get(), Hand.MAIN_HAND);
                                        }

                                        BlockPos yxPos = this.mc.player.getBlockPos();
                                        if ((Boolean) this.lagBack.get()) {
                                            this.doLagBack(yxPos);
                                        }

                                        switch (((SwitchMode) this.switchMode.get()).ordinal()) {
                                            case 1 -> InvUtils.swapBack();
                                            case 2 -> InvUtils.swapBack();
                                            case 3 -> BOInvUtils.pickSwapBack();
                                            case 4 -> BOInvUtils.swapBack();
                                        }

                                        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                                            Managers.ROTATION.end((long) Objects.hash(new Object[]{this.name + "placing"}));
                                        }

                                    }
                                    this.toggle();
                                    this.sendToggledMsg();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void multiPlace(boolean headFillMode) {
        if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.0, 0.0, 0.0)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.0, 0.0, 0.0)));
        }

        if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 0.0, 0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 0.0, 0.3)));
        }

        if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 0.0, 0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 0.0, 0.3)));
        }

        if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 0.0, -0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 0.0, -0.3)));
        }

        if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 0.0, -0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 0.0, -0.3)));
        }

        if (headFillMode) {
            if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.0, 1.0, 0.0)))) {
                this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.0, 1.0, 0.0)));
            }

            if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 1.0, 0.3)))) {
                this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 1.0, 0.3)));
            }

            if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 1.0, 0.3)))) {
                this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 1.0, 0.3)));
            }

            if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 1.0, -0.3)))) {
                this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, 1.0, -0.3)));
            }

            if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 1.0, -0.3)))) {
                this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, 1.0, -0.3)));
            }
        }

    }

    private void attackCrystal(BlockPos pos) {
        if (!((double)(System.currentTimeMillis() - this.lastAttack) < 1000.0 / (Double)this.attackSpeed.get())) {
            if (EntityInfo.CrystalCheck(pos)) {
                Entity blocking = this.getBlocking();
                if (blocking != null) {
                    if (!SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(blocking.getBoundingBox(), (double)this.priority - 0.1, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"}))) {
                        SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
                        sendPacket(PlayerInteractEntityC2SPacket.attack(blocking, this.mc.player.isSneaking()));
                        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
                        if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "attacking"}));
                        }

                        this.lastAttack = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    public double ez() {
        if (!BOBlockUtil.isAir(EntityInfo.playerPos(this.mc.player).up(3))) {
            return 1.2;
        } else {
            double lol = 2.2;

            for(int i = 4; i < 6; ++i) {
                if (!BOBlockUtil.isAir(EntityInfo.playerPos(this.mc.player).up(i))) {
                    return lol + (double)i - 4.0;
                }
            }

            return 10.0;
        }
    }

    private void doLagBack(BlockPos selfPos) {
        switch (((LagBackMode)this.lagBackMode.get()).ordinal()) {
            case 1:
                for(int i = 10; i > 0; --i) {
                    if (BOBlockUtil.isAir(selfPos.add(0, i, 0)) && BOBlockUtil.isAir(selfPos.add(0, i, 0).up())) {
                        BlockPos lagPos = selfPos.add(0, i, 0);
                        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround((double)lagPos.getX() + 0.5, (double)lagPos.getY(), (double)lagPos.getZ() + 0.5, true));
                    }
                }
            case 2:
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 2.0, this.mc.player.getZ(), true));
            case 3:
                break;
            default:
                return;
        }

        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + this.ez(), this.mc.player.getZ(), true));
    }

    private void doFakeJump(List<Vec3d> offsets) {
        if (offsets != null) {
            offsets.forEach((vec) -> {
                if (vec != null && !vec.equals(new Vec3d(0.0, 0.0, 0.0))) {
                    sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, true));
                }
            });
        }
    }

    private List<Vec3d> getFakeJumpOffset(BlockPos burBlock, boolean headFillMode) {
        List<Vec3d> offsets = new LinkedList();
        Vec3d offVec;
        if (headFillMode) {
            if (BOBlockUtil.fakeBBoxCheckFeet(this.mc.player, new Vec3d(0.0, 2.0, 0.0))) {
                offVec = this.getVec3dDirection(burBlock);
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.4199999868869781, this.mc.player.getZ() + offVec.z * 0.42132));
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.7531999805212017, this.mc.player.getZ() + offVec.z * 0.95));
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.9999957640154541, this.mc.player.getZ() + offVec.z * 1.03));
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 1.1661092609382138, this.mc.player.getZ() + offVec.z * 1.0933));
            } else {
                offVec = this.getVec3dDirection(burBlock);
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 0.42132));
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 0.95));
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 1.03));
                offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 1.0933));
            }
        } else if (BOBlockUtil.fakeBBoxCheckFeet(this.mc.player, new Vec3d(0.0, 2.0, 0.0))) {
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.4199999868869781, this.mc.player.getZ()));
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.7531999805212017, this.mc.player.getZ()));
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.9999957640154541, this.mc.player.getZ()));
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 1.1661092609382138, this.mc.player.getZ()));
        } else {
            offVec = this.getVec3dDirection(burBlock);
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 0.42132));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 0.95));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 1.03));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 1.0933));
        }

        return offsets;
    }

    public Vec3d getVec3dDirection(BlockPos burBlockPos) {
        Vec3d v = (new Vec3d((double)burBlockPos.getX(), (double)burBlockPos.getY(), (double)burBlockPos.getZ())).add(0.5, 0.5, 0.5);
        BlockPos pPos = BOBlockUtil.getFlooredPosition(this.mc.player);
        Vec3d s = this.mc.player.getPos().subtract(v);
        Vec3d off = new Vec3d(0.0, 0.0, 0.0);
        if (Math.abs(s.x) >= Math.abs(s.z) && Math.abs(s.x) > 0.2) {
            if (s.x > 0.0) {
                off = new Vec3d(0.8 - s.x, 0.0, 0.0);
            } else {
                off = new Vec3d(-0.8 - s.x, 0.0, 0.0);
            }
        } else if (Math.abs(s.z) >= Math.abs(s.x) && Math.abs(s.z) > 0.2) {
            if (s.z > 0.0) {
                off = new Vec3d(0.0, 0.0, 0.8 - s.z);
            } else {
                off = new Vec3d(0.0, 0.0, -0.8 - s.z);
            }
        } else if (burBlockPos.equals(pPos)) {
            List<Direction> facList = new ArrayList();
            Direction[] var7 = Direction.values();
            int var8 = var7.length;

            for (Direction f : var7) {
                if (f != Direction.UP && f != Direction.DOWN && BOBlockUtil.isAir(pPos.offset(f)) && BOBlockUtil.isAir(pPos.offset(f).offset(Direction.UP))) {
                    facList.add(f);
                }
            }

            facList.sort((f1, f2) -> {
                Vec3d offVec1 = v.add((new Vec3d(f1.getUnitVector())).multiply(0.5));
                Vec3d offVec2 = v.add((new Vec3d(f2.getUnitVector())).multiply(0.5));
                return (int)(PlayerUtils.distanceTo(offVec1.x, this.mc.player.getY(), offVec1.z) - PlayerUtils.distanceTo(offVec2.x, this.mc.player.getY(), offVec2.z));
            });
            if (facList.size() > 0) {
                off = new Vec3d(((Direction)facList.get(0)).getUnitVector());
            }
        }

        return off;
    }

    private Entity getBlocking() {
        Entity crystal = null;
        if (this.mc.world != null && this.mc.player != null) {
            Iterator var2 = this.mc.world.getEntities().iterator();

            while(var2.hasNext()) {
                Entity entity = (Entity)var2.next();
                if (entity instanceof EndCrystalEntity && SettingUtils.inAttackRange(entity.getBoundingBox())) {
                    crystal = entity;
                }
            }
        }

        return crystal;
    }

    protected BlockPos getFillBlock() {
        LinkedHashSet<BlockPos> feetBlock = this.getFeetBlock(0);
        List<BlockPos> collect = feetBlock.stream().filter(BOBlockUtil::isAir).filter((p) -> {
            return !BOBlockUtil.cantBlockPlace(p);
        }).limit(1L).toList();
        return collect.size() == 0 ? null : (BlockPos)collect.get(0);
    }

    public LinkedHashSet<BlockPos> getFeetBlock(int yOff) {
        LinkedHashSet<BlockPos> set = new LinkedHashSet();
        set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.0, (double)yOff, 0.0)));
        set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)yOff, 0.3)));
        set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)yOff, 0.3)));
        set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)yOff, -0.3)));
        set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)yOff, -0.3)));
        if ((Boolean)this.fillHead.get() && yOff == 0) {
            set.addAll(this.getFeetBlock(1));
        }

        return set;
    }

    public void mPlace(Hand hand, BlockPos pos) {
        Vec3d eyes = this.mc.player.getEyePos();
        boolean inside = eyes.x > (double)pos.getX() && eyes.x < (double)(pos.getX() + 1) && eyes.y > (double)pos.getY() && eyes.y < (double)(pos.getY() + 1) && eyes.z > (double)pos.getZ() && eyes.z < (double)(pos.getZ() + 1);
        PlaceData data = SettingUtils.getPlaceData(pos);
        if (data.valid()) {
            renderBlocks.add(new Render(pos, System.currentTimeMillis()));
            SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
            this.sendSequenced((s) -> {
                return new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(data.pos().toCenterPos(), data.dir(), data.pos(), inside), s);
            });
            SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
        }
    }

    public enum SwitchMode{
        Normal,
        Silent,
        PickSilent,
        InvSwitch;
    }

    public enum LagBackMode{
        OBS,
        XIN,
        OLD;
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
