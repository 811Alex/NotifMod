package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.WorldLoadListener;
import eu.gflash.notifmod.client.listeners.DamageListener;
import eu.gflash.notifmod.client.listeners.PlayerListListener;
import eu.gflash.notifmod.client.listeners.WorldTimeListener;
import eu.gflash.notifmod.util.ReminderTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.UUID;

/**
 * Mixin to observe incoming packets.
 * @author Alex811
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    private static final int TICKS_PER_DAY = 24000;
    private static boolean loaded = false;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onPlayerList(Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket;)V", at = @At("RETURN"))
    public void onPlayerList(PlayerListS2CPacket packet, CallbackInfo ci){
        if(!loaded) return; // to skip the first player list sync
        Iterator<PlayerListS2CPacket.Entry> it = packet.getEntries().iterator();
        if(!it.hasNext()) return;
        UUID id = it.next().getProfile().getId();
        PlayerEntity player = this.client.player;
        if(player == null || id.equals(player.getUuid())) return;   // the entry is not for this client's player
        switch(packet.getAction()){
            case ADD_PLAYER -> PlayerListListener.onJoin();
            case REMOVE_PLAYER -> PlayerListListener.onLeave();
        }
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
        PlayerEntity player = this.client.player;
        if(player == null) return;
        ItemStack oldStack = player.playerScreenHandler.getSlot(packet.getSlot()).getStack().copy();
        ItemStack newStack = packet.getItemStack().copy();
        int oldDmg = oldStack.getDamage();
        int newDmg = newStack.getDamage();
        oldStack.setDamage(0);
        newStack.setDamage(0);
        if(ItemStack.areEqual(oldStack, newStack) && oldDmg != newDmg && DamageListener.isTracked(newStack)){   // if it's the same ItemStack with different damage & it's supposed to be tracked
            if(oldDmg < newDmg)
                DamageListener.onDamage(packet.getItemStack());
            else
                DamageListener.onRepair(packet.getItemStack());
        }
    }

    @Inject(method = "onWorldTimeUpdate(Lnet/minecraft/network/packet/s2c/play/WorldTimeUpdateS2CPacket;)V", at = @At("RETURN"))
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci){
        WorldTimeListener.onTimeUpdate((int) (Math.abs(packet.getTimeOfDay()) % TICKS_PER_DAY), this.client.world);    // abs() because if gamerule doDaylightCycle is false, TimeOfDay will be negative
    }
}
