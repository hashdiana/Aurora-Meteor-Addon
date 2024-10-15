package espada.spacex.aurora.modules.autocrystal.abstractpriorit;

import net.minecraft.block.AirBlock;
import net.minecraft.util.math.BlockPos;

import static espada.spacex.aurora.utils.Util.mc;

public class AirCheck {
    public static boolean air(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() instanceof AirBlock;
    }
}
