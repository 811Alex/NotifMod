package eu.gflash.notifmod.config;

import eu.gflash.notifmod.util.Message;
import eu.gflash.notifmod.config.types.ItemList;
import eu.gflash.notifmod.config.types.Key;
import eu.gflash.notifmod.config.types.RegExPattern;
import eu.gflash.notifmod.config.types.SoundSequence;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.Config.Gui.Background;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.PrefixText;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.lwjgl.glfw.GLFW;

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

    public static ModConfig getInstance(){
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    ////// CONFIG //////

    @CollapsibleObject
    public Durability durability = new Durability();
    @CollapsibleObject
    public ChatCategory chat = new ChatCategory();
    @CollapsibleObject
    public PlayerJoinLeaveCategory playerJoinLeave = new PlayerJoinLeaveCategory();
    @CollapsibleObject
    public DoneLoadingCategory doneLoading = new DoneLoadingCategory();
    @CollapsibleObject
    public Reminder reminder = new Reminder();

    /// GROUPING CATEGORIES ///

    public static class ChatCategory {
        @PrefixText
        @CollapsibleObject
        public Chat message = new Chat(".+", "notifmod:chat.message");
        @CollapsibleObject
        public Chat mention = new Chat(".*\\p", "notifmod:chat.mention");
    }

    public static class PlayerJoinLeaveCategory {
        @CollapsibleObject
        public SimpleSound join = new SimpleSound("notifmod:player.join(1.3)");
        @CollapsibleObject
        public SimpleSound leave = new SimpleSound("notifmod:player.leave(1.3)");
    }

    public static class DoneLoadingCategory {
        @CollapsibleObject
        public DoneLoadingGame game = new DoneLoadingGame("notifmod:done_loading.game");
        @CollapsibleObject
        public DoneLoadingWorld world = new DoneLoadingWorld("notifmod:done_loading.world");
    }

    /// SETTINGS CLASSES ///

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
        public DurabilityDamage damageSettings = new DurabilityDamage();
        @CollapsibleObject
        public DurabilityRepair repairSettings = new DurabilityRepair();
    }

    public static class DurabilityDamage {
        @Tooltip
        @BoundedDiscrete(min = 0, max = 100)
        public int damageThreshold = 5;
        @Tooltip
        @BoundedDiscrete(min = 0, max = 100)
        public int weakDamageThreshold = 10;
        @Tooltip
        public int weakThreshold = 120;
        @CollapsibleObject
        public DurabilityDamageSub damage = new DurabilityDamageSub("notifmod:durability.damage");
        @CollapsibleObject
        public DurabilityDamageSub stop = new DurabilityDamageSub("notifmod:durability.stop; 200; notifmod:durability.stop");
    }

    public static class DurabilityDamageSub implements DurabilitySubcategory {
        @EnumHandler(option = BUTTON)
        public Message.Type msgType = Message.Type.CHAT;
        public boolean soundEnabled = false;
        @Tooltip
        public SoundSequence soundSequence;
        @BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        public DurabilityDamageSub(String... defSoundSeq){
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

    public static class DurabilityRepair implements DurabilitySubcategory {
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

    public interface DurabilitySubcategory {
        Message.Type getMsgType();
        boolean isSoundEnabled();
        SoundSequence getSoundSequence();
        int getVolume();
    }

    public static class Chat {
        public boolean enabled = true;
        @Tooltip
        public RegExPattern regexFilter;
        @Tooltip
        public SoundSequence soundSequence;
        @BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        public Chat(String defRegExFilter, String... defSoundSeq){
            regexFilter = new RegExPattern(defRegExFilter);
            soundSequence = new SoundSequence(defSoundSeq);
        }
    }

    public static class SimpleSound {
        public boolean enabled = true;
        @Tooltip
        public SoundSequence soundSequence;
        @BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        public SimpleSound(String... defSoundSeq){
            soundSequence = new SoundSequence(defSoundSeq);
        }
    }

    public static class DoneLoadingGame {
        public boolean enabled = false;
        @Tooltip
        public boolean afterFade = true;
        @Tooltip
        public SoundSequence soundSequence;
        @BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        public DoneLoadingGame(String... defSoundSeq){
            soundSequence = new SoundSequence(defSoundSeq);
        }
    }

    public static class DoneLoadingWorld {
        public boolean enabled = false;
        @Tooltip
        public int chunks = 9;
        @Tooltip
        public SoundSequence soundSequence;
        @BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        public DoneLoadingWorld(String... defSoundSeq){
            soundSequence = new SoundSequence(defSoundSeq);
        }
    }

    public static class Reminder{
        @Tooltip
        public Key keyBind = new Key(GLFW.GLFW_KEY_KP_ADD);
        @Tooltip
        public int defSeconds = 300;
        @Tooltip
        public int pre1Seconds = 120;
        @Tooltip
        public int pre2Seconds = 1200;
        @Tooltip
        public boolean skipGUI = false;
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
    }
}
