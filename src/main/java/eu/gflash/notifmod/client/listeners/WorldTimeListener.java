package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.TextUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

/**
 * @author Alex811
 */
public class WorldTimeListener {
    private static final Text SLEEP_MSG = TextUtil.getWithFormat(Text.translatable("msg.notifmod.sleep"), Formatting.AQUA);
    private static final int CLEAR_SLEEP_TIME = 12542;
    private static final int RAINY_SLEEP_TIME = 12010;
    private static boolean notified = false;
    private static long lastNotifTime = 0L;

    public static void reset(){
        notified = false;
        lastNotifTime = 0L;
    }

    public static void onTimeUpdate(int timeOfDay, World world, PlayerEntity player, boolean isLoading){
        if(isLoading) return;
        ModConfig.SleepReminder settings = ModConfig.getInstance().sleepReminder;
        if(!settings.enabled || (System.currentTimeMillis() - lastNotifTime) < (settings.conditions.cooldown * 1000L)) return;
        if(notified) tryResetNotified(timeOfDay, world, player, settings);
        else tryNotify(timeOfDay, world, player, settings);
    }

    private static void tryResetNotified(int timeOfDay, World world, PlayerEntity player, ModConfig.SleepReminder settings){
        if(noNotify(timeOfDay, world, player, settings)) notified = false;
    }

    private static void tryNotify(int timeOfDay, World world, PlayerEntity player, ModConfig.SleepReminder settings){
        if(noNotify(timeOfDay, world, player, settings)) return;
        settings.notifWithPre(() -> SLEEP_MSG);
        notified = true;
        lastNotifTime = System.currentTimeMillis();
    }

    private static boolean noNotify(int timeOfDay, World world, PlayerEntity player, ModConfig.SleepReminder settings){
        ModConfig.SleepReminder.Conditions cSettings = settings.conditions;
        if(world.getDimension().hasFixedTime() && cSettings.pauseInTimelessDims) return true;
        if(timeOfDay < getSleepTime(world) && !(settings.includeThunder && world.isThundering())) return true;
        if(!cSettings.pauseUnderground) return false;
        BlockPos pos = player.getBlockPos();
        return pos.getY() < cSettings.minAltitude && getSkyLL(world, pos) < cSettings.minSkyLight;
    }

    private static int getSleepTime(World world){
        if(world.isRaining()) return RAINY_SLEEP_TIME;
        return CLEAR_SLEEP_TIME;
    }

    private static int getSkyLL(World world, BlockPos pos){
        return world.getLightLevel(LightType.SKY, pos);
    }
}
