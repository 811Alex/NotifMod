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
    @Shadow @Nullable Entity getLeashHolder();

    @Shadow boolean canBeLeashedTo(Entity entity);

    @Unique private static Identifier getDimId(Entity entity) {return entity.getEntityWorld().getDimension().effects();}

    @Unique private boolean isInDimOf(Entity entity) {return this instanceof Entity thisEntity && getDimId(entity).equals(getDimId(thisEntity));}

    @Inject(method = "setUnresolvedLeashHolderId(I)V", at = @At("HEAD"))
    default void setUnresolvedLeashHolderId(int unresolvedLeashHolderId, CallbackInfo ci){
        if(unresolvedLeashHolderId != 0) return;                    // if not detach event, abort
        Entity leashHolder = getLeashHolder();
        if(leashHolder == null) return;                             // if no current holder data, abort
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(leashHolder.equals(player) && this instanceof Entity leashedEntity && (  // if player was the holder
                !leashedEntity.isAlive() ||                                         // notify if leashed died
                !isInDimOf(player) ||                                               // notify if they're not in the same dimension
                !canBeLeashedTo(player) ||                                          // notify if it definitely snapped due to distance
                !player.canInteractWithEntity(leashedEntity, 0)       // notify if this couldn't have been a leashed entity interaction detach
        )) LeadListener.onLeadBreak(player, leashedEntity);         // note: vanilla doesn't call this for block interactions (leashing to fence) - update if that changes
    }
}
