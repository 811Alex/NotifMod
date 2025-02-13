package eu.gflash.notifmod.config.types;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.gflash.notifmod.config.ConfigTypeBase;
import eu.gflash.notifmod.util.IdentifierUtil;
import eu.gflash.notifmod.util.ThreadUtils;
import joptsimple.internal.Strings;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sound sequence config entry type.
 * @author Alex811
 */
@JsonAdapter(SoundSequence.Adapter.class)
public class SoundSequence extends ConfigTypeBase {
    private static final Pattern NUM_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
    private static final Pattern SOUND_PATTERN = Pattern.compile("^(([^:()]+:)?[^:()]+)(\\((.+)\\))?$");
    private static final Pattern DELIMITER_PATTERN = Pattern.compile(";");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private final String sequenceStr;
    private final List<Sound> sequence = new ArrayList<>();
    private String errorId = "";

    public SoundSequence(String... sequence){
        this(Strings.join(sequence, "; "));
    }

    public SoundSequence(String sequence){
        this.sequenceStr = sequence;
        String noSpaceSequence = SPACE_PATTERN.matcher(sequence).replaceAll("");
        if(noSpaceSequence.isEmpty()) return;
        String[] entrySplit = DELIMITER_PATTERN.split(noSpaceSequence);
        for(int i = 0; i < entrySplit.length && error.isEmpty(); i+=2){
            Matcher soundMatcher = SOUND_PATTERN.matcher(entrySplit[i]);
            if(soundMatcher.matches()){
                float pitch = parseNum(soundMatcher.group(4), "invalidPitch", 1);
                int delay = i + 1 < entrySplit.length - 1 ? (int) parseNum(entrySplit[i + 1], "invalidDelay", 0) : 0;  // next delay (assuming it's not the last element), or 0
                String id = soundMatcher.group(1);
                if(IdentifierUtil.isValid(id))
                    Registries.SOUND_EVENT.getOptionalValue(Identifier.of(id)).ifPresentOrElse(
                            soundEvent -> this.sequence.add(new Sound(soundEvent, pitch, delay)),
                            () -> setError("doesNotExist", id)
                    );
                else
                    setError("invalidIdentifier", id);
            }else
                setError("invalidFormat", entrySplit[i]);
        }
    }

    public static SoundSequence getDefault(){
        return new SoundSequence("");
    }

    /**
     * Safely parses numbers and reports errors.
     * @param num the {@link String} that might represent a number
     * @param err the partial LangKey to use for errors
     * @param def default number
     * @return the number, or {@code def} on error
     */
    private float parseNum(String num, String err, float def){
        if(num == null) return def;
        if(NUM_PATTERN.matcher(num).matches())
            return Float.parseFloat(num);
        setError(err, num);
        return def;
    }

    /**
     * Sets the error message and its parameter.
     * @param err the partial LangKey to use for the error
     * @param id the LangKey parameter
     */
    private void setError(String err, String id){
        error = err;
        errorId = id;
    }

    @Override
    protected Text getUnsafeError() {
        return Text.translatable("error.config.notifmod.soundSequence." + error, errorId);
    }

    /**
     * Validates a sound sequence string.
     * @param sequence string to validate
     * @return empty if valid, otherwise contains the error
     */
    public static Optional<Text> validate(String sequence){
        return new SoundSequence(sequence).getError();
    }

    /**
     * Plays sound sequence
     * @param volume volume percentage
     */
    public void play(int volume){
        play(volume / 100.0F);
    }

    /**
     * Plays sound sequence
     * @param volume volume, range 0 - 1
     */
    public void play(float volume){
        new Thread(() -> sequence.forEach(sound -> {
            sound.play(volume);
            try {
                Thread.sleep(sound.delay());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        })).start();
    }

    @Override
    public String toString() {
        return sequenceStr;
    }

    private record Sound(SoundEvent soundEvent, float pitch, int delay) {
        /**
         * Plays {@link this#soundEvent}, using the defined {@link this#pitch} and the {@code volume} parameter.
         * @param volume volume, range 0 - 1
         */
        public void play(float volume) {
            MinecraftClient mc = MinecraftClient.getInstance();
            ThreadUtils.execOnThread(mc, () -> mc.getSoundManager().play(PositionedSoundInstance.master(soundEvent, pitch, volume)));
        }

        /**
         * The delay to be used after this sound is played.
         * @return the following delay
         */
        @Override
        public int delay() {
            return delay;
        }
    }

    public static class Adapter extends TypeAdapter<SoundSequence> { // JSON adapter
        @Override
        public void write(JsonWriter out, SoundSequence value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public SoundSequence read(JsonReader in) throws IOException {
            return new SoundSequence(in.nextString());
        }
    }

    public static class Provider extends ProviderBase<String> { // GUI provider
        @Override
        public AbstractConfigListEntry<String> getEntry(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
            return ENTRY_BUILDER.startStrField(Text.translatable(i13n), Utils.getUnsafely(field, config, SoundSequence.getDefault()).toString())
                    .setDefaultValue(() -> Utils.getUnsafely(field, defaults).toString())
                    .setSaveConsumer(newValue -> Utils.setUnsafely(field, config, new SoundSequence(newValue)))
                    .setErrorSupplier(SoundSequence::validate)
                    .build();
        }
    }
}
