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
     * @param messageType incoming message type
     * @param sender {@link UUID} of message sender
     * @param message chat message
     */
    public static void onMessage(MessageType messageType, MessageSender sender, String message){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null && (sender == null || !sender.uuid().equals(player.getUuid())) && !Strings.isNullOrEmpty(message))  // player exists & is msg from another client & msg not empty (this will also make it so empty patterns never match incoming messages)
            onIncomingMessage(Message.Channel.fromMessageType(messageType), message);
    }

    /**
     * Called for incoming chat messages (from other people).
     * @param channel incoming message channel
     * @param message chat message
     */
    private static void onIncomingMessage(Message.Channel channel, String message){
        ModConfig.Chat settings = ModConfig.getInstance().chat;
        boolean logInfo = settings.LogMsgInfo;
        if(tryNotify(settings.mention, channel, message, "mention", logInfo)) return;
        if(tryNotify(settings.message, channel, message, "message", logInfo)) return;
        if(logInfo) Log.info("Incoming non-matching message (" + channel + ", " + (settings.message.isCaseSens(channel) ? "case-sens" : "case-insens") + " messages, " + (settings.mention.isCaseSens(channel) ? "case-sens" : "case-insens") + " mentions): " + message);
    }

    /**
     * Gives notification if the settings allow it.
     * @param settings settings to test against and use the sound of
     * @param channel incoming message channel
     * @param message chat message
     * @param type notification type we're trying to match for (used for logging)
     * @param logInfo true to log extra info when the message matches the filter
     * @return true if the player was notified successfully
     */
    private static boolean tryNotify(ModConfig.Chat.Sub settings, Message.Channel channel, String message, String type, boolean logInfo){
        if(!settings.enabled || !settings.relevantPatternMatches(channel, message)) return false;
        settings.playSound();
        if(logInfo) Log.info("Incoming matching message (" + channel + ", " + type + ", " + (settings.isCaseSens(channel) ? "case-sensitive" : "case-insensitive") + "): " + message);
        return true;
    }
}
