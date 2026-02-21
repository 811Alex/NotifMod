package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.util.Message;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to customize the chat hud.
 * @author Alex811
 */
@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true)
    public GuiMessageTag injectedIndicator(GuiMessageTag indicator){
        return Message.Incoming.Customization.mapIndicator(indicator);
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true)
    public Component injectedIndicator(Component message){
        return Message.Incoming.Customization.mapText(message);
    }
}
