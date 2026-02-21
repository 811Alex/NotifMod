package eu.gflash.notifmod.util;

import com.mojang.authlib.GameProfile;
import eu.gflash.notifmod.config.ModConfig;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;

/**
 * Shows messages to this client.
 * Does not send to the server.
 * @author Alex811
 */
public class Message {
    private static final Component CHAT_NAME;
    public static final Component CHAT_PRE_WARN = getPrefix("msg.notifmod.pre.warn", ChatFormatting.RED);
    public static final Component CHAT_PRE_INFO = getPrefix("msg.notifmod.pre.info", ChatFormatting.GREEN);

    static {
        CHAT_NAME = TextUtil.buildText(
                TextUtil.getWithFormat("[", ChatFormatting.GRAY),
                TextUtil.getWithFormat(Component.translatable("msg.notifmod.chat.name"), ChatFormatting.LIGHT_PURPLE),
                TextUtil.getWithFormat("] ", ChatFormatting.GRAY)
        );
    }

    public enum Type {
        NONE, CHAT, ACTIONBAR, TITLE, SUBTITLE;

        @Override
        public String toString() {
            return "text.autoconfig.notifmod.enum.message.type." + this.name().toLowerCase();
        }

        // Message.auto()/autoWithPre() shortcuts
        public void msg(Supplier<Component> msg) {Message.auto(this, msg);}
        public void msg(Supplier<Component> longMsg, Supplier<Component> shortMsg) {Message.auto(this, longMsg, shortMsg);}
        public void msgWithPre(Supplier<Component> msg) {Message.autoWithPre(this, msg);}
        public void msgWithPre(Supplier<Component> longMsg, Supplier<Component> shortMsg) {Message.autoWithPre(this, longMsg, shortMsg);}
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
        private final ChatType.Bound params;
        private final PlayerChatMessage message;
        private final Component msgTxt;
        public NotifType notifType = NotifType.NONE;

        private static Incoming setLatest(Incoming newMsg) {return latest = newMsg;}
        public static Incoming mkNew(GameProfile sender, ChatType.Bound params, PlayerChatMessage message) {return setLatest(new Incoming(sender, params, message));}
        public static Incoming mkNew(ChatType.Bound params, Component message) {return setLatest(new Incoming(params, message));}
        public static Incoming mkNew(Component message) {return setLatest(new Incoming(message));}

        /**
         * @param sender message sender, or null
         * @param params message type parameters, or null
         * @param message original message, or null
         * @param msgTxt {@link Component} representation of the message, or original message if it was originally a {@link Component}.
         */
        private Incoming(GameProfile sender, ChatType.Bound params, PlayerChatMessage message, Component msgTxt) {
            this.sender = sender;
            this.params = params;
            this.message = message;
            this.msgTxt = msgTxt;
        }

        private Incoming(GameProfile sender, ChatType.Bound params, PlayerChatMessage message) {this(sender, params, message, msgToTxt(sender, message));}
        private Incoming(ChatType.Bound params, Component message) {this(null, params, null, message);}
        private Incoming(Component message) {this(null, message);}

        private static Component msgToTxt(GameProfile sender, PlayerChatMessage message){
            if(message == null) return Component.empty();
            if(sender == null) return message.decoratedContent();
            return Optional.ofNullable(message.filterMask())
                    .map(fm -> fm.isEmpty() ? message.decoratedContent() : fm.applyWithFormatting(message.signedContent()))
                    .orElseGet(Component::empty);
        }

        public static Incoming latest() {return latest;}

        public Component getDecorated(){
            return params == null ? msgTxt : params.decorate(msgTxt);
        }

        public Channel channel(){
            if(params == null) return Channel.GAME_INFO;
            return hasSender() ? Channel.CHAT : Channel.SYSTEM;
        }

        public boolean senderIs(Player player){
            return hasSender() && sender.id().equals(player.getUUID());
        }

        public GameProfile getSender() {return sender;}
        public PlayerChatMessage getMessage() {return message;}
        public ChatType.Bound getParams() {return params;}
        public Component getMsgTxt() {return msgTxt;}

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

            public static GuiMessageTag mapIndicator(GuiMessageTag indicator){
                Optional<Integer> color = getSettings()
                        .filter(s -> s.enabled)
                        .map(s -> s.highlighting.indicator)
                        .filter(s -> s.enabled)
                        .map(s -> s.color);
                if(color.isEmpty()) return indicator;
                if(indicator == null) indicator = GuiMessageTag.chatNotSecure();
                return new GuiMessageTag(color.get(), indicator.icon(), indicator.text(), indicator.logTag());
            }

            public static Component mapText(Component message){
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
     * @param msg the {@link Supplier} of the {@link Component} to show.
     */
    public static void auto(Type type, Supplier<Component> msg){
        auto(type, msg, msg);
    }

    /**
     * Shows a message to this client in the way specified by the message {@code type}.
     * @param type where to display the message
     * @param longMsg the {@link Supplier} of the regular {@link Component} to show
     * @param shortMsg the alternative {@link Supplier} to be used when there's less available space
     */
    public static void auto(Type type, Supplier<Component> longMsg, Supplier<Component> shortMsg){
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
     * @param msg the {@link Supplier} of the {@link Component} to show.
     */
    public static void autoWithPre(Type type, Supplier<Component> msg){
        autoWithPre(type, msg, msg);
    }

    /**
     * Same as {@link #auto(Type, Supplier, Supplier)}, but adds {@link #CHAT_PRE_INFO} to {@code longMsg}.
     * @param type where to display the message
     * @param longMsg the {@link Supplier} of the regular {@link Component} to show
     * @param shortMsg the alternative {@link Supplier} to be used when there's less available space
     */
    public static void autoWithPre(Type type, Supplier<Component> longMsg, Supplier<Component> shortMsg){
        auto(type, () -> TextUtil.buildText(Message.CHAT_PRE_INFO, longMsg.get()), shortMsg);
    }

    /**
     * Shows message in the client's chat.
     * @param msg the message to show
     */
    public static void chat(Component msg){
        getPlayer().displayClientMessage(TextUtil.buildText(CHAT_NAME, msg), false);
    }

    /**
     * Shows message in the client's actionbar.
     * @param msg the message to show
     */
    public static void actionBar(Component msg){
        getPlayer().displayClientMessage(msg, true);
    }

    /**
     * Shows message to the client as a title.
     * It also wipes the current subtitle.
     * @param msg the message to show
     */
    public static void title(Component msg){
        getHud().resetTitleTimes();
        getHud().setTitle(msg);
        getHud().setSubtitle(TextUtil.EMPTY);
    }

    /**
     * Shows message to the client as a subtitle.
     * It first wipes the current title.
     * @param msg the message to show
     */
    public static void subTitle(Component msg){
        title(TextUtil.EMPTY);
        getHud().setSubtitle(msg);
    }

    /**
     * Builds chat prefix {@link Component}s.
     * @param langKey the prefix LangKey
     * @param color the prefix color
     * @return the colored prefix, followed by a gray colon
     */
    private static Component getPrefix(String langKey, ChatFormatting color){
        return TextUtil.buildText(
                TextUtil.getWithFormat(Component.translatable(langKey), color),
                TextUtil.getWithFormat(": ", ChatFormatting.GRAY)
        );
    }

    private static Gui getHud(){
        return Minecraft.getInstance().gui;
    }

    private static LocalPlayer getPlayer(){
        return Minecraft.getInstance().player;
    }
}
