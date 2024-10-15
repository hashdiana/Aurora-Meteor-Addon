package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Vec3dInfo {
    public static boolean isInRange(Vec3d vec3d, double radius) {
        return vec3d.isInRange(vec3d, radius);
    }

    public static boolean isWithinRange(Vec3d vec3d, double range) {
        return MeteorClient.mc.player.getBlockPos().isWithinDistance(vec3d, range);
    }

    public static Vec3d add(Vec3d vec3d, Vec3d added) {
        return new Vec3d(vec3d.add(added).getX(), vec3d.add(added).getY(), vec3d.add(added).getZ());
    }

    public static Vec3d add(Vec3d vec3d, double x, double y, double z) {
        return new Vec3d(vec3d.add(x, y, z).getX(), vec3d.add(x, y, z).getY(), vec3d.add(x, y, z).getZ());
    }

    public static boolean notNull(Vec3d vec3d) {
        return vec3d != null;
    }

    public static Vec3d getEyeVec(PlayerEntity entity) {
        return entity.getPos().add(0.0, entity.getEyeHeight(entity.getPose()), 0.0);
    }

    public static Vec3d closestVec3d(BlockPos blockPos) {
        if (blockPos == null) {
            return new Vec3d(0.0, 0.0, 0.0);
        }
        double x = MathHelper.clamp(MeteorClient.mc.player.getX() - (double)blockPos.getX(), 0.0, 1.0);
        double y = MathHelper.clamp(MeteorClient.mc.player.getY() - (double)blockPos.getY(), 0.0, 0.6);
        double z = MathHelper.clamp(MeteorClient.mc.player.getZ() - (double)blockPos.getZ(), 0.0, 1.0);
        return new Vec3d((double)blockPos.getX() + x, (double)blockPos.getY() + y, (double)blockPos.getZ() + z);
    }
}
