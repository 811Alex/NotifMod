package eu.gflash.notifmod.mixin;

import com.mojang.authlib.GameProfile;
import eu.gflash.notifmod.client.listeners.MessageListener;
import eu.gflash.notifmod.util.Message;
import jakarta.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.util.function.BooleanSupplier;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;

/**
 * Mixin to observe the chat hud.
 * @author Alex811
 */
@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @Unique private static Message.Incoming lastMsg;

    @Unique
    private static Message.Incoming getLastMsgAndReset(){
        Message.Incoming msg = lastMsg;
        lastMsg = null;
        return msg;
    }

    @Shadow protected abstract void handleMessage(@Nullable MessageSignature signature, BooleanSupplier processor);

    @Inject(method = "showMessageToPlayer(Lnet/minecraft/network/chat/ChatType$Bound;Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/network/chat/Component;Lcom/mojang/authlib/GameProfile;ZLjava/time/Instant;)Z", at = @At("HEAD"))
    public void onChatMessagePre(ChatType.Bound params, PlayerChatMessage message, Component decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir){
        lastMsg = MessageListener.onMessagePreprocess(Message.Incoming.mkNew(sender, params, message));
    }

    @Inject(method = "showMessageToPlayer(Lnet/minecraft/network/chat/ChatType$Bound;Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/network/chat/Component;Lcom/mojang/authlib/GameProfile;ZLjava/time/Instant;)Z", at = @At("RETURN"))
    public void onChatMessage(ChatType.Bound params, PlayerChatMessage message, Component decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) MessageListener.onMessage(getLastMsgAndReset());
    }

    @Inject(method = "handleDisguisedChatMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"))
    public void onProfilelessMessageReceived(Component content, ChatType.Bound params, CallbackInfo ci){
        lastMsg = MessageListener.onMessagePreprocess(Message.Incoming.mkNew(params, content)); // temporarily store incoming message at method head
    }

    @Redirect(method = "handleDisguisedChatMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleMessage(Lnet/minecraft/network/chat/MessageSignature;Ljava/util/function/BooleanSupplier;)V"))
    public void onProfilelessMessageProcess(ChatListener instance, MessageSignature signature, BooleanSupplier processor){
        Message.Incoming msg = getLastMsgAndReset();                // store message from method's head, to use when it gets processed
        handleMessage(signature, () -> {                                  // Inject into processor (important for when a chat delay is set)
            boolean r = processor.getAsBoolean();
            if(msg != null && r) MessageListener.onMessage(msg);
            return r;
        });
    }

    @Inject(method = "handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"))
    public void onGameMessagePre(Component message, boolean overlay, CallbackInfo ci){
        lastMsg = MessageListener.onMessagePreprocess(Message.Incoming.mkNew(message));
    }

    @Inject(method = "handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("RETURN"))
    public void onGameMessage(Component message, boolean overlay, CallbackInfo ci){
        MessageListener.onMessage(getLastMsgAndReset());
    }
}