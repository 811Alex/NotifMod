package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.WorldLoadListener;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to observe chunk building.
 * @author Alex811
 */
@Mixin(BuiltChunk.class)
public class BuiltChunkMixin {
    @Inject(method = "scheduleRebuild(Lnet/minecraft/client/render/chunk/ChunkBuilder;Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;)V", at = @At("RETURN"))
    public void scheduleRebuild(ChunkBuilder chunkRenderer, ChunkRendererRegionBuilder builder, CallbackInfo ci){
        WorldLoadListener.onChunkBuild();
    }
}
