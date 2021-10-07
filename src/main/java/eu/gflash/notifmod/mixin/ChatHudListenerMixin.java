package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.ChatListener;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Mixin to observe the chat hud.
 * @author Alex811
 */
@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {
    @Inject(method = "onChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V", at = @At("RETURN"))
    public void onChatMessage(MessageType messageType, Text message, UUID sender, CallbackInfo ci){
        ChatListener.onMessage(messageType, sender, message.getString());
    }
}
