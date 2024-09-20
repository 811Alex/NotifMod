package eu.gflash.notifmod.mixin;

import com.mojang.authlib.GameProfile;
import eu.gflash.notifmod.client.listeners.MessageListener;
import eu.gflash.notifmod.util.Message;
import jakarta.annotation.Nullable;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
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

/**
 * Mixin to observe the chat hud.
 * @author Alex811
 */
@Mixin(MessageHandler.class)
public abstract class InGameHudListenerMixin {
    @Unique private static Message.Incoming lastMsg;

    @Unique
    private static Message.Incoming getLastMsgAndReset(){
        Message.Incoming msg = lastMsg;
        lastMsg = null;
        return msg;
    }

    @Shadow protected abstract void process(@Nullable MessageSignatureData signature, BooleanSupplier processor);

    @Inject(method = "processChatMessageInternal(Lnet/minecraft/network/message/MessageType$Parameters;Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/text/Text;Lcom/mojang/authlib/GameProfile;ZLjava/time/Instant;)Z", at = @At("HEAD"))
    public void onChatMessagePre(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir){
        lastMsg = MessageListener.onMessagePreprocess(Message.Incoming.mkNew(sender, params, message));
    }

    @Inject(method = "processChatMessageInternal(Lnet/minecraft/network/message/MessageType$Parameters;Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/text/Text;Lcom/mojang/authlib/GameProfile;ZLjava/time/Instant;)Z", at = @At("RETURN"))
    public void onChatMessage(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) MessageListener.onMessage(getLastMsgAndReset());
    }

    @Inject(method = "onProfilelessMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"))
    public void onProfilelessMessageReceived(Text content, MessageType.Parameters params, CallbackInfo ci){
        lastMsg = MessageListener.onMessagePreprocess(Message.Incoming.mkNew(params, content)); // temporarily store incoming message at method head
    }

    @Redirect(method = "onProfilelessMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/message/MessageHandler;process(Lnet/minecraft/network/message/MessageSignatureData;Ljava/util/function/BooleanSupplier;)V"))
    public void onProfilelessMessageProcess(MessageHandler instance, MessageSignatureData signature, BooleanSupplier processor){
        Message.Incoming msg = getLastMsgAndReset();                // store message from method's head, to use when it gets processed
        process(signature, () -> {                                  // Inject into processor (important for when a chat delay is set)
            boolean r = processor.getAsBoolean();
            if(msg != null && r) MessageListener.onMessage(msg);
            return r;
        });
    }

    @Inject(method = "onGameMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
    public void onGameMessagePre(Text message, boolean overlay, CallbackInfo ci){
        lastMsg = MessageListener.onMessagePreprocess(Message.Incoming.mkNew(message));
    }

    @Inject(method = "onGameMessage(Lnet/minecraft/text/Text;Z)V", at = @At("RETURN"))
    public void onGameMessage(Text message, boolean overlay, CallbackInfo ci){
        MessageListener.onMessage(getLastMsgAndReset());
    }
}