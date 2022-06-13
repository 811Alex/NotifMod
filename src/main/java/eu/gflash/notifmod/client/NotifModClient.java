package eu.gflash.notifmod.client;

import eu.gflash.notifmod.client.listeners.ReminderListener;
import eu.gflash.notifmod.client.sound.CustomSounds;
import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.Message;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * <h1>NotifMod</h1>
 * <hr>
 * <i>Client-side Fabric mod that gives you notifications when certain things happen!</i>
 *
 * @author Alex811
 */
@Environment(EnvType.CLIENT)
public class NotifModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CustomSounds.register();
        ModConfig.register();
        ReminderListener.register();
        Message.log("NotifMod loaded!");
    }
}
