package espada.spacex.aurora.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

    public class KillEffects extends Modules {
    public KillEffects() {
        super(Aurora.AURORA, "Kill Effects", "Render some things where enemy died.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description(".")
        .defaultValue(Mode.LightningBolt)
        .build()
    );
    private final Setting<Boolean> playSound = sgGeneral.add(new BoolSetting.Builder()
        .name("Play Sound")
        .description(".")
        .defaultValue(true)
        .visible(() -> !mode.get().equals(Mode.FallingLava))
        .build()
    );

    private final Map<Entity, Long> renderEntities = new ConcurrentHashMap<>();
    private final Map<Entity, Long> lightingEntities = new ConcurrentHashMap<>();

    public enum Mode {
        FallingLava,
        LightningBolt
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        switch (mode.get()) {
            case FallingLava -> renderEntities.keySet().forEach(entity -> {
                for (int i = 0; i < entity.getHeight() * 10; i++) {
                    for (int j = 0; j < entity.getWidth() * 10; j++) {
                        for (int k = 0; k < entity.getWidth() * 10; k++) {
                            mc.world.addParticle(ParticleTypes.FALLING_LAVA, entity.getX() + j * 0.1, entity.getY() + i * 0.1, entity.getZ() + k * 0.1, 0, 0, 0);
                        }
                    }
                }

                renderEntities.remove(entity);
            });
            case LightningBolt -> renderEntities.forEach((entity, time) -> {
                LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                lightningEntity.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                EntitySpawnS2CPacket pac = new EntitySpawnS2CPacket(lightningEntity);
                pac.apply(mc.getNetworkHandler());

                if (playSound.get()) {
                    mc.world.playSound(mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F * 0.2F);
                    mc.world.playSound(mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F * 0.2F);
                }

                renderEntities.remove(entity);
                lightingEntities.put(entity, System.currentTimeMillis());
            });
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity)) return;
            if (entity == mc.player || renderEntities.containsKey(entity) || lightingEntities.containsKey(entity))
                return;
            if (entity.isAlive() || ((PlayerEntity) entity).getHealth() != 0) return;
            renderEntities.put(entity, System.currentTimeMillis());
        });

        if (!lightingEntities.isEmpty()) {
            lightingEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 0) {
                    lightingEntities.remove(entity);
                }
            });
        }
    }
}
