package espada.spacex.aurora.managers;

import espada.spacex.aurora.modules.automine.AuroraMine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BreakManager {
    public Map<String, BlockPos> map = new HashMap();

    public BreakManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean isMine(BlockPos pos, boolean self) {
        Iterator var3 = this.map.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, BlockPos> block = (Map.Entry)var3.next();
            if (block.getValue().equals(pos)) {
                return true;
            }
        }

        return self && Modules.get().isActive(AuroraMine.class) && pos.equals(Modules.get().get(AuroraMine.class).targetPos());
    }

    @EventHandler(
        priority = 200
    )
    private void onReceive(PacketEvent.Receive event) {
        Packet var3 = event.packet;
        if (var3 instanceof BlockBreakingProgressS2CPacket p) {
            Entity entity = MeteorClient.mc.world.getEntityById(p.getEntityId());
            PlayerEntity breaker = entity == null ? null : (PlayerEntity)entity;
            if (breaker == null) {
                return;
            }

            this.map.put(breaker.getGameProfile().getName(), p.getPos());
        }

    }
}
