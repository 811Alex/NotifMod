package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.MinecraftClient;

import java.util.Optional;

/**
 * Handles world loading related events.
 * @author Alex811
 */
public class WorldLoadListener {
    private static boolean notified = false;

    public static void register(){
        ClientChunkEvents.CHUNK_LOAD.register((s, c) -> WorldLoadListener.onChunkBuild());
    }

    private static int getLoadedChunkCount(){
        return Optional.ofNullable(MinecraftClient.getInstance().world)
                .map(world -> world.getChunkManager().getLoadedChunkCount())
                .orElse(0);
    }

    /**
     * Gets called after a successful connection to a server.
     */
    public static void onLoad(){
        ModConfig.DoneLoading.World settings = ModConfig.getInstance().doneLoading.world;
        if(settings.enabled){
            if(settings.chunks <= 0) notify(settings);
        }else notified = true;  // world load notifications are disabled, exit onChunkBuild() faster
    }

    /**
     * Gets called when a chunk gets built.
     */
    public static void onChunkBuild(){
        if(notified) return;
        ModConfig.DoneLoading.World settings = ModConfig.getInstance().doneLoading.world;
        if(settings.enabled && getLoadedChunkCount() >= settings.chunks)
            notify(settings);
    }

    /**
     * Resets variables when we disconnect from the server.
     */
    public static void reset(){
        notified = false;
    }

    /**
     * Plays sound notification and locks further notifications until we disconnect.
     * @param settings sound notification settings
     */
    private static void notify(ModConfig.DoneLoading.World settings){
        notified = true;
        settings.playSound();
    }
}
