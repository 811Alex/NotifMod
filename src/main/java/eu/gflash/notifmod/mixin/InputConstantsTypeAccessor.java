package eu.gflash.notifmod.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin to gain access to {@link InputConstants.Type}'s {@link InputConstants.Key} map, to have a list of valid keys available.
 * @author Alex811
 */
@Mixin(InputConstants.Type.class)
public interface InputConstantsTypeAccessor {
    @Accessor Int2ObjectMap<InputConstants.Key> getMap();
}
