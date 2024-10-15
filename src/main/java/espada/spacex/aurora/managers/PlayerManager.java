package espada.spacex.aurora.managers;

import espada.spacex.aurora.utils.RenderUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;

public class PlayerManager {
    public void init() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        RenderUtils.updateJello();
    }
}
