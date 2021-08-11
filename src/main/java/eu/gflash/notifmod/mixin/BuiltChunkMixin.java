package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.WorldLoadListener;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to observe chunk building.
 * @author Alex811
 */
@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkMixin {
    @Inject(method = "rebuild()V", at = @At("RETURN"))
    public void rebuild(CallbackInfo ci){
        WorldLoadListener.onChunkBuild();
    }
}
