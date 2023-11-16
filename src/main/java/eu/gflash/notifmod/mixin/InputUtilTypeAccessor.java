package eu.gflash.notifmod.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin to gain access to {@link InputUtil.Type}'s {@link InputUtil.Key} map, to have a list of valid keys available.
 * @author Alex811
 */
@Mixin(InputUtil.Type.class)
public interface InputUtilTypeAccessor {
    @Accessor Int2ObjectMap<InputUtil.Key> getMap();
}
