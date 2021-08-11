package eu.gflash.notifmod.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * Common code for our config's custom types' {@link GuiProvider}s.
 * @author Alex811
 */
public abstract class ProviderBase<T> implements GuiProvider {
    protected static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

    @SuppressWarnings("rawtypes")
    @Override
    public List<AbstractConfigListEntry> get(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
        if (field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class)) return Collections.emptyList();
        return Collections.singletonList(getEntry(i13n, field, config, defaults, registry));
    }

    public abstract AbstractConfigListEntry<T> getEntry(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry);
}
