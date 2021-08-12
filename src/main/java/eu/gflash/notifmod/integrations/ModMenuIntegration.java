package eu.gflash.notifmod.integrations;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import eu.gflash.notifmod.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;

/**
 * Mod Menu integration.
 * Registers our config screen on Mod Menu.
 * @author Alex811
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}
