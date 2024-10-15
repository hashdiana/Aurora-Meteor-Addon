package espada.spacex.aurora.utils;

import espada.spacex.aurora.globalsettings.RotationPrioritySettings;
import espada.spacex.aurora.modules.*;
import espada.spacex.aurora.modules.autocrystal.AutoCrystal;
import espada.spacex.aurora.modules.automine.AuroraMine;
import espada.spacex.aurora.modules.autoweb.AutoWeb;
import espada.spacex.aurora.modules.maojunqing.MaoJunQingAura;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTrap;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.combat.SelfWeb;
import meteordevelopment.meteorclient.systems.modules.movement.AntiAFK;

/**
 * @author OLEPOSSU
 */

public class PriorityUtils {
        public static int get(Object module) {
            RotationPrioritySettings priority = Modules.get().get(RotationPrioritySettings.class);

            if (priority != null) {
                if (module instanceof AntiAim) return priority.antiAim.get();
                if (module instanceof AntiAFK) return priority.antiAFK.get();
                if (module instanceof HoleFillRewrite) return priority.autoHoleFillPlus.get();
                if (module instanceof AutoPearl) return priority.autoPearlClip.get();
                if (module instanceof AutoTrapPlus) return priority.autoTrap.get();
                if (module instanceof AuroraMine) return priority.autoMine.get();
                if (module instanceof KillAura) return priority.killAura.get();
                if (module instanceof PistonCrystal) return priority.pistonCrystal.get();
                if (module instanceof ScaffoldPlus) return priority.scaffold.get();
                if (module instanceof SelfTrapPlus) return priority.selfTrap.get();
                if (module instanceof SurroundPlus) return priority.surroundPlus.get();
        }
            return 1000;
        }
}
