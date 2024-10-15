package espada.spacex.aurora.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RSCombatInfo {

    public static Box getCrystalBox(BlockPos pos) {
        return new Box(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
    }

    public static Box getCrystalBox(Vec3d pos) {
        return new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }
}
