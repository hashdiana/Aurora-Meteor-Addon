package espada.spacex.aurora.modules.autocrystal.abstractpriorit;

import espada.spacex.aurora.utils.SettingUtils;
import net.minecraft.util.math.BlockPos;

public class RangeCheck {
    public static boolean inPlaceRange(BlockPos pos) {
        return SettingUtils.inPlaceRange(pos);
    } //Should I Make one More Range only For AutoCrystal?
}
