package espada.spacex.aurora.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class MathUtil {

    public static Vec3d interpolateEntity(final Entity entity, final float time) {
        return new Vec3d(entity.lastRenderX + (entity.getX() - entity.lastRenderX) * time, entity.lastRenderY + (entity.getY() - entity.lastRenderY) * time, entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * time);
    }
}
