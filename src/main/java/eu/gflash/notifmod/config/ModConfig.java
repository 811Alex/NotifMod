package eu.gflash.notifmod.config;

import com.google.gson.Gson;
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
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
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

        updateConfig();
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
     * Finds missing keys in the current JSON and sets their values to the defaults.
     * Recurses through categories. This will overwrite the JSON file, with the updated one, when done.
     * Call after config registration. {@link ModConfig#originalJson} will be cleared when done.
     */
    private static void updateConfig(){
        if(originalJson == null) return;    // no config existed, or updated already
        updateConfig(getInstance(), new ModConfig(), originalJson);
        getHolder().save();
        originalJson = null;    // done, free memory
    }

    /**
     * Finds missing keys in the current {@code json} and sets their values to the {@code defaults}, in {@code root}.
     * Recurses through categories. You probably want to call {@link ModConfig#updateConfig()} instead.
     * @param root the actual config instance root or current category
     * @param defaults a config instance root or category that has the relevant defaults
     * @param json the current config JSON loaded into a {@link Map}
     * @param <C> {@link ModConfig} or a category (inner class)
     */
    private static <C> void updateConfig(C root, C defaults, Map<?, ?> json){
        Arrays.stream(root.getClass().getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .filter(field -> !field.isAnnotationPresent(Excluded.class))
                .forEach(field -> {   // loop root's (non-static, non-final) fields
                    try {
                        Object curr = field.get(root);
                        Object def = field.get(defaults);
                        Object jsonCurr = json.get(field.getName());
                        if(jsonCurr == null)
                            field.set(root, def);   // not in json, reset to default
                        else if(field.isAnnotationPresent(CollapsibleObject.class)){
                            if(jsonCurr instanceof Map)
                                updateConfig(curr, def, (Map<?, ?>) jsonCurr);  // recurse for categories
                            else
                                field.set(root, def);   // not a category in json, reset to default
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
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

            public RegExPattern getRelevantPattern(MessageType messageType){
                int cso = caseSens.ordinal();
                return switch(Message.Channel.fromMessageType(messageType)){
                    case CHAT -> regexFilter.setCaseSensitivity(cso % 2 == 1);
                    case SYSTEM -> regexFilterSys.setCaseSensitivity((cso / 2) % 2 == 1);
                    case GAME_INFO -> regexFilterGame.setCaseSensitivity(cso / 4 == 1);
                };
            }

            public boolean relevantPatternMatches(MessageType messageType, String message){
                return getRelevantPattern(messageType).matches(message);
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
        public int defSeconds = 300;
        @Tooltip
        public int pre1Seconds = 120;
        @Tooltip
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
}
