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
        ModConfig.DoneLoadingWorld settings = ModConfig.getInstance().doneLoading.world;
        if(settings.enabled && settings.chunks <= 0)
            notify(settings);
    }

    /**
     * Gets called when a chunk gets built.
     */
    public static void onChunkBuild(){
        if(notified) return;
        ModConfig.DoneLoadingWorld settings = ModConfig.getInstance().doneLoading.world;
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
    private static void notify(ModConfig.DoneLoadingWorld settings){
        notified = true;
        settings.soundSequence.play(settings.volume);
    }
}
