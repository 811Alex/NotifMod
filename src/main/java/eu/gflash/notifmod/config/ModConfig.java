package eu.gflash.notifmod.config;

import com.google.gson.Gson;
import eu.gflash.notifmod.util.Log;
import eu.gflash.notifmod.util.Message;
import eu.gflash.notifmod.config.types.ItemList;
import eu.gflash.notifmod.config.types.Key;
import eu.gflash.notifmod.config.types.RegExPattern;
import eu.gflash.notifmod.config.types.SoundSequence;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.Config.Gui.Background;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.PrefixText;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static eu.gflash.notifmod.util.ItemUtil.getArmor;
import static eu.gflash.notifmod.util.ItemUtil.getTools;
import static me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON;

/**
 * The mod's config entries.
 * @see <a href="https://shedaniel.gitbook.io/cloth-config/auto-config">Auto Config 1u wiki</a>
 * @author Alex811
 */
@SuppressWarnings("CanBeFinal")
@Config(name = "notifmod")
@Background("minecraft:textures/block/note_block.png")
public class ModConfig implements ConfigData {
    @SuppressWarnings("rawtypes")
    @Excluded
    private static Map originalJson;    // only used to update config file on startup

    static {
        Path configPath = getConfigPath();
        if(Files.exists(configPath))
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                originalJson = new Gson().fromJson(reader, Map.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Registers config and its providers/transformers.
     */
    public static void register(){
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        GuiRegistry registry = AutoConfig.getGuiRegistry(ModConfig.class);

        registry.registerTypeProvider(new Key.Provider(), Key.class);
        registry.registerTypeProvider(new RegExPattern.Provider(), RegExPattern.class);
        registry.registerTypeProvider(new SoundSequence.Provider(), SoundSequence.class);
        registry.registerTypeProvider(new ItemList.Provider(), ItemList.class);
    }

    public static ConfigHolder<ModConfig> getHolder(){
        return AutoConfig.getConfigHolder(ModConfig.class);
    }

    public static ModConfig getInstance(){
        return getHolder().getConfig();
    }

    private static Path getConfigPath(){
        return Utils.getConfigFolder().resolve(ModConfig.class.getAnnotation(Config.class).name() + ".json");
    }

    /**
     * Finds missing keys in the current {@code json} and sets their values to the {@code defaults}, in {@code root}.
     * Recurses through categories.
     * @param root the actual config instance root or current category
     * @param defaults a config instance root or category that has the relevant defaults
     * @param json the current config JSON loaded into a {@link Map}
     * @param path the current setting path (can be used when printing relevant info)
     * @param <C> {@link ModConfig} or a category (inner class)
     * @return true if any fields were updated in any way, false if none were
     */
    private static <C> boolean updateConfig(C root, C defaults, @Nonnull Map<?, ?> json, String path){
        return Setting.forEach(root, defaults, json, path, setting -> {
            if(!setting.hasJson()){
                Log.debug("Settings: adding new field (path: " + setting.path + ")!");
                setting.reset();   // not in json, reset to default
            }else if(setting.isCategory()){
                if(setting.jsonCurr instanceof Map jc) return updateConfig(setting.curr, setting.def, jc, setting.path + ".");  // recurse for categories
                Log.debug("Settings: field changed to category in this version, resetting (path: " + setting.path + ")!");
                setting.reset();   // not a category in json, reset to default
            }else return false;
            return true;
        });
    }

    /**
     * Corrects invalid field values. Recurses through categories.
     * @param root the actual config instance root or current category
     * @param defaults a config instance root or category that has the relevant defaults
     * @param path the current setting path (can be used when printing relevant info)
     * @param <C> {@link ModConfig} or a category (inner class)
     * @return true if any fields were corrected in any way, false if none were
     */
    private <C> boolean validatePostLoad(C root, C defaults, String path) {
        return Setting.forEach(root, defaults, path, setting -> {
            if(setting.isNull()){
                Log.debug("Settings: resetting missing/invalid field (path: " + setting.path + ")!");
                setting.reset();
            }else if(setting.curr instanceof Integer c){
                Optional<ImmutablePair<Long, Long>> bounds = setting.bounds();
                if(bounds.isEmpty()) return false;
                int n = bounds.map(b -> (int) MathHelper.clamp(c, b.left, b.right)).get();
                if(n == c) return false;
                Log.debug("Settings: adjusting out of bounds number (path: " + setting.path + ", value: " + c + " -> " + n + ")!");
                setting.set(n);
            }else if(setting.curr instanceof ConfigTypeBase c && c.hasError()){
                Log.debug("Settings: resetting invalid field (path: " + setting.path + ", error: " + c.getError() + ")!");
                setting.reset();
            }else if(setting.curr instanceof Number)
                throw new NotImplementedException("Config validation currently only supports integer numbers, pls fix (path: " + setting.path + ")!");
            else if(setting.isCategory())
                return validatePostLoad(setting.curr, setting.def, setting.path + ".");  // recurse for categories
            else return false;
            return true;
        });
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        ModConfig def = new ModConfig();
        if(originalJson != null) {    // config exists and is not updated already
            Log.info("Updating config...");
            Log.info("Config " + (updateConfig(this, def, originalJson, "") ? "updated." : "already up to date."));
            originalJson = null;    // done, free memory
        }
        Log.info("Validating config...");
        ConfigData.super.validatePostLoad();
        if(validatePostLoad(this, def, "")) Log.warn("Config repaired.");
        else Log.info("Config valid.");
    }

    ////// CONFIG //////

    @CollapsibleObject
    public Durability durability = new Durability();
    @CollapsibleObject
    public Chat chat = new Chat();
    @CollapsibleObject
    public PlayerJoinLeave playerJoinLeave = new PlayerJoinLeave();
    @CollapsibleObject
    public SleepReminder sleepReminder = new SleepReminder();
    @CollapsibleObject
    public DoneLoading doneLoading = new DoneLoading();
    @CollapsibleObject
    public Reminder reminder = new Reminder();

    /// SETTINGS CLASSES/CATEGORIES ///

    public static class Durability {
        public boolean enabled = true;
        @Tooltip
        public ItemList trackedItems = new ItemList(
                "minecraft:elytra",
                "minecraft:turtle_helmet",
                "minecraft:trident",
                getArmor("chainmail"),
                getArmor("diamond"),
                getTools("diamond"),
                getArmor("netherite"),
                getTools("netherite")
        );
        @Tooltip
        public boolean alwaysNamed = true;
        @Tooltip
        public boolean alwaysEnchanted = true;
        @Tooltip
        public ItemList blacklistedEnchantedItems = ItemList.getDefault();
        @Tooltip
        public ItemList unbreakableItems = new ItemList("minecraft:elytra");
        @CollapsibleObject
        public Damage damageSettings = new Damage();
        @CollapsibleObject
        public Repair repairSettings = new Repair();

        public static class Damage {
            @Tooltip
            @BoundedDiscrete(min = 0, max = 100)
            public int damageThreshold = 5;
            @Tooltip
            @BoundedDiscrete(min = 0, max = 100)
            public int weakDamageThreshold = 10;
            @Tooltip
            @SimpleBoundedDiscrete
            public int weakThreshold = 120;
            @CollapsibleObject
            public Sub damage = new Sub("notifmod:durability.damage");
            @CollapsibleObject
            public Sub stop = new Sub("notifmod:durability.stop; 200; notifmod:durability.stop");

            public static class Sub implements SimpleAudibleTextNotif {
                @EnumHandler(option = BUTTON)
                public Message.Type msgType = Message.Type.CHAT;
                public boolean soundEnabled = false;
                @Tooltip
                public SoundSequence soundSequence;
                @BoundedDiscrete(min = 0, max = 100)
                public int volume = 100;

                public Sub(String... defSoundSeq){
                    soundSequence = new SoundSequence(defSoundSeq);
                }

                @Override
                public Message.Type getMsgType() {return msgType;}
                @Override
                public boolean isSoundEnabled() {return soundEnabled;}
                @Override
                public SoundSequence getSoundSequence() {return soundSequence;}
                @Override
                public int getVolume() {return volume;}
            }
        }

        public static class Repair implements SimpleAudibleTextNotif {
            @Tooltip
            @BoundedDiscrete(min = 0, max = 100)
            public int unlockThreshold = 75;
            @EnumHandler(option = BUTTON)
            public Message.Type msgType = Message.Type.CHAT;
            public boolean soundEnabled = false;
            @Tooltip
            public SoundSequence soundSequence = new SoundSequence("notifmod:durability.mend(1.1)");
            @BoundedDiscrete(min = 0, max = 100)
            public int volume = 100;

            @Override
            public Message.Type getMsgType() {return msgType;}
            @Override
            public boolean isSoundEnabled() {return soundEnabled;}
            @Override
            public SoundSequence getSoundSequence() {return soundSequence;}
            @Override
            public int getVolume() {return volume;}
        }
    }

    public static class Chat {
        @PrefixText
        @CollapsibleObject
        public Sub message = new Sub(".+", "", "", "notifmod:chat.message");
        @CollapsibleObject
        public Sub mention = new Sub(".*\\p.*", "", "", "notifmod:chat.mention");
        @Tooltip
        public boolean LogMsgInfo = false;

        public static class Sub implements AudibleNotif {
            public boolean enabled = true;
            @Tooltip
            public RegExPattern regexFilter;
            @Tooltip
            public RegExPattern regexFilterSys;
            @Tooltip
            public RegExPattern regexFilterGame;
            @Tooltip
            @EnumHandler(option = BUTTON)
            public Message.ChannelCombo caseSens = Message.ChannelCombo.NONE;
            @Tooltip
            public SoundSequence soundSequence;
            @BoundedDiscrete(min = 0, max = 100)
            public int volume = 100;

            public Sub(String defRegExFilter, String defRegExFilterSys, String defRegExFilterGame, String... defSoundSeq){
                regexFilter = new RegExPattern(defRegExFilter);
                regexFilterSys = new RegExPattern(defRegExFilterSys);
                regexFilterGame = new RegExPattern(defRegExFilterGame);
                soundSequence = new SoundSequence(defSoundSeq);
            }

            public boolean isCaseSens(Message.Channel channel){
                int cso = caseSens.ordinal();
                return switch(channel){
                    case CHAT -> cso % 2 == 1;
                    case SYSTEM -> (cso / 2) % 2 == 1;
                    case GAME_INFO -> cso / 4 == 1;
                };
            }

            public RegExPattern getRelevantPattern(Message.Channel channel){
                return (switch(channel){
                    case CHAT -> regexFilter;
                    case SYSTEM -> regexFilterSys;
                    case GAME_INFO -> regexFilterGame;
                }).setCaseSensitivity(isCaseSens(channel));
            }

            public boolean relevantPatternMatches(Message.Channel channel, String message){
                return getRelevantPattern(channel).matches(message);
            }

            @Override
            public boolean isSoundEnabled() {return enabled;}
            @Override
            public SoundSequence getSoundSequence() {return soundSequence;}
            @Override
            public int getVolume() {return volume;}
        }
    }

    public static class PlayerJoinLeave {
        @CollapsibleObject
        public Sound join = new Sound("notifmod:player.join(1.3)");
        @CollapsibleObject
        public Sound leave = new Sound("notifmod:player.leave(1.3)");

        public static class Sound implements AudibleNotif {
            public boolean enabled = true;
            @Tooltip
            public SoundSequence soundSequence;
            @BoundedDiscrete(min = 0, max = 100)
            public int volume = 100;

            public Sound(String... defSoundSeq){
                soundSequence = new SoundSequence(defSoundSeq);
            }

            @Override
            public boolean isSoundEnabled() {return enabled;}
            @Override
            public SoundSequence getSoundSequence() {return soundSequence;}
            @Override
            public int getVolume() {return volume;}
        }
    }

    public static class SleepReminder implements SimpleAudibleTextNotif {
        @PrefixText
        public boolean enabled = false;
        public boolean includeThunder = true;
        @CollapsibleObject
        public Conditions conditions = new Conditions();
        @EnumHandler(option = BUTTON)
        public Message.Type msgType = Message.Type.CHAT;
        public boolean soundEnabled = false;
        @Tooltip
        public SoundSequence soundSequence = new SoundSequence("notifmod:sleep.now");
        @BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        @Override
        public Message.Type getMsgType() {return msgType;}
        @Override
        public boolean isSoundEnabled() {return soundEnabled;}
        @Override
        public SoundSequence getSoundSequence() {return soundSequence;}
        @Override
        public int getVolume() {return volume;}

        public static class Conditions {
            @Tooltip
            public boolean pauseInTimelessDims = true;
            @Tooltip
            public boolean pauseUnderground = true;
            public int minAltitude = 50;
            @BoundedDiscrete(min = 1, max = 15)
            public int minSkyLight = 1;
            @Tooltip
            @SimpleBoundedDiscrete
            public int cooldown = 120;
        }
    }

    public static class DoneLoading {
        @CollapsibleObject
        public Game game = new Game("notifmod:done_loading.game");
        @CollapsibleObject
        public World world = new World("notifmod:done_loading.world");

        public static class Game implements AudibleNotif {
            public boolean enabled = false;
            @Tooltip
            public boolean afterFade = true;
            @Tooltip
            public SoundSequence soundSequence;
            @BoundedDiscrete(min = 0, max = 100)
            public int volume = 100;

            public Game(String... defSoundSeq){
                soundSequence = new SoundSequence(defSoundSeq);
            }

            @Override
            public boolean isSoundEnabled() {return enabled;}
            @Override
            public SoundSequence getSoundSequence() {return soundSequence;}
            @Override
            public int getVolume() {return volume;}
        }

        public static class World implements AudibleNotif {
            public boolean enabled = false;
            @Tooltip
            @SimpleBoundedDiscrete
            public int chunks = 9;
            @Tooltip
            public SoundSequence soundSequence;
            @BoundedDiscrete(min = 0, max = 100)
            public int volume = 100;

            public World(String... defSoundSeq){
                soundSequence = new SoundSequence(defSoundSeq);
            }

            @Override
            public boolean isSoundEnabled() {return enabled;}
            @Override
            public SoundSequence getSoundSequence() {return soundSequence;}
            @Override
            public int getVolume() {return volume;}
        }
    }

    public static class Reminder implements AudibleNotif {
        @Tooltip
        public Key keyBind = new Key(GLFW.GLFW_KEY_KP_ADD);
        @Tooltip
        public Key keyBindNoGUI = new Key(GLFW.GLFW_KEY_UNKNOWN);
        @Tooltip
        @SimpleBoundedDiscrete
        public int defSeconds = 300;
        @Tooltip
        @SimpleBoundedDiscrete
        public int pre1Seconds = 120;
        @Tooltip
        @SimpleBoundedDiscrete
        public int pre2Seconds = 1200;
        @Tooltip
        @EnumHandler(option = BUTTON)
        public Message.Type msgTypeStart = Message.Type.ACTIONBAR;
        @Tooltip
        @EnumHandler(option = BUTTON)
        public Message.Type msgTypeDone = Message.Type.CHAT;
        public boolean soundEnabled = true;
        @Tooltip
        public SoundSequence soundSequence = new SoundSequence("notifmod:reminder.done");
        @BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        @Override
        public boolean isSoundEnabled() {return soundEnabled;}
        @Override
        public SoundSequence getSoundSequence() {return soundSequence;}
        @Override
        public int getVolume() {return volume;}
    }

    /// INTERFACES ///

    public interface SimpleAudibleTextNotif extends AudibleNotif, SimpleTextNotif {}

    public interface SimpleTextNotif {
        Message.Type getMsgType();

        // Message.Type.msg()/msgWithPre() shortcuts
        default void msg(Supplier<Text> msg) {getMsgType().msg(msg);}
        default void msg(Supplier<Text> longMsg, Supplier<Text> shortMsg) {getMsgType().msg(longMsg, shortMsg);}
        default void msgWithPre(Supplier<Text> msg) {getMsgType().msgWithPre(msg);}
        default void msgWithPre(Supplier<Text> longMsg, Supplier<Text> shortMsg) {getMsgType().msgWithPre(longMsg, shortMsg);}
    }

    public interface AudibleNotif {
        boolean isSoundEnabled();
        SoundSequence getSoundSequence();
        int getVolume();

        /**
         * Plays the selected {@link SoundSequence}, with the selected volume, if the sound notification is enabled.
         */
        default void playSound(){
            if(isSoundEnabled()) getSoundSequence().play(getVolume());
        }
    }

    /// UTIL ///

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface SimpleBoundedDiscrete {
        long min() default 0;
        long max() default Integer.MAX_VALUE;
    }

    private record Setting<C>(Field field, C currObj, Object curr, Object def, Object jsonCurr, String path){
        public Setting(Field field, C currObj, C defObj, String path) throws IllegalAccessException {
            this(field, currObj, defObj, null, path);
        }
        public Setting(Field field, C currObj, C defObj, Map<?, ?> json, String path) throws IllegalAccessException {
            this(field, currObj, field.get(currObj), field.get(defObj), json == null ? null : json.get(field.getName()), path + field.getName());
        }

        public void set(Object n) {
            try {field.set(currObj, n);}
            catch (IllegalAccessException e) {throw new RuntimeException(e);}
        }
        public void reset() {set(def);}
        public boolean isNull() {return curr == null;}
        public boolean isCategory() {return field.isAnnotationPresent(CollapsibleObject.class);}
        public boolean hasJson() {return jsonCurr != null;}
        public Optional<ImmutablePair<Long, Long>> bounds() {return BoundMap.get(field);}

        /**
         * Same as {@link Setting#forEach(Object, Object, Map, String, Predicate)}, but sets the {@code json} arg to null.
         * @param root used to instantiate {@link Setting} and to get the declared {@link Field}s
         * @param defaults used to instantiate {@link Setting}
         * @param path used to instantiate {@link Setting}
         * @param predicate {@link Predicate} to test against each {@link Setting}
         * @return true if the predicate test returns true for any of the {@link Setting} instances, false if none does
         * @param <C> {@link ModConfig} or a category (inner class)
         * @see Setting#forEach(Object, Object, Map, String, Predicate)
         */
        public static <C> boolean forEach(C root, C defaults, String path, Predicate<Setting<C>> predicate){
            return forEach(root, defaults, null, path, predicate);
        }

        /**
         * For each declared {@link Field} in {@code root}
         * that is not static, final or annotated as {@link Excluded},
         * map into a {@link Setting} instance and test the {@code predicate} against it.
         * @param root used to instantiate {@link Setting} and to get the declared {@link Field}s
         * @param defaults used to instantiate {@link Setting}
         * @param json used to instantiate {@link Setting}
         * @param path used to instantiate {@link Setting}
         * @param predicate {@link Predicate} to test against each {@link Setting}
         * @return true if the predicate test returns true for any of the {@link Setting} instances, false if none does
         * @param <C> {@link ModConfig} or a category (inner class)
         */
        public static <C> boolean forEach(C root, C defaults, @Nullable Map<?, ?> json, String path, Predicate<Setting<C>> predicate) {  // loop root's (non-static, non-final) fields
            //noinspection ReplaceInefficientStreamCount
            return Arrays.stream(root.getClass().getDeclaredFields())
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .filter(field -> !Modifier.isFinal(field.getModifiers()))
                    .filter(field -> !field.isAnnotationPresent(Excluded.class))
                    .map(field -> {
                        try {return new Setting<>(field, root, defaults, json, path);}
                        catch(IllegalAccessException e) {throw new RuntimeException(e);}
                    })
                    .filter(predicate)
                    .count() > 0;   // filter->count instead of anyMatch, to force *all* of them to be evaluated
        }
    }

    private static class BoundMap{
        private static final Entry[] map = new Entry[]{
                add(BoundedDiscrete.class, a -> pair(a.min(), a.max())),
                add(SimpleBoundedDiscrete.class, a -> pair(a.min(), a.max()))
        };

        /**
         * Returns the declared bounds for the {@code field}, according to its annotations.
         * @param field {@link Field} to get bounds for
         * @return the bounds ({@link ImmutablePair#left} is min, {@link ImmutablePair#right} is max), or empty {@link Optional} if none were found
         */
        public static Optional<ImmutablePair<Long, Long>> get(Field field){
            return Arrays.stream(map).map(m -> m.map(field)).filter(Objects::nonNull).findFirst();
        }

        @SuppressWarnings("unchecked")
        private static <A extends Annotation> Entry add(Class<A> annotationClass, Function<A, ImmutablePair<Long, Long>> mapper){
            return new Entry(annotationClass, (Function<Annotation, ImmutablePair<Long, Long>>) mapper);
        }

        private static ImmutablePair<Long, Long> pair(Long min, Long max) {return ImmutablePair.of(min, max);}

        private record Entry(Class<? extends Annotation> annotationClass, Function<Annotation, ImmutablePair<Long, Long>> mapper){
            public ImmutablePair<Long, Long> map(Field field){
                return field.isAnnotationPresent(annotationClass) ? mapper.apply(field.getAnnotation(annotationClass)) : null;
            }
        }
    }
}
