package espada.spacex.aurora.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.autocrystal.AutoCrystal;
import espada.spacex.aurora.utils.RenderUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

public class AttackIndicator extends Modules {
    public AttackIndicator() {
        super(Aurora.EAncillary, "Attack Indicator", "attack animation.");
    }

    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgWhitelist = settings.createGroup("Render Whitelist");

    //--------------------Render--------------------//
    public Setting<Double> lifetime = sgRender.add(new DoubleSetting.Builder()
        .name("Live Time")
        .description("The lifetime of indicator in seconds.")
        .defaultValue(1)
        .min(0)
        .range(0, 10)
        .build()
    );

    //--------------------Render Whitelist--------------------//
    public final Setting<Boolean> renderOnCA = sgWhitelist.add(new BoolSetting.Builder()
        .name("Render On CA")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<ListMode> listMode = sgWhitelist.add(new EnumSetting.Builder<ListMode>()
        .name("List Mode")
        .description("Selection mode.")
        .defaultValue(ListMode.Whitelist)
        .build()
    );
    private final Setting<Set<EntityType<?>>> whitelist = sgWhitelist.add(new EntityTypeListSetting.Builder()
        .name("Whitelist")
        .description("The entities you want to render.")
        .defaultValue(EntityType.END_CRYSTAL)
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .build()
    );
    private final Setting<Set<EntityType<?>>> blacklist = sgWhitelist.add(new EntityTypeListSetting.Builder()
        .name("Blacklist")
        .description("The entities you don't want to render.")
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .build()
    );

    public boolean shouldRender = false;
    private long lastAttackTime = 0;

    public enum ListMode {
        Whitelist,
        Blacklist
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        shouldRender = isRenderEntity(event.entity.getType());
        if (shouldRender) {
            lastAttackTime = System.currentTimeMillis();
        }
    }

    private boolean isRenderEntity(EntityType<?> type) {
        return switch (listMode.get()) {
            case Whitelist -> whitelist.get().contains(type);
            case Blacklist -> !blacklist.get().contains(type);
        };
    }

    public void render(DrawContext context, int width, int height) {
        AutoCrystal autoCrystal = meteordevelopment.meteorclient.systems.modules.Modules.get().get(AutoCrystal.class);
        if ((shouldRender || (autoCrystal.isActive() && renderOnCA.get() && autoCrystal.placePos!= null && isRenderEntity(EntityType.END_CRYSTAL)))) {
            int alpha;
            long currentTime = System.currentTimeMillis();
            long startTime = 0;
            if (shouldRender)
                startTime = lastAttackTime;
            if (autoCrystal.isActive() && autoCrystal.placePos!= null)
                startTime = autoCrystal.lastAttack;

            long timeElapsed = currentTime - startTime;
            if (timeElapsed < 1000) {
                alpha = (int) (255 - (timeElapsed / (lifetime.get() * 1000.0)) * 255);
            } else {
                alpha = 0;
            }

            alpha = MathHelper.clamp(alpha, 0, 255);

            if (alpha > 0) {
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1, 1, 1, alpha / 255f);

                RenderUtils.drawTexture(context, new Identifier("spacex", "hitmarker.png"), (width - 15) / 2, (height - 15) / 2, 15, 15);
            }
        }
    }
}
