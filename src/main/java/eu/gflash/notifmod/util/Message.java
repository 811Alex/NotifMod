package eu.gflash.notifmod.util;

import com.mojang.authlib.GameProfile;
import eu.gflash.notifmod.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
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
        NONE, CHAT, ACTIONBAR, TITLE, SUBTITLE;

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
     */
    public static class Incoming {
        public enum NotifType {NONE, DROPPED, MESSAGE, MENTION}

        private static Incoming latest;

        private final GameProfile sender;
        private final MessageType.Parameters params;
        private final SignedMessage message;
        private final Text msgTxt;
        public NotifType notifType = NotifType.NONE;

        private static Incoming setLatest(Incoming newMsg) {return latest = newMsg;}
        public static Incoming mkNew(GameProfile sender, MessageType.Parameters params, SignedMessage message) {return setLatest(new Incoming(sender, params, message));}
        public static Incoming mkNew(MessageType.Parameters params, Text message) {return setLatest(new Incoming(params, message));}
        public static Incoming mkNew(Text message) {return setLatest(new Incoming(message));}

        /**
         * @param sender message sender, or null
         * @param params message type parameters, or null
         * @param message original message, or null
         * @param msgTxt {@link Text} representation of the message, or original message if it was originally a {@link Text}.
         */
        private Incoming(GameProfile sender, MessageType.Parameters params, SignedMessage message, Text msgTxt) {
            this.sender = sender;
            this.params = params;
            this.message = message;
            this.msgTxt = msgTxt;
        }

        private Incoming(GameProfile sender, MessageType.Parameters params, SignedMessage message) {this(sender, params, message, msgToTxt(sender, message));}
        private Incoming(MessageType.Parameters params, Text message) {this(null, params, null, message);}
        private Incoming(Text message) {this(null, message);}

        private static Text msgToTxt(GameProfile sender, SignedMessage message){
            if(message == null) return Text.empty();
            if(sender == null) return message.getContent();
            return Optional.ofNullable(message.filterMask())
                    .map(fm -> fm.isPassThrough() ? message.getContent() : fm.getFilteredText(message.getSignedContent()))
                    .orElseGet(Text::empty);
        }

        public static Incoming latest() {return latest;}

        public Text getDecorated(){
            return params == null ? msgTxt : params.applyChatDecoration(msgTxt);
        }

        public Channel channel(){
            if(params == null) return Channel.GAME_INFO;
            return hasSender() ? Channel.CHAT : Channel.SYSTEM;
        }

        public boolean senderIs(PlayerEntity player){
            return hasSender() && sender.id().equals(player.getUuid());
        }

        public GameProfile getSender() {return sender;}
        public SignedMessage getMessage() {return message;}
        public MessageType.Parameters getParams() {return params;}
        public Text getMsgTxt() {return msgTxt;}

        @Override
        public String toString() {return msgTxt.getString();}
        public boolean isEmpty() {return toString().isEmpty();}
        public boolean hasSender() {return sender != null;}

        public static class Customization{
            private static Optional<ModConfig.Chat.Sub> getSettings(){
                ModConfig.Chat settings = ModConfig.getInstance().chat;
                return Optional.ofNullable(latest()).map(latest -> switch(latest.notifType){
                    case MENTION -> settings.mention;
                    case MESSAGE -> settings.message;
                    default -> null;
                });
            }

            public static MessageIndicator mapIndicator(MessageIndicator indicator){
                Optional<Integer> color = getSettings()
                        .filter(s -> s.enabled)
                        .map(s -> s.highlighting.indicator)
                        .filter(s -> s.enabled)
                        .map(s -> s.color);
                if(color.isEmpty()) return indicator;
                if(indicator == null) indicator = MessageIndicator.notSecure();
                return new MessageIndicator(color.get(), indicator.icon(), indicator.text(), indicator.loggedName());
            }

            public static Text mapText(Text message){
                return getSettings()
                        .filter(s -> s.enabled)
                        .map(s -> s.highlighting.matchedTextStyle)
                        .filter(ModConfig.Chat.Sub.Highlighting.MatchedTextStyle::isEnabled)
                        .map(s -> s.fill(message))
                        .orElse(message);
            }
        }
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
