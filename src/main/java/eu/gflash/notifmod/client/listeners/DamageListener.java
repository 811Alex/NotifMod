package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.util.Message;
import eu.gflash.notifmod.util.TextUtil;
import eu.gflash.notifmod.config.ModConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;
import java.util.function.Supplier;

/**
 * Handles item damage changes, for items we want to track.
 * @author Alex811
 */
public class DamageListener {
    private static final Map<ItemStackWrapper, TrackedInfo> itemTracker = new HashMap<>();

    public static void onDamage(ItemStack itemStack){
        ModConfig.Durability settings = ModConfig.getInstance().durability;
        if(itemStack.getDamage() == itemStack.getMaxDamage() - 1 && settings.unbreakableItems.contains(itemStack))  // if unbreakable item stopped working
            notify(settings.damageSettings.stop,
                    () -> TextUtil.buildText(
                        Message.CHAT_PRE_WARN,
                        Text.translatable(
                                "msg.notifmod.durability.damage.brokenUnbreakable.long",
                                TextUtil.getWithFormat(itemStack.getName(), Formatting.AQUA))),
                    () -> Text.translatable(
                            "msg.notifmod.durability.damage.brokenUnbreakable.short",
                            TextUtil.getWithFormat(itemStack.getName(), Formatting.AQUA))
            );

        int durabilityPercentage = getDurabilityPercentage(itemStack);
        TrackedInfo itemInfo = itemTracker.get(new ItemStackWrapper(itemStack));
        if(itemInfo == null){   // if not tracked yet, add it
            itemInfo = new TrackedInfo(itemStack);
            itemTracker.put(new ItemStackWrapper(itemStack), itemInfo);
        }else if(!itemInfo.setLastPercentNotif(durabilityPercentage))   // if already tracked & same percentage
            return;

        if(durabilityPercentage <= settings.repairSettings.unlockThreshold)
            itemInfo.setUnlockedRepairNotif(true);
        if(durabilityPercentage > 0 && isPastDmgThreshold(itemStack.getMaxDamage(), durabilityPercentage))
            notify(settings.damageSettings.damage,
                    () -> TextUtil.buildText(
                            Message.CHAT_PRE_WARN,
                            Text.translatable(
                                    "msg.notifmod.durability.damage.percentage.long",
                                    TextUtil.getWithFormat(itemStack.getName(), Formatting.AQUA),
                                    TextUtil.getWithFormat(durabilityPercentage, Formatting.RED))),
                    () -> Text.translatable(
                            "msg.notifmod.durability.damage.percentage.short",
                            TextUtil.getWithFormat(itemStack.getName(), Formatting.AQUA),
                            TextUtil.getWithFormat(durabilityPercentage, Formatting.RED))
            );
    }

    public static void onRepair(ItemStack itemStack){
        ModConfig.Durability settings = ModConfig.getInstance().durability;
        if(!itemStack.isDamaged()){
            TrackedInfo itemInfo = itemTracker.get(new ItemStackWrapper(itemStack));
            if(itemInfo != null && itemInfo.setUnlockedRepairNotif(false))
                notify(settings.repairSettings,
                        () -> TextUtil.buildText(
                                Message.CHAT_PRE_INFO,
                                Text.translatable(
                                        "msg.notifmod.durability.repair.long",
                                        TextUtil.getWithFormat(itemStack.getName(), Formatting.AQUA),
                                        TextUtil.getWithFormat(Text.translatable("msg.notifmod.durability.repair.long.full"), Formatting.GREEN))),
                        () -> Text.translatable(
                                "msg.notifmod.durability.repair.short",
                                TextUtil.getWithFormat(itemStack.getName(), Formatting.AQUA),
                                TextUtil.getWithFormat(Text.translatable("msg.notifmod.durability.repair.short.full"), Formatting.GREEN))
                );
        }
    }

    /**
     * Determines if {@link this#onDamage(ItemStack)} & {@link this#onRepair(ItemStack)} should be called.
     * @param itemStack {@link ItemStack} that just had its damage (only) changed
     * @return true to allow the call
     */
    public static boolean isTracked(ItemStack itemStack){
        ModConfig.Durability settings = ModConfig.getInstance().durability;
        if(!settings.enabled) return false;
        if(settings.trackedItems.contains(itemStack)) return true;
        if(settings.alwaysNamed && itemStack.hasCustomName()) return true;
        return settings.alwaysEnchanted && itemStack.hasEnchantments() && !settings.blacklistedEnchantedItems.contains(itemStack);
    }

    /**
     * Send message and play sound according to the settings.
     * @param settings settings to use
     * @param longMsg regular message {@link Supplier}
     * @param shortMsg message {@link Supplier} to use when there's less available space
     */
    private static void notify(ModConfig.DurabilitySubcategory settings, Supplier<Text> longMsg, Supplier<Text> shortMsg){
        Message.auto(settings.getMsgType(), longMsg, shortMsg);
        if(settings.isSoundEnabled())
            settings.getSoundSequence().play(settings.getVolume());
    }

    /**
     * Determines if the durability percentage is below the selected threshold, in the settings.
     * @param maxDamage max item damage
     * @param durabilityPercentage current item durability percentage
     * @return true if below the selected threshold
     */
    private static boolean isPastDmgThreshold(int maxDamage, int durabilityPercentage){
        ModConfig.DurabilityDamage settings = ModConfig.getInstance().durability.damageSettings;
        if(maxDamage > settings.weakThreshold)
            return durabilityPercentage <= settings.damageThreshold;
        return durabilityPercentage <= settings.weakDamageThreshold;
    }

    private static int getDurabilityPercentage(ItemStack itemStack){
        return 100 - Math.round((100F * itemStack.getDamage()) / itemStack.getMaxDamage());
    }

    /**
     * Wrapper for {@link ItemStack} that allows comparisons required for them to work properly as {@link HashMap} keys.
     * Note: damage differences will be ignored, so different {@link ItemStack} objects, that only differ in damage, would count as equal.
     */
    private record ItemStackWrapper(ItemStack itemStack){
        /**
         * Get copy without any damage.
         * @return undamaged copy
         */
        public ItemStack getNoDmg() {
            ItemStack itemStack = this.itemStack.copy();
            itemStack.setDamage(0);
            return itemStack;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return ItemStack.areEqual(getNoDmg(), ((ItemStackWrapper) o).getNoDmg());
        }

        @Override
        public int hashCode() {
            ItemStack noDmg = getNoDmg();
            return new HashCodeBuilder(19, 61)
                    .append(noDmg.getCount())
                    .append(noDmg.getItem().toString())
                    .append(noDmg.getNbt())
                    .toHashCode();
        }
    }

    /**
     * Stores info that we need to track for various {@link ItemStack} objects.
     */
    private static class TrackedInfo {
        private boolean unlockedRepairNotif;
        private int lastPercentNotif;

        public TrackedInfo(ItemStack itemStack){
            lastPercentNotif = getDurabilityPercentage(itemStack);
            unlockedRepairNotif = lastPercentNotif < ModConfig.getInstance().durability.repairSettings.unlockThreshold;
        }

        public boolean isUnlockedRepairNotif() {
            return unlockedRepairNotif;
        }

        /**
         * Locks/unlocks repair notifications.
         * @param unlockedRepairNotif true to unlock, false to lock
         * @return previous value
         */
        public boolean setUnlockedRepairNotif(boolean unlockedRepairNotif) {
            boolean temp = this.unlockedRepairNotif;
            this.unlockedRepairNotif = unlockedRepairNotif;
            return temp;
        }

        public int getLastPercentNotif() {
            return lastPercentNotif;
        }

        /**
         * Sets the percentage at which we sent the latest notification.
         * @param lastPercentNotif new percentage
         * @return true if there was a change
         */
        public boolean setLastPercentNotif(int lastPercentNotif) {
            if(this.lastPercentNotif != lastPercentNotif){
                this.lastPercentNotif = lastPercentNotif;
                return true;
            }
            return false;
        }
    }
}
