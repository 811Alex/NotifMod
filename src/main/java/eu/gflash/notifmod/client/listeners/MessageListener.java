package eu.gflash.notifmod.client.listeners;

import com.google.common.base.Strings;
import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.Log;
import eu.gflash.notifmod.util.Message;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.MessageType;

import java.util.UUID;

/**
 * Handles incoming messages.
 * @author Alex811
 */
public class MessageListener {
    /**
     * Called for all chat messages.
     * @param sender {@link UUID} of message sender
     * @param message chat message
     */
    public static void onMessage(MessageType messageType, MessageSender sender, String message){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null && (sender == null || !sender.uuid().equals(player.getUuid())))  // player exists & is game msg or chat msg from another client
            onIncomingMessage(messageType, message);
    }

    /**
     * Called for incoming chat messages (from other people).
     * @param message chat message
     */
    private static void onIncomingMessage(MessageType messageType, String message){
        if(Strings.isNullOrEmpty(message)) return;  // ignore empty, this will also make it so empty patterns never match incoming messages
        ModConfig.Chat settings = ModConfig.getInstance().chat;
        if(settings.LogMsgInfo)
            Log.info("Incoming message (" + Message.Channel.fromMessageType(messageType) + "): " + message);
        if(!tryNotify(settings.mention, messageType, message))
            tryNotify(settings.message, messageType, message);
    }

    /**
     * Gives notification if the settings allow it.
     * @param settings settings to test against and use the sound of
     * @param message chat message
     * @return true if the player was notified successfully
     */
    private static boolean tryNotify(ModConfig.Chat.Sub settings, MessageType messageType, String message){
        if(!settings.enabled || !settings.relevantPatternMatches(messageType, message)) return false;
        settings.playSound();
        return true;
    }
}
