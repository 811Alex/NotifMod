package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.MessageListener;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to observe the chat hud.
 * @author Alex811
 */
@Mixin(InGameHud.class)
public class InGameHudListenerMixin {
    @Inject(method = "onChatMessage(Lnet/minecraft/network/message/MessageType;Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSender;)V", at = @At("RETURN"))
    public void onChatMessage(MessageType messageType, Text message, MessageSender sender, CallbackInfo ci){
        MessageListener.onMessage(messageType, sender, message.getString());
    }

    @Inject(method = "onGameMessage(Lnet/minecraft/network/message/MessageType;Lnet/minecraft/text/Text;)V", at = @At("RETURN"))
    public void onGameMessage(MessageType messageType, Text message, CallbackInfo ci){
        MessageListener.onMessage(messageType, null, message.getString());
    }
}