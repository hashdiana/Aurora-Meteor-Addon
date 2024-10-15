package espada.spacex.aurora.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.events.Render2DEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        Render2DEvent event = MeteorClient.EVENT_BUS.post(Render2DEvent.Render2DEvent(context));
        RenderSystem.applyModelViewMatrix();
    }


}
