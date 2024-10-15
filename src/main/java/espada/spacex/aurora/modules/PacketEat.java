package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

import java.util.Objects;

public class PacketEat extends Modules {
    public PacketEat() {
        super(Aurora.EAncillary, "PacketEat" , "PackEat");
    }
    private Item PackEatItem;

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (mc.player != null && mc.player.isUsingItem()) {
            PackEatItem = mc.player.getActiveItem().getItem();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        try {
            if(event.packet instanceof PlayerActionC2SPacket packet) {
                if (packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && Objects.requireNonNull(this.PackEatItem.getFoodComponent()).isAlwaysEdible()) {
                    event.cancel();
                }
            }
        } catch (Exception ignored) {
        }
    }


}
