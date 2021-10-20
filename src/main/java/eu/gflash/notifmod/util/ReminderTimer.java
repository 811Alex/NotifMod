package eu.gflash.notifmod.util;

import com.google.common.base.Strings;
import eu.gflash.notifmod.config.ModConfig;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Handles reminder timers.
 * @author Alex811
 */
public class ReminderTimer {
    private static final Map<UUID, ReminderTimer> active = Collections.synchronizedMap(new HashMap<>());
    private final UUID id;
    private final Timer timer;
    private final int seconds;
    private final String name;
    private long start = -1;

    /**
     * Timer constructor. Doesn't start the timer, you need to do that manually.
     * @param seconds seconds to wait before notifying that the time's up
     * @param name timer title
     * @see #start()
     * @see ReminderTimer#startNew(int, String)
     */
    public ReminderTimer(int seconds, String name) {
        this.id = UUID.randomUUID();
        this.timer = new Timer(true);
        this.seconds = seconds;
        this.name = name;
    }

    /**
     * Starts timer thread, stores timer as {@link #active} & displays appropriate message.
     */
    public void start() {
        active.put(id, this);
        start = getCurrSecs();
        timer.schedule(new Task(), seconds * 1000L);
        ModConfig.Reminder settings = ModConfig.getInstance().reminder;
        Message.auto(settings.msgTypeStart,
                () -> TextUtil.buildText(
                        Message.CHAT_PRE_INFO,
                        TextUtil.getWithFormat(
                                Strings.isNullOrEmpty(name) ?
                                        new TranslatableText("msg.notifmod.reminder.start.long.unnamed", TextUtil.getWithFormat(NumUtil.secToHMSString(seconds), Formatting.YELLOW)) :
                                        new TranslatableText("msg.notifmod.reminder.start.long.named", TextUtil.getWithFormat(NumUtil.secToHMSString(seconds), Formatting.YELLOW), TextUtil.getWithFormat(name, Formatting.YELLOW)),
                                Formatting.AQUA)),
                () -> TextUtil.getWithFormat(new TranslatableText("msg.notifmod.reminder.start.short"), Formatting.AQUA)
        );
    }

    /**
     * Makes new timer & runs {@link #start()} on it.
     * @param seconds seconds to wait before notifying that the time's up
     * @param name timer title
     */
    public static void startNew(int seconds, String name) {
        new ReminderTimer(seconds, name).start();
    }

    /**
     * Stops timers that haven't finished, removes from the {@link #active} list.
     */
    public void kill() {
        timer.cancel();
        active.remove(id);
    }

    /**
     * Runs {@link #kill()} on all {@link #active} timers.
     */
    public static void killAll() {
        List.copyOf(active.values()).forEach(ReminderTimer::kill);
    }

    /**
     * Returns collection of all active timers.
     * @return active timers
     */
    public static Collection<ReminderTimer> getActive() {
        return active.values();
    }

    public String getName() {
        return name;
    }

    /**
     * Returns remaining seconds of a running timer.
     * If finished, it'll be negative.
     * If it never started, it'll be -1.
     * @return remaining seconds or -1 if it never ran
     */
    public int getRemaining() {
        return start >= 0 ? (int) (start + seconds - getCurrSecs()) : -1;
    }

    public boolean isActive(){
        return active.containsKey(id);
    }

    public boolean hasName(){
        return !name.isEmpty();
    }

    /**
     * Returns current time in seconds.
     * @see System#currentTimeMillis()
     * @return current time in seconds
     */
    private long getCurrSecs() {
        return Math.floorDiv(System.currentTimeMillis(), 1000);
    }

    /**
     * A {@link TimerTask} that notifies the player when the timer finishes and removes the timer from the {@link #active} list.
     */
    private class Task extends TimerTask {
        @Override
        public void run() {
            ModConfig.Reminder settings = ModConfig.getInstance().reminder;
            Message.auto(settings.msgTypeDone,
                    () -> TextUtil.buildText(
                            Message.CHAT_PRE_INFO,
                            TextUtil.getWithFormat(
                                    Strings.isNullOrEmpty(name) ?
                                            new TranslatableText("msg.notifmod.reminder.done.unnamed") :
                                            new TranslatableText("msg.notifmod.reminder.done.named", TextUtil.getWithFormat(name, Formatting.YELLOW)),
                                    Formatting.GREEN)),
                    () -> Strings.isNullOrEmpty(name) ? TextUtil.getWithFormat(new TranslatableText("msg.notifmod.reminder.done.unnamed"), Formatting.GREEN) : TextUtil.getWithFormat(name, Formatting.GREEN)
            );
            if (settings.soundEnabled)
                settings.soundSequence.play(settings.volume);
            active.remove(id);
        }
    }
}
