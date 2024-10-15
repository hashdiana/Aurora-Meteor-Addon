package espada.spacex.aurora.mixins;

import espada.spacex.aurora.modules.MultiTasks;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MinecraftClient.class, priority = 1001)
public abstract class MinecraftClientMixin implements IMinecraftClient {
    @ModifyArg(method = "updateWindowTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"))
    private String setTitle(String original) {
        String customTitle = "Welcome use Aurora User version UserName : " + MinecraftClient.getInstance().getSession().getUsername();
        return customTitle;
    }

    @Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    public boolean breakBlock(ClientPlayerEntity clientPlayer) {
        MultiTasks multiTasks = Modules.get().get(MultiTasks.class);
        if (multiTasks.isActive()) {
            return false;
        }
        return clientPlayer.isUsingItem();
    }
}
