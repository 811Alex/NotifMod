package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.LeadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to observe MobEntities.
 * @author Alex811
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends Entity {
    @Shadow private int holdingEntityId;

    public MobEntityMixin(EntityType<?> type, World world) {super(type, world);}

    @Unique private static Identifier getDimId(Entity entity) {return entity.getWorld().getDimension().effects();}

    @Inject(method = "setHoldingEntityId(I)V", at = @At("HEAD"))
    public void setHoldingEntityId(int newHoldingEntityId, CallbackInfo ci){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null || player.getId() != holdingEntityId || newHoldingEntityId > 0) return;
        if(player.distanceTo(this) > 10.0f || !this.isAlive() || !getDimId(player).equals(getDimId(this)))
            LeadListener.onLeadBreak(player, this);
    }
}
