package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.automine.AuroraMine;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Timer;
import espada.spacex.aurora.utils.Util;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public class KeyCity
    extends Modules {
    private final SettingGroup sgGeneral;
    private final Setting<Double> targetRange;
    private final Setting<Boolean> mineHead;
    private final Setting<Boolean> onlybur;
    private final Setting<Integer> delay;
    private final Setting<Boolean> pauseEat;


    private PlayerEntity target;
    private final Timer timer = new Timer();
    AuroraMine autoMine = meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);

    public KeyCity() {
        super(Aurora.Extendcombat, "AntiSurround", "Breaks target's surround with PacketMine.");
        this.sgGeneral = this.settings.getDefaultGroup();
        delay = this.sgGeneral.add(new IntSetting.Builder().name("Delay").defaultValue(35).min(0).sliderRange(0, 2000).build());
        this.targetRange = this.sgGeneral.add(new DoubleSetting.Builder().name("Target Range").description("The range players can be targeted.").defaultValue(5).sliderRange(0, 7).build());
        this.mineHead = this.sgGeneral.add(new BoolSetting.Builder().name("Mine Head").description("an.").defaultValue(true).build());
        this.onlybur = this.sgGeneral.add(new BoolSetting.Builder().name("Only Burrow").description("an.").defaultValue(true).build());
        pauseEat = sgGeneral.add(new BoolSetting.Builder()
            .name("Pause On Eat")
            .description("Pause while eating.")
            .defaultValue(false)
            .build()
        );
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (pauseEat.get() && mc.player.isUsingItem()) return;
        if (this.timer.passedMs(this.delay.get())) {
            this.target = TargetUtils.getPlayerTarget(this.targetRange.get().intValue(), SortPriority.LowestDistance);
            if (TargetUtils.isBadTarget(this.target, this.targetRange.get().intValue())) {
                return;
            }
            if (!InvUtils.findInHotbar(Items.IRON_PICKAXE, Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE).found()) {
                return;
            }
            this.surroundMine();
        }
    }

    private void surroundMine() {
        if (this.target == null) {
            return;
        }
        BlockPos feet = this.target.getBlockPos();
        if (this.canMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, 0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, 0.2)));
            return;
        }if (this.canMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, 0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, 0.2)));
            return;
        }if (this.canMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, -0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, -0.2)));
            return;
        }if (this.canMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, -0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, -0.2)));
            return;
        }
        if (this.mineHead.get() && this.canMine2(feet.up().up())) {
            this.surroundMine(feet.up().up());
            return;
        }
        if (this.mineHead.get() && this.canMine2(feet.up().up().up())) {
            this.surroundMine(feet.up().up().up());
            return;
        }
        if(!onlybur.get() && !CheckMineSur()){
            if (this.canMine2(feet.east())) {
                this.surroundMine(feet.east());
                return;
            }
            if (this.canMine2(feet.west())) {
                this.surroundMine(feet.west());
                return;
            }
            if (this.canMine2(feet.south())) {
                this.surroundMine(feet.south());
                return;
            }
            if (this.canMine2(feet.north())) {
                this.surroundMine(feet.north());
            }
        }
    }

    private void surroundMine(BlockPos position) {
        Direction dir = SettingUtils.getPlaceOnDirection(position);
        if(dir == null) return;
        this.mc.interactionManager.attackBlock(position, dir);
        timer.reset();
    }

    private boolean canMine(BlockPos block) {
        if(isSelf(block) || isFriend(block)){
            return false;
        }
        Direction dir = SettingUtils.getPlaceOnDirection(block);
        if(dir == null) return false;
        if (isMine2()) {
            return false;
        }
        if (isMine1()) {
            if (CheckMinePos1(block)){
                return false;
            }
        }
        if(!SettingUtils.inMineRange(block)) return false;
        return !this.isAir(block) && !this.godBlock(block) && BOBlockUtil.getBlock(block) != Blocks.COBWEB;
    }

    private boolean canMine2(BlockPos block) {
        if(isSelf(block) || isFriend(block)){
            return false;
        }
        Direction dir = SettingUtils.getPlaceOnDirection(block);
        if(dir == null) return false;
        if (isMine2()) {
            return false;
        }
        if (isMine1()) {
            if (CheckMinePos1(block)){
                return false;
            }
        }
        if(!SettingUtils.inMineRange(block)) return false;
        return !this.isAir(block) && !this.godBlock(block) && BOBlockUtil.getBlock(block) != Blocks.COBWEB && BOBlockUtil.getBlock(block) != Blocks.RESPAWN_ANCHOR;
    }

    private boolean isSelf(BlockPos pos) {
        for (Entity entity : Util.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity != Util.mc.player) continue;
            return true;
        }
        return false;
    }

    private boolean isFriend(BlockPos pos) {
        for (PlayerEntity entity : Util.mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
            if (!Friends.get().isFriend(entity)) continue;
            return true;
        }
        return false;
    }

    private boolean godBlock(BlockPos block) {
        return BOBlockUtil.getBlock(block) == Blocks.BEDROCK;
    }

    private boolean isAir(BlockPos block) {
        return BOBlockUtil.getBlock(block) == Blocks.AIR;
    }

    @Override
    public String getInfoString() {
        return this.target != null ? this.target.getGameProfile().getName() : null;
    }

    private boolean isMine1() {
        return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).targetPos() != null;
    }

    private boolean CheckMinePos1(BlockPos pos) {
        return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && pos.equals(meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).targetPos());
    }

    private boolean isMine2() {
        return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).breakPos != null;
    }

    private boolean CheckMinePos2(BlockPos pos) {
        return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && pos.equals(meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).breakPos);
    }

    private BlockPos getMinePos(){
        return meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).targetPos();
    }

    private BlockPos getMinePos2(){
        return meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class).breakPos;
    }

    private boolean CheckMine(){
        BlockPos feet = this.target.getBlockPos();
        if (Objects.equals(getMinePos(), BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, 0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos(), BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, 0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos(), BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, -0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos(), BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, -0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos(), feet.up().up()) && mineHead.get()) {
            return true;
        }
        if(!onlybur.get()){
            if (Objects.equals(getMinePos(), feet.east())) {
                return true;
            }
            if (Objects.equals(getMinePos(), feet.west())) {
                return true;
            }
            if (Objects.equals(getMinePos(), feet.south())) {
                return true;
            }
            if (Objects.equals(getMinePos(), feet.north())) {
                return true;
            }
        }
        return false;
    }

    private boolean CheckMineSur(){
        BlockPos feet = this.target.getBlockPos();
        if (Objects.equals(getMinePos(), feet.east())) {
            return true;
        }
        if (Objects.equals(getMinePos(), feet.west())) {
            return true;
        }
        if (Objects.equals(getMinePos(), feet.south())) {
            return true;
        }
        if (Objects.equals(getMinePos(), feet.north())) {
            return true;
        }
        return false;
    }

    private boolean CheckMine2(){
        BlockPos feet = this.target.getBlockPos();
        if (Objects.equals(getMinePos2(), BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, 0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos2(), BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, 0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos2(), BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, 0, -0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos2(), BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, 0, -0.2)))) {
            return true;
        }
        if (Objects.equals(getMinePos2(), feet.up().up()) && mineHead.get()) {
            return true;
        }
        if(!onlybur.get()){
            if (Objects.equals(getMinePos2(), feet.east())) {
                return true;
            }
            if (Objects.equals(getMinePos2(), feet.west())) {
                return true;
            }
            if (Objects.equals(getMinePos2(), feet.south())) {
                return true;
            }
            if (Objects.equals(getMinePos2(), feet.north())) {
                return true;
            }
        }
        return false;
    }
}
