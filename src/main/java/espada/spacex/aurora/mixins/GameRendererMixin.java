package espada.spacex.aurora.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.utils.RenderUtils;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
        opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void render3dHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        RenderUtils.lastProjMat.set(RenderSystem.getProjectionMatrix());
        RenderUtils.lastModMat.set(RenderSystem.getModelViewMatrix());
    }

}
