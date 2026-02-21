package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import java.util.Optional;
import net.minecraft.client.multiplayer.PlayerInfo;

/**
 * Handles the events of other players joining/leaving the server.
 * @author Alex811
 */
public class PlayerListListener {
    public static void onJoin(PlayerInfo profile){
        ModConfig.PlayerJoinLeave settings = ModConfig.getInstance().playerJoinLeave;
        tryNotify(profile, settings, settings.join);
    }

    public static void onLeave(PlayerInfo profile){
        ModConfig.PlayerJoinLeave settings = ModConfig.getInstance().playerJoinLeave;
        tryNotify(profile, settings, settings.leave);
    }

    public static void tryNotify(PlayerInfo profile, ModConfig.PlayerJoinLeave settings, ModConfig.AudibleNotif notif){
        if(settings.filters.no0Latency && profile.getLatency() < 1) return;
        if(Optional.ofNullable(profile.getTeam()).filter(team -> team.getName().startsWith("[ZNPC]")).isPresent()) return;    // ZNPC patch
        notif.playSound();
    }
}
