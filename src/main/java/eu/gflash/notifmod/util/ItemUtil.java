package eu.gflash.notifmod.util;

import eu.gflash.notifmod.config.types.ItemList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Item related utility functions.
 * @author Alex811
 */
public class ItemUtil {
    /**
     * Constructs a {@link String}, suitable as an input for {@link ItemList#ItemList(String)},
     * for MC items of the specified tier and types.
     * @param tier the item tier
     * @param types the item types
     * @return the item list {@link String}
     */
    public static String getItems(String tier, String... types){
        return Stream.of(types).map(type -> "minecraft:" + tier + "_" + type).collect(Collectors.joining("; "));
    }

    /**
     * Get all MC armor items of this tier, in a {@link String} suitable as an input for {@link ItemList#ItemList(String)}.
     * @param tier the armor tier
     * @return the item list {@link String}
     */
    public static String getArmor(String tier){
        return getItems(tier, "helmet", "chestplate", "leggings", "boots");
    }

    /**
     * Get all MC tool items of this tier, in a {@link String} suitable as an input for {@link ItemList#ItemList(String)}.
     * @param tier the tool tier
     * @return the item list {@link String}
     */
    public static String getTools(String tier){
        return getItems(tier, "shovel", "pickaxe", "axe", "hoe", "sword");
    }

    /**
     * Check is two {@link ItemStack}s are equal, ignoring their damage.
     * @param left 1st stack
     * @param right 2nd stack
     * @return true if they're equal, ignoring their damage
     */
    public static boolean areEqualIgnoringDmg(ItemStack left, ItemStack right){
        ItemStack leftCopy = left.copy();
        ItemStack rightCopy = right.copy();
        leftCopy.setDamage(0);
        rightCopy.setDamage(0);
        return ItemStack.areEqual(leftCopy, rightCopy);
    }

    /**
     * Get current player's {@link ItemStack} in the specified inventory {@code slot}.
     * @param slot inventory slot to get from
     * @return the {@link ItemStack}, or null if the player entity is null
     */
    @Nullable
    public static ItemStack getPlayerSlotItems(int slot){
        return Optional.ofNullable(MinecraftClient.getInstance().player)
                .map(player -> player.playerScreenHandler.getSlot(slot).getStack())
                .orElse(null);
    }
}
