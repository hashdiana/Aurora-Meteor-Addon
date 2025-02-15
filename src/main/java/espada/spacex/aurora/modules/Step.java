package espada.spacex.aurora.modules;

import com.google.common.collect.Streams;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.pathing.PathManagers;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;

import java.util.Comparator;
import java.util.Optional;

public class Step extends Modules {
    public Step() {
        super(Aurora.ExtendMove, "AutoStep", "Allows you to walk up full blocks instantly.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> pauseWeb = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Web")
        .description("Pause when player is stuck by cobweb.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> earth = sgGeneral.add(new BoolSetting.Builder()
        .name("3arthhck-Mode")
        .description("only on ground to step")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("Step height.")
        .defaultValue(1)
        .min(0)
        .build()
    );

    private final Setting<ActiveWhen> activeWhen = sgGeneral.add(new EnumSetting.Builder<ActiveWhen>()
        .name("active-when")
        .description("Step is active when you meet these requirements.")
        .defaultValue(ActiveWhen.Always)
        .build()
    );

    private final Setting<Boolean> safeStep = sgGeneral.add(new BoolSetting.Builder()
        .name("safe-step")
        .description("Doesn't let you step out of a hole if you are low on health or there is a crystal nearby.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> stepHealth = sgGeneral.add(new IntSetting.Builder()
        .name("step-health")
        .description("The health you stop being able to step at.")
        .defaultValue(5)
        .range(1, 36)
        .sliderRange(1, 36)
        .visible(safeStep::get)
        .build()
    );

    public float prevStepHeight;
    public boolean prevPathManagerStep;

    @Override
    public void onActivate() {
        prevStepHeight = mc.player.getStepHeight();

        prevPathManagerStep = PathManagers.get().getSettings().getStep().get();
        PathManagers.get().getSettings().getStep().set(true);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (pauseWeb.get() && BOEntityUtils.isWebbed(mc.player)) return;

        boolean work = (activeWhen.get() == ActiveWhen.Always) || (activeWhen.get() == ActiveWhen.Sneaking && mc.player.isSneaking()) || (activeWhen.get() == ActiveWhen.NotSneaking && !mc.player.isSneaking());
        mc.player.setBoundingBox(mc.player.getBoundingBox().offset(0, 1, 0));
        if (work && (!safeStep.get() || (getHealth() > stepHealth.get() && getHealth() - getExplosionDamage() > stepHealth.get()))) {
            mc.player.setStepHeight(height.get().floatValue());
        } else {
            mc.player.setStepHeight(prevStepHeight);
        }
        if(earth.get() && (mc.player.checkFallFlying())) return;
        mc.player.setBoundingBox(mc.player.getBoundingBox().offset(0, -1, 0));
    }

    @Override
    public void onDeactivate() {
        mc.player.setStepHeight(prevStepHeight);

        PathManagers.get().getSettings().getStep().set(prevPathManagerStep);
    }

    private float getHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    private double getExplosionDamage() {
        Optional<EndCrystalEntity> crystal = Streams.stream(mc.world.getEntities())
            .filter(entity -> entity instanceof EndCrystalEntity)
            .filter(Entity::isAlive)
            .max(Comparator.comparingDouble(o -> DamageUtils.crystalDamage(mc.player, o.getPos())))
            .map(entity -> (EndCrystalEntity) entity);
        return crystal.map(endCrystalEntity -> DamageUtils.crystalDamage(mc.player, endCrystalEntity.getPos())).orElse(0.0);
    }

    public enum ActiveWhen {
        Always,
        Sneaking,
        NotSneaking
    }
}
