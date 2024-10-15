package espada.spacex.aurora.mixins;

import espada.spacex.aurora.modules.AttackIndicator;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", shift = At.Shift.AFTER, ordinal = 0), cancellable = true)
    private void onRender(DrawContext context, CallbackInfo ci) {
        AttackIndicator attackIndicator = Modules.get().get(AttackIndicator.class);
        if (attackIndicator.isActive()) {
            attackIndicator.render(context, scaledWidth, scaledHeight);
        }
    }
}
