package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.MessageListener;
import eu.gflash.notifmod.util.Message;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

/**
 * Mixin to observe the chat hud.
 * @author Alex811
 */
@Mixin(MessageHandler.class)
public abstract class InGameHudListenerMixin {
    @Inject(method = "processChatMessage(Lnet/minecraft/network/message/MessageType$Parameters;Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/text/Text;Lnet/minecraft/client/network/PlayerListEntry;ZLjava/time/Instant;)Z", at = @At("RETURN"))
    public void processChatMessage(MessageType.Parameters params, SignedMessage message, Text decorated, PlayerListEntry senderEntry, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) MessageListener.onMessage(new Message.Incoming(senderEntry, params, message));
    }

    @Inject(method = "processProfilelessMessage(Lnet/minecraft/network/message/MessageType$Parameters;Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/text/Text;)Z", at = @At("RETURN"))
    public void processProfilelessMessage(MessageType.Parameters params, SignedMessage message, Text decorated, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) MessageListener.onMessage(new Message.Incoming(params, message));
    }

    @Inject(method = "onGameMessage(Lnet/minecraft/text/Text;Z)V", at = @At("RETURN"))
    public void onGameMessage(Text message, boolean overlay, CallbackInfo ci){
        MessageListener.onMessage(new Message.Incoming(message));
    }
}