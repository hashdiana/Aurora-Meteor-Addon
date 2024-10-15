package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.SettingUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class BreakCrystal
    extends Modules {
    private Entity crystal;

    public BreakCrystal() {
        super(Aurora.AURORA, "BreakCrystal", "");
    }

    @EventHandler(priority=200)
    private void onTick(TickEvent.Pre event) {
        this.crystal = this.getBlocking();
        if (this.crystal == null) {
            return;
        }
        this.sendPacket(PlayerInteractEntityC2SPacket.attack(this.crystal, this.mc.player.isSneaking()));
        this.toggle();
    }

    private Entity getBlocking() {
        Entity crystal = null;
        double lowest = 1000.0;
        for (Entity entity : this.mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity) || this.mc.player.distanceTo(entity) > 5.0f || !SettingUtils.inAttackRange(entity.getBoundingBox())) continue;
            crystal = entity;
        }
        return crystal;
    }
}
