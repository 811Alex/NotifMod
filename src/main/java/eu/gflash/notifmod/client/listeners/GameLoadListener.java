package eu.gflash.notifmod.client.listeners;

import eu.gflash.notifmod.config.ModConfig;

/**
 * Handles game loading related events.
 * @author Alex811
 */
public class GameLoadListener {
    /**
     * Gets called the first time the {@link net.minecraft.client.gui.screen.TitleScreen} appears.
     * Note: this will be called once before the fade in animation and a second time when it's done.
     * @param afterFade true if called right before the fade in animation, false if the animation just finished
     */
    public static void onTitleScreen(boolean afterFade){
        ModConfig.DoneLoadingGame settings = ModConfig.getInstance().doneLoading.game;
        if(settings.enabled && settings.afterFade == afterFade)
            settings.soundSequence.play(settings.volume);
    }
}
