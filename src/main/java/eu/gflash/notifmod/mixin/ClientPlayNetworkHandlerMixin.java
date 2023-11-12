package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.WorldLoadListener;
import eu.gflash.notifmod.client.listeners.DamageListener;
import eu.gflash.notifmod.client.listeners.PlayerListListener;
import eu.gflash.notifmod.client.listeners.WorldTimeListener;
import eu.gflash.notifmod.util.ReminderTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Mixin to observe incoming packets.
 * @author Alex811
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    private static final int TICKS_PER_DAY = 24000;
    private static boolean loaded = false;

    @Unique private MinecraftClient getClient() {return MinecraftClient.getInstance();}

    @Shadow @Final private Map<UUID, PlayerListEntry> playerListEntries;
    @Shadow @Final private Set<PlayerListEntry> listedPlayerListEntries;

    @Inject(method = "handlePlayerListAction(Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Action;Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Entry;Lnet/minecraft/client/network/PlayerListEntry;)V", at = @At("HEAD"))
    public void onPlayerListAction(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry, CallbackInfo ci){
        if(!loaded || action != PlayerListS2CPacket.Action.ADD_PLAYER) return;   // to skip the first player list sync & other actions
        if(receivedEntry.listed()) PlayerListListener.onJoin(currentEntry);
    }

    @Inject(method = "onPlayerRemove(Lnet/minecraft/network/packet/s2c/play/PlayerRemoveS2CPacket;)V", at = @At("HEAD"))
    public void onPlayerRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci){
        if(getClient().isOnThread())   // must be main thread ('cause this gets executed multiple times)
            packet.profileIds().stream()
                    .filter(playerListEntries::containsKey)         // if current player list contains received ID
                    .map(playerListEntries::get)
                    .filter(listedPlayerListEntries::contains)      // & player entry is listed
                    .findFirst()
                    .ifPresent(PlayerListListener::onLeave);        // notify
    }

    @Inject(method = "onAdvancements(Lnet/minecraft/network/packet/s2c/play/AdvancementUpdateS2CPacket;)V", at = @At("RETURN"))
    public void onAdvancements(AdvancementUpdateS2CPacket packet, CallbackInfo ci){
        if(!loaded){
            loaded = true;  // consider the world loaded, after the advancement sync (it's one of the last things to happen when connecting)
            WorldLoadListener.onLoad();
        }
    }

    @Inject(method = "clearWorld()V", at = @At("RETURN"))
    public void clearWorld(CallbackInfo ci){
        loaded = false;
        WorldLoadListener.reset();
        WorldTimeListener.reset();
        ReminderTimer.killAll();
    }

    @Inject(method = "onScreenHandlerSlotUpdate(Lnet/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket;)V", at = @At("HEAD"))
    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci){
        if(packet.getSyncId() != 0 || Thread.currentThread().getName().equals("Render thread")) return; // stop if in a GUI or the render thread (since this fires there too)
        PlayerEntity player = getClient().player;
        if(player == null) return;
        ItemStack oldStack = player.playerScreenHandler.getSlot(packet.getSlot()).getStack().copy();
        ItemStack newStack = packet.getStack().copy();
        int oldDmg = oldStack.getDamage();
        int newDmg = newStack.getDamage();
        oldStack.setDamage(0);
        newStack.setDamage(0);
        if(ItemStack.areEqual(oldStack, newStack) && oldDmg != newDmg && DamageListener.isTracked(newStack)){   // if it's the same ItemStack with different damage & it's supposed to be tracked
            if(oldDmg < newDmg)
                DamageListener.onDamage(packet.getStack());
            else
                DamageListener.onRepair(packet.getStack());
        }
    }

    @Inject(method = "onWorldTimeUpdate(Lnet/minecraft/network/packet/s2c/play/WorldTimeUpdateS2CPacket;)V", at = @At("RETURN"))
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci){
        MinecraftClient mc = getClient();
        WorldTimeListener.onTimeUpdate((int) (Math.abs(packet.getTimeOfDay()) % TICKS_PER_DAY), mc.world, mc.player, mc.currentScreen instanceof DownloadingTerrainScreen);    // abs() because if gamerule doDaylightCycle is false, TimeOfDay will be negative
    }
}
