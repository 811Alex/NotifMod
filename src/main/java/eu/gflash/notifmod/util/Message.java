package eu.gflash.notifmod.util;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Shows messages to this client.
 * Does not send to the server.
 * @author Alex811
 */
public class Message {
    private static final Text CHAT_NAME;
    public static final Text CHAT_PRE_WARN = getPrefix("msg.notifmod.pre.warn", Formatting.RED);
    public static final Text CHAT_PRE_INFO = getPrefix("msg.notifmod.pre.info", Formatting.GREEN);

    static {
        CHAT_NAME = TextUtil.buildText(
                TextUtil.getWithFormat("[", Formatting.GRAY),
                TextUtil.getWithFormat(Text.translatable("msg.notifmod.chat.name"), Formatting.LIGHT_PURPLE),
                TextUtil.getWithFormat("] ", Formatting.GRAY)
        );
    }

    public enum Type {
        CHAT, ACTIONBAR, TITLE, SUBTITLE;

        @Override
        public String toString() {
            return "text.autoconfig.notifmod.enum.message.type." + this.name().toLowerCase();
        }

        // Message.auto()/autoWithPre() shortcuts
        public void msg(Supplier<Text> msg) {Message.auto(this, msg);}
        public void msg(Supplier<Text> longMsg, Supplier<Text> shortMsg) {Message.auto(this, longMsg, shortMsg);}
        public void msgWithPre(Supplier<Text> msg) {Message.autoWithPre(this, msg);}
        public void msgWithPre(Supplier<Text> longMsg, Supplier<Text> shortMsg) {Message.autoWithPre(this, longMsg, shortMsg);}
    }

    public enum ChannelCombo {  // Channel config settings
        NONE, CHAT, SYSTEM, CHAT_SYSTEM, GAMEINFO, CHAT_GAMEINFO, SYSTEM_GAMEINFO, ALL;

        @Override
        public String toString() {
            return "text.autoconfig.notifmod.enum.message.channelCombo." + this.name().toLowerCase();
        }
    }

    public enum Channel {
        CHAT, SYSTEM, GAME_INFO;

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace('_', ' ');
        }
    }

    /**
     * Incoming message + meta wrapper.
     * @param sender message sender, or null
     * @param params message type parameters, or null
     * @param message original message, or null
     * @param msgTxt {@link Text} representation of the message, or original message if it was originally a {@link Text}.
     */
    public record Incoming(GameProfile sender, MessageType.Parameters params, SignedMessage message, Text msgTxt){
        public Incoming(GameProfile sender, MessageType.Parameters params, SignedMessage message) {this(sender, params, message, msgToTxt(sender, message));}
        public Incoming(MessageType.Parameters params, Text message) {this(null, params, null, message);}
        public Incoming(Text message) {this(null, message);}

        private static Text msgToTxt(GameProfile sender, SignedMessage message){
            if(message == null) return Text.empty();
            if(sender == null) return message.getContent();
            return Optional.ofNullable(message.filterMask())
                    .map(fm -> fm.isPassThrough() ? message.getContent() : fm.getFilteredText(message.getSignedContent()))
                    .orElseGet(Text::empty);
        }

        public Text getDecorated(){
            return params == null ? msgTxt : params.applyChatDecoration(msgTxt);
        }

        public Channel channel(){
            if(params == null) return Channel.GAME_INFO;
            return hasSender() ? Channel.CHAT : Channel.SYSTEM;
        }

        public boolean senderIs(PlayerEntity player){
            return hasSender() && sender.getId().equals(player.getUuid());
        }

        @Override
        public String toString() {return msgTxt.getString();}
        public boolean isEmpty() {return toString().isEmpty();}
        public boolean hasSender() {return sender != null;}
    }

    /**
     * Same as {@link this#auto(Type, Supplier, Supplier)} but sets both message {@link Supplier}s to be the same.
     * @param type message type
     * @param msg the {@link Supplier} of the {@link Text} to show.
     */
    public static void auto(Type type, Supplier<Text> msg){
        auto(type, msg, msg);
    }

    /**
     * Shows a message to this client in the way specified by the message {@code type}.
     * @param type where to display the message
     * @param longMsg the {@link Supplier} of the regular {@link Text} to show
     * @param shortMsg the alternative {@link Supplier} to be used when there's less available space
     */
    public static void auto(Type type, Supplier<Text> longMsg, Supplier<Text> shortMsg){
        switch(type){
            case CHAT -> chat(longMsg.get());
            case ACTIONBAR -> actionBar(longMsg.get());
            case TITLE -> title(shortMsg.get());
            case SUBTITLE -> subTitle(longMsg.get());
        }
    }

    /**
     * Same as {@link #auto(Type, Supplier)}, but adds {@link #CHAT_PRE_INFO} to {@code msg}, when the long form is used.
     * @param type where to display the message
     * @param msg the {@link Supplier} of the {@link Text} to show.
     */
    public static void autoWithPre(Type type, Supplier<Text> msg){
        autoWithPre(type, msg, msg);
    }

    /**
     * Same as {@link #auto(Type, Supplier, Supplier)}, but adds {@link #CHAT_PRE_INFO} to {@code longMsg}.
     * @param type where to display the message
     * @param longMsg the {@link Supplier} of the regular {@link Text} to show
     * @param shortMsg the alternative {@link Supplier} to be used when there's less available space
     */
    public static void autoWithPre(Type type, Supplier<Text> longMsg, Supplier<Text> shortMsg){
        auto(type, () -> TextUtil.buildText(Message.CHAT_PRE_INFO, longMsg.get()), shortMsg);
    }

    /**
     * Shows message in the client's chat.
     * @param msg the message to show
     */
    public static void chat(Text msg){
        getPlayer().sendMessage(TextUtil.buildText(CHAT_NAME, msg), false);
    }

    /**
     * Shows message in the client's actionbar.
     * @param msg the message to show
     */
    public static void actionBar(Text msg){
        getPlayer().sendMessage(msg, true);
    }

    /**
     * Shows message to the client as a title.
     * It also wipes the current subtitle.
     * @param msg the message to show
     */
    public static void title(Text msg){
        getHud().setDefaultTitleFade();
        getHud().setTitle(msg);
        getHud().setSubtitle(TextUtil.EMPTY);
    }

    /**
     * Shows message to the client as a subtitle.
     * It first wipes the current title.
     * @param msg the message to show
     */
    public static void subTitle(Text msg){
        title(TextUtil.EMPTY);
        getHud().setSubtitle(msg);
    }

    /**
     * Builds chat prefix {@link Text}s.
     * @param langKey the prefix LangKey
     * @param color the prefix color
     * @return the colored prefix, followed by a gray colon
     */
    private static Text getPrefix(String langKey, Formatting color){
        return TextUtil.buildText(
                TextUtil.getWithFormat(Text.translatable(langKey), color),
                TextUtil.getWithFormat(": ", Formatting.GRAY)
        );
    }

    private static InGameHud getHud(){
        return MinecraftClient.getInstance().inGameHud;
    }

    private static ClientPlayerEntity getPlayer(){
        return MinecraftClient.getInstance().player;
    }
}
