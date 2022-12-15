package eu.gflash.notifmod.mixin;

import com.mojang.authlib.GameProfile;
import eu.gflash.notifmod.client.listeners.MessageListener;
import eu.gflash.notifmod.util.Message;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    private static Message.Incoming lastProfilelessMsg;

    @Shadow protected abstract void process(@Nullable MessageSignatureData signature, BooleanSupplier processor);

    @Inject(method = "processChatMessageInternal(Lnet/minecraft/network/message/MessageType$Parameters;Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/text/Text;Lcom/mojang/authlib/GameProfile;ZLjava/time/Instant;)Z", at = @At("RETURN"))
    public void onChatMessage(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) MessageListener.onMessage(new Message.Incoming(sender, params, message));
    }

    @Inject(method = "onProfilelessMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"))
    public void onProfilelessMessageReceived(Text content, MessageType.Parameters params, CallbackInfo ci){
        lastProfilelessMsg = new Message.Incoming(params, content); // temporarily store incoming message at method head
    }

    @Redirect(method = "onProfilelessMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/message/MessageHandler;process(Lnet/minecraft/network/message/MessageSignatureData;Ljava/util/function/BooleanSupplier;)V"))
    public void onProfilelessMessageProcess(MessageHandler instance, MessageSignatureData signature, BooleanSupplier processor){
        Message.Incoming msg = lastProfilelessMsg;                  // store message from method's head, to use when it gets processed
        lastProfilelessMsg = null;
        process(signature, () -> {                                  // Inject into processor (important for when a chat delay is set)
            boolean r = processor.getAsBoolean();
            if(msg != null && r) MessageListener.onMessage(msg);
            return r;
        });
    }

    @Inject(method = "onGameMessage(Lnet/minecraft/text/Text;Z)V", at = @At("RETURN"))
    public void onGameMessage(Text message, boolean overlay, CallbackInfo ci){
        MessageListener.onMessage(new Message.Incoming(message));
    }
}