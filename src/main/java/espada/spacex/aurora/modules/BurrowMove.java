package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class BurrowMove extends Modules {
    public BurrowMove() {
        super(Aurora.ExtendMove, "Burrow Move", "Allow you move in burrow.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("The speed in blocks per second.")
        .defaultValue(0.3)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    public final Setting<Double> AnchorSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("AnchorSpeed")
        .description("The speed in blocks per second.")
        .defaultValue(0.3)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    public final Setting<Double> webspeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("WebSpeed")
        .description("Test.")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<Double> effectspeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("EffectSpeed")
        .description("Test.")
        .defaultValue(0.3)
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (BOEntityUtils.isBurrowed(mc.player, !pEndChest.get())) {
            Vec3d vel = PlayerUtils.getHorizontalVelocity(BOEntityUtils.isWebbed(mc.player) ? webspeed.get() : (BOEntityUtils.isAnchor(mc.player) ? AnchorSpeed.get() : mc.player.hasStatusEffect(StatusEffects.SPEED) ? effectspeed.get() : speed.get()));
            double velX = vel.getX();
            double velZ = vel.getZ();

            Anchor anchor = meteordevelopment.meteorclient.systems.modules.Modules.get().get(Anchor.class);
            if (anchor.isActive() && anchor.controlMovement) {
                velX = anchor.deltaX;
                velZ = anchor.deltaZ;
            }

            ((IVec3d) event.movement).set(velX, event.movement.y, velZ);
        }
    }

    @EventHandler
    public void onLivingEntityMove(LivingEntityMoveEvent event) {
        if (event.entity != mc.player) return;
        if (BOEntityUtils.isBurrowed(mc.player, !pEndChest.get())) {
            Vec3d vel = PlayerUtils.getHorizontalVelocity(BOEntityUtils.isWebbed(mc.player) ? webspeed.get() : mc.player.hasStatusEffect(StatusEffects.SPEED) ? effectspeed.get() : speed.get());
            double velX = vel.getX();
            double velZ = vel.getZ();


            Anchor anchor = meteordevelopment.meteorclient.systems.modules.Modules.get().get(Anchor.class);
            if (anchor.isActive() && anchor.controlMovement) {
                velX = anchor.deltaX;
                velZ = anchor.deltaZ;
            }

            ((IVec3d) event.movement).set(velX, event.movement.y, velZ);
        }
    }
}
