package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.client.gui.ReminderListScreen;
import eu.gflash.notifmod.client.gui.ReminderScreen;
import eu.gflash.notifmod.util.ReminderTimer;
import eu.gflash.notifmod.config.types.Key;
import eu.gflash.notifmod.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Handles reminder key binding presses and starts reminder threads.
 * @author Alex811
 */
public class ReminderListener {
    private static final boolean[] lastState = {false, false};    // previous key binding state

    public static void register(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModConfig.Reminder settings = ModConfig.getInstance().reminder;
            boolean GUIKeyPressed = gotPressed(settings.keyBind, 0);
            boolean NoGUIKeyPressed = gotPressed(settings.keyBindNoGUI, 1); // Note: must always run gotPressed() for all bindings
            if(!(GUIKeyPressed || NoGUIKeyPressed)) return;
            Screen currScreen = Minecraft.getInstance().screen;
            if(!GUIKeyPressed){
                if(currScreen == null) ReminderTimer.startNew(settings.defSeconds, null);
            }else switch(currScreen){
                case null -> ReminderScreen.open();
                case ReminderScreen s -> currScreen.onClose();
                case ReminderListScreen s -> ReminderScreen.open();
                default -> {}
            }
        });
    }

    /**
     * Is true if the key just changed from not being pressed to being pressed.
     * Afterwards, this will not return true again until the key gets released and pressed again.
     * The last state it checks against, is the one observed during the previous call.
     * @param key the {@link Key} to check
     * @param keyIndex the index to use for {@link #lastState}
     * @return true if it was just pressed
     */
    private static boolean gotPressed(Key key, int keyIndex){
        boolean pressed = !lastState[keyIndex] && key.isDown();
        lastState[keyIndex] = key.isDown();
        return pressed;
    }
}
