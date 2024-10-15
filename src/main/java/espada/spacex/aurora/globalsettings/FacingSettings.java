package espada.spacex.aurora.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

/**
 * @author OLEPOSSU
 */

public class FacingSettings extends Modules {
    public FacingSettings() {
        super(Aurora.SETTINGS, "Facing", "Global facing settings for every aurora module.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private static FacingSettings INSTANCE = new FacingSettings();

    //  Place Ranges
    public final Setting<Boolean> strictDir = sgGeneral.add(new BoolSetting.Builder()
        .name("Strict Direction")
        .description("Doesn't place on faces which aren't in your direction.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> unblocked = sgGeneral.add(new BoolSetting.Builder()
        .name("Unblocked")
        .description("Doesn't place on faces that have block on them.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("Air Place")
        .description("Allows placing blocks in air.")
        .defaultValue(false)
        .build()
    );
    public final Setting<MaxHeight> maxHeight = sgGeneral.add(new EnumSetting.Builder<MaxHeight>()
        .name("Max Height")
        .description("Doesn't place on top sides of blocks at max height. Old: 1.12, New: 1.17+")
        .defaultValue(MaxHeight.New)
        .build()
    );


    public static FacingSettings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FacingSettings();
        }

        return INSTANCE;
    }

    public PlaceData getPlaceDataOR(BlockPos pos, Predicate predicate, boolean ignoreContainers) {
        if (pos == null) {
            return new PlaceData(null, null, false);
        } else {
            Direction best = null;
            if (this.mc.world != null && this.mc.player != null) {
                if (this.airPlace.get()) {
                    return new PlaceData(pos, Direction.UP, true);
                }

                double cDist = -1.0D;

                for(Direction dir : Direction.values()) {
                    if (!this.heightCheck(pos.offset(dir)) && (!ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && (OLEPOSSUtils.solid(pos.offset(dir)) || predicate == null || predicate.test(pos.offset(dir))) && (!this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                        double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                        if (dist >= 0.0D && (cDist < 0.0D || dist < cDist)) {
                            best = dir;
                            cDist = dist;
                        }
                    }
                }
            }

            return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
        }
    }

    public PlaceData getPlaceDataAND(BlockPos pos, Predicate<Direction> predicate, Predicate<BlockPos> predicatePos, boolean ignoreContainers) {
        if (pos == null) {
            return new PlaceData((BlockPos)null, (Direction)null, false);
        } else {
            Direction best = null;
            if (this.mc.world != null && this.mc.player != null) {
                if ((Boolean)this.airPlace.get()) {
                    return new PlaceData(pos, Direction.UP, true);
                }

                double cDist = -1.0;
                Direction[] var8 = Direction.values();
                int var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    Direction dir = var8[var10];
                    if (!this.heightCheck(pos.offset(dir)) && (this.mc.player.isSneaking() || !ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && (this.mc.player.isSneaking() || !ignoreContainers || !(this.mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof ButtonBlock)) && OLEPOSSUtils.solid(pos.offset(dir)) && (predicate == null || predicate.test(dir)) && (predicatePos == null || predicatePos.test(pos.offset(dir))) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                        double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                        if (dist >= 0.0 && (cDist < 0.0 || dist < cDist)) {
                            best = dir;
                            cDist = dist;
                        }
                    }
                }
            }

            return best == null ? new PlaceData((BlockPos)null, (Direction)null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
        }
    }

    public PlaceData getPlaceDataA(BlockPos pos, boolean ignoreContainers) {
        if (pos == null) {
            return new PlaceData((BlockPos)null, (Direction)null, false);
        } else {
            Direction best = null;
            if (this.mc.world != null && this.mc.player != null) {
                if ((Boolean)this.airPlace.get()) {
                    return new PlaceData(pos, Direction.UP, true);
                }

                double cDist = -1.0;
                Direction[] var6 = Direction.values();
                int var7 = var6.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    Direction dir = var6[var8];
                    if (!this.heightCheck(pos.offset(dir)) && (this.mc.player.isSneaking() || !ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && OLEPOSSUtils.solid(pos.offset(dir)) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                        double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                        if (dist >= 0.0 && (cDist < 0.0 || dist < cDist)) {
                            best = dir;
                            cDist = dist;
                        }
                    }
                }
            }

            return best == null ? new PlaceData((BlockPos)null, (Direction)null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
        }
    }

    public PlaceData getPlaceData(BlockPos pos, boolean ignoreContainers) {
        if (pos == null) {
            return new PlaceData(null, null, false);
        } else {
            Direction best = null;
            if (this.mc.world != null && this.mc.player != null) {
                if ((Boolean)this.airPlace.get()) {
                    return new PlaceData(pos, Direction.UP, true);
                }

                double cDist = -1.0;
                Direction[] var6 = Direction.values();
                int var7 = var6.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    Direction dir = var6[var8];
                    if (!this.heightCheck(pos.offset(dir)) && (this.mc.player.isSneaking() || !ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && (this.mc.player.isSneaking() || !ignoreContainers || !(this.mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof ButtonBlock)) && (this.mc.player.isSneaking() || !ignoreContainers || !(this.mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof RespawnAnchorBlock) || (Integer)this.mc.world.getBlockState(pos.offset(dir)).get(Properties.CHARGES) < 1) && OLEPOSSUtils.solid(pos.offset(dir)) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                        double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                        if (dist >= 0.0 && (cDist < 0.0 || dist < cDist)) {
                            best = dir;
                            cDist = dist;
                        }
                    }
                }
            }

            return best == null ? new PlaceData(null, null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
        }
    }

    public Direction getPlaceOnDirection(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        Direction best = null;
        if (mc.world != null && mc.player != null) {
            double cDist = -1;
            for (Direction dir : Direction.values()) {

                // Doesn't place on top of max height
                if (heightCheck(pos.offset(dir))) {
                    continue;
                }

                // Unblocked check (mostly for autocrystal placement facings)
                if (unblocked.get() && !(getBlock(pos.offset(dir)) instanceof AirBlock)) {
                    continue;
                }

                // Strict dir check (checks if face isnt on opposite side of the block to player)
                if (strictDir.get() && !OLEPOSSUtils.strictDir(pos, dir)) {
                    continue;
                }

                // Only accepts if closer than last accepted direction
                double dist = dist(pos, dir);
                if (dist >= 0 && (cDist < 0 || dist < cDist)) {
                    best = dir;
                    cDist = dist;
                }
            }
        }
        return best;
    }

    private boolean heightCheck(BlockPos pos) {
        return pos.getY() >= switch (maxHeight.get()) {
            case Old -> 255;
            case New -> 319;
            case Disabled -> 1000; // im pretty sure 1000 is enough
        };
    }

    private double dist(BlockPos pos, Direction dir) {
        if (mc.player == null) {
            return 0;
        }

        Vec3d vec = new Vec3d(pos.getX() + dir.getOffsetX() / 2f, pos.getY() + dir.getOffsetY() / 2f, pos.getZ() + dir.getOffsetZ() / 2f);
        Vec3d dist = mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z);

        return Math.sqrt(dist.x * dist.x + dist.y * dist.y + dist.z * dist.z);
    }

    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public enum MaxHeight {
        Old,
        New,
        Disabled
    }
}
