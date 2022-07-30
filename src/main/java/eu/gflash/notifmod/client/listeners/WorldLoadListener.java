package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;

/**
 * Handles world loading related events.
 * @author Alex811
 */
public class WorldLoadListener {
    private static int chunkCounter = 0;
    private static boolean notified = false;

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
        if(settings.enabled && ++chunkCounter >= settings.chunks)
            notify(settings);
    }

    /**
     * Resets variables when we disconnect from the server.
     */
    public static void reset(){
        chunkCounter = 0;
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
