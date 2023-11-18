package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.util.Message;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to customize the chat hud.
 * @author Alex811
 */
@Mixin(ChatHud.class)
public class ChatHudMixin {
    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), argsOnly = true)
    public MessageIndicator injectedIndicator(MessageIndicator indicator){
        return Message.Incoming.Customization.mapIndicator(indicator);
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), argsOnly = true)
    public Text injectedIndicator(Text message){
        return Message.Incoming.Customization.mapText(message);
    }
}
