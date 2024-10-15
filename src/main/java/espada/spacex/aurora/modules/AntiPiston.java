package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public class AntiPiston extends Modules {
    int slot = -1;
    public AntiPiston() {
        super(Aurora.EAncillary , "AntiPiston" , "1");
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (AntiPiston.nullCheck()) {
            return;
        }
        if (!this.mc.player.isOnGround()) {
            return;
        }
        this.slot = InvUtils.findInHotbar(Items.OBSIDIAN).slot();
        if (this.slot == -1) {
            return;
        }
        BlockPos eyePos = BlockPos.ofFloored(this.mc.player.getEyePos());
        if (!BOBlockUtil.isAir(eyePos.up())) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || direction == Direction.UP || !(BOBlockUtil.getBlock(eyePos.offset(direction)) instanceof PistonBlock) && BOBlockUtil.getBlock(eyePos.offset(direction)) != Blocks.MOVING_PISTON && BOBlockUtil.getBlock(eyePos.offset(direction)) != Blocks.PISTON_HEAD) continue;
            this.doPlace(Hand.MAIN_HAND, eyePos.offset(direction.getOpposite()));
            this.doPlace(Hand.MAIN_HAND, eyePos.offset(direction).up());
        }
    }

    public void doPlace(Hand hand, BlockPos pos) {
        if (!BOBlockUtil.isAir(pos) || BOBlockUtil.cantBlockPlace(pos)) {
            return;
        }
        PlaceData data = SettingUtils.getPlaceData(pos);
        if (!data.valid()) {
            return;
        }
        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long) Objects.hash(this.name + "placing"))) {
            return;
        }
        InvUtils.swap(this.slot, true);
        this.placeBlock(hand, data.pos().toCenterPos(), data.dir(), data.pos());
        InvUtils.swapBack();
        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(this.name + "placing"));
        }
    }
}
