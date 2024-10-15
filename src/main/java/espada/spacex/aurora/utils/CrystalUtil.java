package espada.spacex.aurora.utils;

import net.minecraft.block.AirBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CrystalUtil {

    public static Vec3d getMotionVec(Entity entity, int ticks, boolean collision) {
        double dX = entity.getX() - entity.prevX;
        double dZ = entity.getZ() - entity.prevZ;
        double entityMotionPosX = 0.0;
        double entityMotionPosZ = 0.0;
        if (collision) {
            for (int i = 1; i <= ticks && mc.world.getBlockState(new BlockPos((int) (entity.getBlockX() + dX * (double)i), entity.getBlockX(), (int)(entity.getBlockZ() + dZ * (double)i))).getBlock() instanceof AirBlock; ++i) {
                entityMotionPosX = dX * (double)i;
                entityMotionPosZ = dZ * (double)i;
            }
        } else {
            entityMotionPosX = dX * (double)ticks;
            entityMotionPosZ = dZ * (double)ticks;
        }
        return new Vec3d(entityMotionPosX, 0.0, entityMotionPosZ);
    }
}
