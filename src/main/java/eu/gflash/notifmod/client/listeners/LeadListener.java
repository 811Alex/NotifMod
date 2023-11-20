package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.Message;
import eu.gflash.notifmod.util.TextUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Handles lead related events.
 * @author Alex811
 */
public class LeadListener {
    public static void onLeadBreak(ClientPlayerEntity player, Entity mob){
        if(!player.isAlive()) return;
        String langKeyPart = mob.isAlive() ? "distance" : "mobDied";
        ModConfig.Lead settings = ModConfig.getInstance().lead;
        settings.msg(
                () -> TextUtil.buildText(
                        Message.CHAT_PRE_WARN,
                        Text.translatable(
                                "msg.notifmod.lead.break.long." + langKeyPart,
                                TextUtil.getWithFormat(mob.getName(), Formatting.AQUA))),
                () -> Text.translatable("msg.notifmod.lead.break.short." + langKeyPart)
        );
        settings.playSound();
    }
}
