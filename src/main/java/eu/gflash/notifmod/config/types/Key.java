package eu.gflash.notifmod.config.types;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.gflash.notifmod.config.ConfigTypeBase;
import eu.gflash.notifmod.mixin.InputUtilTypeAccessor;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Key binding config entry type.
 * @author Alex811
 */
@JsonAdapter(Key.Adapter.class)
public class Key extends ConfigTypeBase {
    private final InputUtil.Key key;

    public Key(int keyCode){
        this(safeFromCode(keyCode));
    }

    public Key(InputUtil.Key key){
        this.key = key;
    }

    private static InputUtil.Key safeFromCode(int keyCode){
        InputUtil.Type kb = InputUtil.Type.KEYSYM;
        return ((InputUtilTypeAccessor) (Object) kb).getMap().containsKey(keyCode) ?
                kb.createFromCode(keyCode) : InputUtil.UNKNOWN_KEY;
    }

    public static Key getDefault(){
        return new Key(InputUtil.UNKNOWN_KEY);
    }

    public InputUtil.Key get() {
        return key;
    }

    public int getCode(){
        return key.getCode();
    }

    public boolean isDown(){
        return key != InputUtil.UNKNOWN_KEY && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), getCode());
    }

    @Override
    protected Text getUnsafeError() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key1 = (Key) o;
        return new EqualsBuilder().append(key, key1.key).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 59).append(key).toHashCode();
    }

    public static class Adapter extends TypeAdapter<Key> { // JSON adapter
        @Override
        public void write(JsonWriter out, Key value) throws IOException {
            out.value(value.getCode());
        }

        @Override
        public Key read(JsonReader in) throws IOException {
            return new Key(in.nextInt());
        }
    }

    public static class Provider extends ProviderBase<ModifierKeyCode> { // GUI provider
        @Override
        public AbstractConfigListEntry<ModifierKeyCode> getEntry(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
            KeyCodeEntry entry = ENTRY_BUILDER.startKeyCodeField(Text.translatable(i13n), Utils.getUnsafely(field, config, Key.getDefault()).get())
                    .setDefaultValue(() -> ((Key) Utils.getUnsafely(field, defaults)).get())
                    .setKeySaveConsumer(newValue -> Utils.setUnsafely(field, config, new Key(newValue)))
                    .build();
            entry.setAllowMouse(false);
            return entry;
        }
    }
}
