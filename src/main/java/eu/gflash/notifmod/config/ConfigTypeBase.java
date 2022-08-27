package eu.gflash.notifmod.config;

import joptsimple.internal.Strings;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Common code for our config's custom types.
 * @see ModConfig
 * @author Alex811
 */
public abstract class ConfigTypeBase {
    protected String error = "";

    /**
     * Get error when knowing for sure that there is one.
     * @return the error {@link Text} to display
     */
    protected abstract Text getUnsafeError();

    /**
     * Get error during construction.
     * @return {@link Optional} containing the error, or empty if none
     */
    public Optional<Text> getError() {
        return hasError() ? Optional.of(getUnsafeError()) : Optional.empty();
    }

    public boolean hasError(){
        return !Strings.isNullOrEmpty(error);
    }

    public abstract static class ProviderBase<T> implements GuiProvider {
        protected static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

        @SuppressWarnings("rawtypes")
        @Override
        public List<AbstractConfigListEntry> get(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
            if (field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class)) return Collections.emptyList();
            return Collections.singletonList(getEntry(i13n, field, config, defaults, registry));
        }

        public abstract AbstractConfigListEntry<T> getEntry(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry);
    }
}
