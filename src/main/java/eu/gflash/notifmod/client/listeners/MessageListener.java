package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.Log;
import eu.gflash.notifmod.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Handles incoming messages.
 * @author Alex811
 */
public class MessageListener {
    /**
     * Called early for all chat messages.
     * Classifies each message and does additional preprocessing for message customization.
     * @param msg incoming message
     * @return msg for convenience
     */
    public static Message.Incoming onMessagePreprocess(Message.Incoming msg){
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null && (!msg.hasSender() || !msg.senderIs(player)) && !msg.isEmpty())  // player exists & is msg from another client & msg not empty (this will also make it so empty patterns never match incoming messages)
            onIncomingMessagePreprocess(msg.channel(), msg.toString(), msg);
        else msg.notifType = Message.Incoming.NotifType.DROPPED;
        return msg;
    }

    /**
     * Called early for incoming chat messages.
     * Classifies each message and does additional preprocessing for message customization.
     * @param channel the message channel
     * @param message the incoming message text
     * @param msg the incoming message to be classified and preprocessed
     */
    private static void onIncomingMessagePreprocess(Message.Channel channel, String message, Message.Incoming msg){
        ModConfig.Chat settings = ModConfig.getInstance().chat;
        if(procNotifType(settings.mention, channel, message, msg, Message.Incoming.NotifType.MENTION)) return;
        procNotifType(settings.message, channel, message, msg, Message.Incoming.NotifType.MESSAGE);
    }

    /**
     * Called early for incoming chat messages.
     * Checks if an incoming message matches the relevant filter.
     * If it matches, it classifies the message accordingly.
     * @param settings relevant chat settings to match
     * @param channel the message channel
     * @param message the incoming message text
     * @param msg the incoming message to be classified and preprocessed, if it matches
     * @param type the {@link Message.Incoming.NotifType} to assign, if the {@code msg} matches
     * @return true if it matches
     */
    private static boolean procNotifType(ModConfig.Chat.Sub settings, Message.Channel channel, String message, Message.Incoming msg, Message.Incoming.NotifType type){
        if(!settings.enabled || !settings.relevantPatternMatches(channel, message)) return false;
        msg.notifType = type;
        return true;
    }

    /**
     * Called late for chat messages.
     * Processes message and notifies if needed.
     * @param msg message to process and potentially notify for
     */
    public static void onMessage(Message.Incoming msg){
        if(msg.notifType == Message.Incoming.NotifType.DROPPED) return;
        ModConfig.Chat settings = ModConfig.getInstance().chat;
        Message.Channel channel = msg.channel();
        String message = msg.toString();
        boolean logInfo = settings.LogMsgInfo;
        switch(msg.notifType){
            case MENTION -> notify(settings.mention, channel, message, "mention", logInfo);
            case MESSAGE -> notify(settings.message, channel, message, "message", logInfo);
            case NONE -> {if (logInfo) Log.info("Incoming non-matching message (" + channel + ", " + (settings.message.isCaseSens(channel) ? "case-sens" : "case-insens") + " messages, " + (settings.mention.isCaseSens(channel) ? "case-sens" : "case-insens") + " mentions): " + message);}
        }
    }

    /**
     * Called late to give notification for a matching message.
     * @param settings notification's settings
     * @param channel incoming message channel
     * @param message chat message
     * @param type matched notification type
     * @param logInfo true to log extra info for the matching message
     */
    private static void notify(ModConfig.Chat.Sub settings, Message.Channel channel, String message, String type, boolean logInfo){
        settings.playSound();
        if(logInfo) Log.info("Incoming matching message (" + channel + ", " + type + ", " + (settings.isCaseSens(channel) ? "case-sensitive" : "case-insensitive") + "): " + message);
    }
}
