package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

public class PortalGodMode extends Modules {
    public PortalGodMode() {super(Aurora.AURORA, "Portal God Mode", "Prevents taking damage while in portals");}
    @EventHandler
    private void onSend(PacketEvent.Send event) {
        if (event.packet instanceof TeleportConfirmC2SPacket) {
            event.cancel();
        }
    }
}
