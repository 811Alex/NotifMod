package eu.gflash.notifmod.config.types;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.gflash.notifmod.config.ProviderBase;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regular expression pattern config entry type.
 * @author Alex811
 */
@JsonAdapter(RegExPattern.Adapter.class)
public class RegExPattern {
    private final String original;
    private Pattern pattern;
    private String error = "";
    private String lastName;
    private boolean caseSensitive = false;

    public RegExPattern(String pattern){
        this.original = pattern;
        compile();
    }

    /**
     * Sets case sensitivity. Takes effect after {@link #compile()} gets called again.
     * @param caseSensitive true to make it case-sensitive
     * @return this object, for convenience
     */
    public RegExPattern setCaseSensitivity(boolean caseSensitive){
        this.caseSensitive = caseSensitive;
        return this;
    }

    /**
     * Recompiles pattern if necessary.
     */
    public void compile(){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        String currName = player == null ? null : player.getDisplayName().getString();
        if(Objects.equals(lastName, currName) && pattern != null)   // compiled with current name
            if((pattern.flags() == 0) == caseSensitive)             // compiled with current case-sensitivity
                return;                                             // no need to recompile
        lastName = currName;
        String preppedPattern = original.replace("\\p", currName == null ? "." : currName);
        try{
            pattern = Pattern.compile(preppedPattern, caseSensitive ? 0 : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }catch(PatternSyntaxException ex){
            pattern = Pattern.compile(".*");
            this.error = ex.getDescription();
        }
    }

    public static RegExPattern getDefault(){
        return new RegExPattern(".*");
    }

    /**
     * Get error during construction.
     * @return {@link Optional} containing the error, or empty if none
     */
    public Optional<Text> getError() {
        return error.isEmpty() ? Optional.empty() : Optional.of((Text) new TranslatableText("error.config.notifmod.RegExPattern", error));
    }

    /**
     * Validates a RegExPattern string.
     * @param pattern string to validate
     * @return empty if valid, otherwise contains the error
     */
    public static Optional<Text> validate(String pattern){
        return new RegExPattern(pattern).getError();
    }

    public boolean matches(String str){
        return get().matcher(str).matches();
    }

    public Pattern get() {
        compile();  // make sure to recompile if the username changed
        return pattern;
    }

    @Override
    public String toString() {
        return original;
    }

    public static class Adapter extends TypeAdapter<RegExPattern> { // JSON adapter
        @Override
        public void write(JsonWriter out, RegExPattern value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public RegExPattern read(JsonReader in) throws IOException {
            return new RegExPattern(in.nextString());
        }
    }

    public static class Provider extends ProviderBase<String> { // GUI provider
        @Override
        public AbstractConfigListEntry<String> getEntry(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
            return ENTRY_BUILDER.startStrField(new TranslatableText(i13n), Utils.getUnsafely(field, config, RegExPattern.getDefault()).toString())
                    .setDefaultValue(() -> Utils.getUnsafely(field, defaults).toString())
                    .setSaveConsumer(newValue -> Utils.setUnsafely(field, config, new RegExPattern(newValue)))
                    .setErrorSupplier(RegExPattern::validate)
                    .build();
        }
    }
}
