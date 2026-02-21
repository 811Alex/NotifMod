package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.DamageListener;
import eu.gflash.notifmod.client.listeners.PlayerListListener;
import eu.gflash.notifmod.client.listeners.WorldLoadListener;
import eu.gflash.notifmod.client.listeners.WorldTimeListener;
import eu.gflash.notifmod.util.ItemUtil;
import eu.gflash.notifmod.util.ReminderTimer;
import eu.gflash.notifmod.util.ThreadUtils;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.world.item.ItemStack;

/**
 * Mixin to observe incoming packets.
 * @author Alex811
 */
@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Unique private static final int TICKS_PER_DAY = 24000;
    @Unique private static boolean loaded = false;

    @Unique private Minecraft getClient() {return Minecraft.getInstance();}

    @Shadow @Final private Map<UUID, PlayerInfo> playerInfoMap;
    @Shadow @Final private Set<PlayerInfo> listedPlayers;

    @Inject(method = "applyPlayerInfoUpdate(Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket$Action;Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket$Entry;Lnet/minecraft/client/multiplayer/PlayerInfo;)V", at = @At("HEAD"))
    public void onPlayerListAction(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry receivedEntry, PlayerInfo currentEntry, CallbackInfo ci){
        if(!loaded || action != ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) return;   // to skip the first player list sync & other actions
        if(receivedEntry.listed()) PlayerListListener.onJoin(currentEntry);
    }

    @Inject(method = "handlePlayerInfoRemove(Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoRemovePacket;)V", at = @At("HEAD"))
    public void onPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci){
        if(ThreadUtils.isMainThread())                          // must be main thread ('cause this gets executed multiple times)
            packet.profileIds().stream()
                    .filter(playerInfoMap::containsKey)     // if current player list contains received ID
                    .map(playerInfoMap::get)
                    .filter(listedPlayers::contains)  // & player entry is listed
                    .findFirst()
                    .ifPresent(PlayerListListener::onLeave);    // notify
    }

    @Inject(method = "handleUpdateAdvancementsPacket(Lnet/minecraft/network/protocol/game/ClientboundUpdateAdvancementsPacket;)V", at = @At("RETURN"))
    public void onAdvancements(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci){
        if(!loaded){
            loaded = true;  // consider the world loaded, after the advancement sync (it's one of the last things to happen when connecting)
            WorldLoadListener.onLoad();
        }
    }

    @Inject(method = "clearLevel()V", at = @At("RETURN"))
    public void clearWorld(CallbackInfo ci){
        loaded = false;
        WorldLoadListener.reset();
        WorldTimeListener.reset();
        ReminderTimer.killAll();
    }

    @Inject(method = "handleContainerSetSlot(Lnet/minecraft/network/protocol/game/ClientboundContainerSetSlotPacket;)V", at = @At("HEAD"))
    public void onScreenHandlerSlotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci){
        if(packet.getContainerId() != 0 || !ThreadUtils.isMainThread()) return;   // stop if in a GUI or not on the main thread (since this fires in multiple threads)
        ItemStack oldStack = ItemUtil.getPlayerSlotItems(packet.getSlot());
        ItemStack newStack = packet.getItem();
        if(oldStack == null) return;                                        // happens if player == null
        int oldDmg = oldStack.getDamageValue();
        int newDmg = newStack.getDamageValue();
        if(ItemUtil.areEqualIgnoringDmg(oldStack, newStack) && oldDmg != newDmg && DamageListener.isTracked(newStack)){   // if it's the same ItemStack with different damage & it's supposed to be tracked
            if(oldDmg < newDmg) DamageListener.onDamage(newStack);
            else DamageListener.onRepair(newStack);
        }
    }

    @Inject(method = "handleSetTime(Lnet/minecraft/network/protocol/game/ClientboundSetTimePacket;)V", at = @At("RETURN"))
    public void onWorldTimeUpdate(ClientboundSetTimePacket packet, CallbackInfo ci){
        Minecraft mc = getClient();
        if(mc.screen instanceof LevelLoadingScreen) return;
        WorldTimeListener.onTimeUpdate((int) (Math.abs(packet.dayTime()) % TICKS_PER_DAY), mc.level, mc.player);  // abs() because if gamerule doDaylightCycle is false, TimeOfDay will be negative
    }
}
