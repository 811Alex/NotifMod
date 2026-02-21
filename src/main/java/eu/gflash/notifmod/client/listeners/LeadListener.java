package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.Message;
import eu.gflash.notifmod.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

/**
 * Handles lead related events.
 * @author Alex811
 */
public class LeadListener {
    public static void onLeadBreak(LocalPlayer player, Entity mob){
        if(!player.isAlive()) return;
        String langKeyPart = mob.isAlive() ? "distance" : "mobDied";
        ModConfig.getInstance().lead.notif(
                () -> TextUtil.buildText(
                        Message.CHAT_PRE_WARN,
                        Component.translatable(
                                "msg.notifmod.lead.break.long." + langKeyPart,
                                TextUtil.getWithFormat(mob.getName(), ChatFormatting.AQUA))),
                () -> Component.translatable("msg.notifmod.lead.break.short." + langKeyPart)
        );
    }
}
