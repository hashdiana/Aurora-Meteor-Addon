package espada.spacex.aurora.mixins;

import espada.spacex.aurora.events.WeatherRenderEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldRenderer.class, priority = 999)
public class MixinWorldRenderer {
    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void onRenderWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo info) {
        WeatherRenderEvent event = MeteorClient.EVENT_BUS.post(WeatherRenderEvent.get(manager,f,d,e,g));
        if (event.isCancelled()) info.cancel();

        if (Modules.get().get(NoRender.class).noWeather()) info.cancel();
    }
}
