package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.Message;
import eu.gflash.notifmod.util.TextUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * @author Alex811
 */
public class WorldTimeListener {
    private static final int CLEAR_SLEEP_TIME = 12542;
    private static final int RAINY_SLEEP_TIME = 12010;
    private static boolean notified = false;

    public static void reset(){
        notified = false;
    }

    public static void onTimeUpdate(int timeOfDay, World world){
        ModConfig.SleepReminder settings = ModConfig.getInstance().sleepReminder;
        if(!settings.enabled) return;
        if(notified){
            tryResetNotified(timeOfDay, world, settings);
        }else
            tryNotify(timeOfDay, world, settings);
    }

    private static void tryResetNotified(int timeOfDay, World world, ModConfig.SleepReminder settings){
        if(timeOfDay >= getSleepTime(world)) return;
        if(settings.includeThunder){
            if(!world.isThundering()) notified = false;
        }else notified = false;
    }

    private static void tryNotify(int timeOfDay, World world, ModConfig.SleepReminder settings){
        if(timeOfDay >= getSleepTime(world) || (settings.includeThunder && world.isThundering())){
            Message.auto(settings.msgType,
                    () -> TextUtil.buildText(Message.CHAT_PRE_INFO, getMsg()),
                    WorldTimeListener::getMsg
            );
            if(settings.soundEnabled)
                settings.soundSequence.play(settings.volume);
            notified = true;
        }
    }

    private static Text getMsg(){
        return TextUtil.getWithFormat(new TranslatableText("msg.notifmod.sleep"), Formatting.AQUA);
    }

    private static int getSleepTime(World world){
        if(world.isRaining()) return RAINY_SLEEP_TIME;
        return CLEAR_SLEEP_TIME;
    }
}
