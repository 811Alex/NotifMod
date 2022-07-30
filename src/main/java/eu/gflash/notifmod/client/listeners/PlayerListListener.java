package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;

/**
 * Handles the events of other players joining/leaving the server.
 * @author Alex811
 */
public class PlayerListListener {
    public static void onJoin(){
        ModConfig.getInstance().playerJoinLeave.join.playSound();
    }

    public static void onLeave(){
        ModConfig.getInstance().playerJoinLeave.leave.playSound();
    }
}
