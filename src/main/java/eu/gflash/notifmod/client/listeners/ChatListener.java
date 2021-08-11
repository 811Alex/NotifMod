package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.UUID;

/**
 * Handles incoming messages.
 * @author Alex811
 */
public class ChatListener {
    /**
     * Called for all chat messages.
     * @param sender {@link UUID} of message sender
     * @param message chat message
     */
    public static void onMessage(UUID sender, String message){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null && !sender.equals(player.getUuid()))  // not this client's player
            onIncomingMessage(message);
    }

    /**
     * Called for incoming chat messages (from other people).
     * @param message chat message
     */
    private static void onIncomingMessage(String message){
        if(!tryNotify(ModConfig.getInstance().chat.mention, message))
            tryNotify(ModConfig.getInstance().chat.message, message);
    }

    /**
     * Gives notification if the settings allow it.
     * @param settings settings to test against and use the sound of
     * @param message chat message
     * @return true if the player was notified successfully
     */
    private static boolean tryNotify(ModConfig.Chat settings, String message){
        if(settings.enabled && settings.regexFilter.matches(message)) {
            settings.soundSequence.play(settings.volume);
            return true;
        }
        return false;
    }
}
