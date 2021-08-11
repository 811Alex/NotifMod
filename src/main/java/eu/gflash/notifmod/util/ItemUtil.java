package eu.gflash.notifmod.util;

import eu.gflash.notifmod.config.types.ItemList;

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
}
