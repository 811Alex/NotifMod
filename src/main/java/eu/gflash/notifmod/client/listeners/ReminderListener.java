package eu.gflash.notifmod.client.listeners;

import com.google.common.base.Strings;
import eu.gflash.notifmod.client.gui.ReminderScreen;
import eu.gflash.notifmod.util.Message;
import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.util.TextUtil;
import eu.gflash.notifmod.config.types.Key;
import eu.gflash.notifmod.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

/**
 * Handles reminder key binding presses and starts reminder threads.
 * @author Alex811
 */
public class ReminderListener {
    private static boolean lastState = false;   // previous key binding state

    public static void register(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModConfig.Reminder settings = ModConfig.getInstance().reminder;
            if(gotPressed(settings.keyBind) && MinecraftClient.getInstance().currentScreen == null){
                if(settings.skipGUI)
                    start(settings.defSeconds, null);
                else
                    ReminderScreen.open();
            }
        });
    }

    /**
     * Starts timer thread.
     * @param seconds seconds to wait before notifying that the time's up
     * @param name timer title
     */
    public static void start(int seconds, String name){
        ModConfig.Reminder settings = ModConfig.getInstance().reminder;
        new Thread(() -> {
            Message.auto(settings.msgTypeStart,
                    () -> TextUtil.buildText(
                            Message.CHAT_PRE_INFO,
                            TextUtil.getWithFormat(
                                    Strings.isNullOrEmpty(name) ?
                                            new TranslatableText("msg.notifmod.reminder.start.long.unnamed", TextUtil.getWithFormat(NumUtil.secToHMSString(seconds), Formatting.YELLOW)) :
                                            new TranslatableText("msg.notifmod.reminder.start.long.named", TextUtil.getWithFormat(NumUtil.secToHMSString(seconds), Formatting.YELLOW), TextUtil.getWithFormat(name, Formatting.YELLOW)),
                                    Formatting.AQUA)),
                    () -> TextUtil.getWithFormat(new TranslatableText("msg.notifmod.reminder.start.short"), Formatting.AQUA)
            );
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message.auto(settings.msgTypeDone,
                    () -> TextUtil.buildText(
                            Message.CHAT_PRE_INFO,
                            TextUtil.getWithFormat(
                                    Strings.isNullOrEmpty(name) ?
                                        new TranslatableText("msg.notifmod.reminder.done.unnamed") :
                                        new TranslatableText("msg.notifmod.reminder.done.named", TextUtil.getWithFormat(name, Formatting.YELLOW)),
                                    Formatting.GREEN)),
                    () -> Strings.isNullOrEmpty(name) ? TextUtil.getWithFormat(new TranslatableText("msg.notifmod.reminder.done.unnamed"), Formatting.GREEN) : TextUtil.getWithFormat(name, Formatting.GREEN)
            );
            if(settings.soundEnabled)
                settings.soundSequence.play(settings.volume);
        }).start();
    }

    /**
     * Is true if the key just changed from not being pressed to being pressed.
     * Afterwards, this will not return true again until the key gets released and pressed again.
     * The last state it checks against, is the one observed during the previous call.
     * @param key the {@link Key} to check
     * @return true if it was just pressed
     */
    private static boolean gotPressed(Key key){
        boolean pressed = !lastState && key.isDown();
        lastState = key.isDown();
        return pressed;
    }
}
