package espada.spacex.aurora.modules.autocrystal.abstractpriorit;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import static espada.spacex.aurora.utils.Util.mc;

public class CrystalPlaceBlockCheck {
    public static boolean crystalBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) ||
            mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }
}
