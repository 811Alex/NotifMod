package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;

/**
 * Handles the events of other players joining/leaving the server.
 * @author Alex811
 */
public class PlayerListListener {
    public static void onJoin(){
        tryNotify(ModConfig.getInstance().playerJoinLeave.join);
    }

    public static void onLeave(){
        tryNotify(ModConfig.getInstance().playerJoinLeave.leave);
    }

    /**
     * Play sound notification if the settings allow it.
     * @param settings settings to use
     */
    private static void tryNotify(ModConfig.SimpleSound settings){
        if(settings.enabled)
            settings.soundSequence.play(settings.volume);
    }
}
