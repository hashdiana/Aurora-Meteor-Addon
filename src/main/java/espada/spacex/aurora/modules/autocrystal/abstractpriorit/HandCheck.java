package espada.spacex.aurora.modules.autocrystal.abstractpriorit;

import espada.spacex.aurora.managers.Managers;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.function.Predicate;

import static espada.spacex.aurora.utils.Util.mc;

public class HandCheck {
    public static Hand getHand(Predicate<ItemStack> predicate) {
        return predicate.test(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND :
            predicate.test(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;
    }
}
