package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class MoveUp extends Modules {
    public MoveUp() {
        super(Aurora.ExtendMove, "AutoBup", "Help you move up from burrow");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyMoveInBurrow = sgGeneral.add(new BoolSetting.Builder()
        .name("onlyMoveInBurrow")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> rubberbandOffset = sgGeneral.add(new DoubleSetting.Builder()
        .name("rubberbandOffset")
        .description("Delay between breaking torches.")
        .defaultValue(9)
        .range(-10, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> rubberbandPackets = sgGeneral.add(new DoubleSetting.Builder()
        .name("rubberbandPackets")
        .description("Delay between breaking torches.")
        .defaultValue(1)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> pEndChest = sgGeneral.add(new BoolSetting.Builder()
        .name("PauseInEndChest")
        .description("Pause ec player.")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onActivate() {
        if (!onlyMoveInBurrow.get() || BOEntityUtils.isBurrowed(mc.player, !pEndChest.get())) {
            double y = 0;
            double velocity = 0.42;

            while (y < 1.1) {
                y += velocity;
                velocity = (velocity - 0.08) * 0.98;

                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y, mc.player.getZ(), false));
            }

            for (int i = 0; i < rubberbandPackets.get(); i++) {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y + rubberbandOffset.get(), mc.player.getZ(), false));
            }
        }
        toggle();
    }
}
