package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.Log;
import eu.gflash.notifmod.util.Message;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Handles incoming messages.
 * @author Alex811
 */
public class MessageListener {
    /**
     * Called for all chat messages.
     * @param msg incoming message
     */
    public static void onMessage(Message.Incoming msg){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null && (!msg.hasSender() || !msg.senderIs(player)) && !msg.isEmpty())  // player exists & is msg from another client & msg not empty (this will also make it so empty patterns never match incoming messages)
            onIncomingMessage(msg.channel(), msg.toString());
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
