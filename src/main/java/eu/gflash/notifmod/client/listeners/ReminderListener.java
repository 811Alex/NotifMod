package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.client.gui.ReminderScreen;
import eu.gflash.notifmod.util.ReminderTimer;
import eu.gflash.notifmod.config.types.Key;
import eu.gflash.notifmod.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * Handles reminder key binding presses and starts reminder threads.
 * @author Alex811
 */
public class ReminderListener {
    private static boolean lastState = false;   // previous key binding state

    public static void register(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModConfig.Reminder settings = ModConfig.getInstance().reminder;
            if(gotPressed(settings.keyBind)){
                Screen currScreen = MinecraftClient.getInstance().currentScreen;
                if(currScreen == null){
                    if(settings.skipGUI)
                        ReminderTimer.startNew(settings.defSeconds, null);
                    else
                        ReminderScreen.open();
                }else if(currScreen instanceof ReminderScreen)
                    currScreen.close();
            }
        });
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
