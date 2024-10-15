package espada.spacex.aurora.mixins;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {
    public MixinClientWorld() {}
}
