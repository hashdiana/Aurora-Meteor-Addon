package espada.spacex.aurora.hud;


import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.utils.render.color.Color;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PacketHud extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> swap = sgGeneral.add(new BoolSetting.Builder()
        .name("swap")
        .description("Swaps the order of the text.")
        .defaultValue(false)
        .build()
    );

    public static final HudElementInfo<PacketHud> INFO = new HudElementInfo<>(Aurora.HUD_EDIT,"Packet Hud", "Displays your average send packets.",PacketHud::new);

    public PacketHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        int send = mc.getNetworkHandler() == null ? 23 : (int) mc.getNetworkHandler().getConnection().getAveragePacketsSent();
        int received = mc.getNetworkHandler() == null ? 86 : (int) mc.getNetworkHandler().getConnection().getAveragePacketsReceived();

        double width = 0;
        double height = 0;

        width += renderer.textWidth("Send: " + send + 10);
        height += renderer.textHeight() * 2;
        if (renderer.textWidth("Received: " + received + 10) > width) width = renderer.textWidth("Received: " + received + 10);

        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        int send = mc.getNetworkHandler() == null ? 23 : (int) mc.getNetworkHandler().getConnection().getAveragePacketsSent();
        int received = mc.getNetworkHandler() == null ? 86 : (int) mc.getNetworkHandler().getConnection().getAveragePacketsReceived();

        double x = this.x;
        double y = this.y;

        Color primaryColor = TextHud.getSectionColor(0);
        Color secondaryColor = TextHud.getSectionColor(1);

        if (swap.get()) {
            renderer.text("Received: ", x, y, primaryColor,true);
            x += renderer.textWidth("Received: ");
            renderer.text("" + received, x, y, secondaryColor,true);
        } else {
            renderer.text("Send: ", x, y, primaryColor,true);
            x += renderer.textWidth("Send: ");
            renderer.text("" + send, x, y, secondaryColor,true);
        }

        y += renderer.textHeight();
        x = this.x;

        if (swap.get()) {
            renderer.text("Send: ", x, y,primaryColor,true);
            x += renderer.textWidth("Send: ");
            renderer.text("" + send, x, y, secondaryColor,true);
        } else {
            renderer.text("Received: ", x, y,primaryColor,true);
            x += renderer.textWidth("Received: ");
            renderer.text("" + received, x, y,secondaryColor,true);
        }
    }
}
