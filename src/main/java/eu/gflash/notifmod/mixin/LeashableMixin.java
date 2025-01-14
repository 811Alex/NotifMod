package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.LeadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to observe Leashables.
 * @author Alex811
 */
@Mixin(Leashable.class)
public interface LeashableMixin extends Leashable {
    @Shadow @Nullable LeashData getLeashData();

    @Unique private static Identifier getDimId(Entity entity) {return entity.getWorld().getDimension().effects();}

    @Inject(method = "setUnresolvedLeashHolderId(I)V", at = @At("HEAD"))
    default void setUnresolvedLeashHolderId(int unresolvedLeashHolderId, CallbackInfo ci){
        if(unresolvedLeashHolderId != 0) return;                            // if not detach event, abort
        LeashData currLD = getLeashData();
        if(currLD == null || currLD.leashHolder == null) return;            // if no current holder data, abort
        Entity currHolder = currLD.leashHolder;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null || player.getId() != currHolder.getId()) return;  // if player wasn't the holder, abort
        Entity leashed = (Entity) this;
        if(player.distanceTo(leashed) > MAX_LEASH_LENGTH || !leashed.isAlive() || !getDimId(player).equals(getDimId(leashed)))  // if the lead broke, not detached by the player
            LeadListener.onLeadBreak(player, leashed);
    }
}
