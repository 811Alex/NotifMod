package eu.gflash.notifmod.util;

import com.google.common.base.Strings;
import eu.gflash.notifmod.config.ModConfig;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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
    private final boolean repeat;
    private long start = -1;

    /**
     * Timer constructor. Doesn't start the timer, you need to do that manually.
     * @param seconds seconds to wait before notifying that the time's up
     * @param name timer title
     * @param repeat auto-repeat timer
     * @see #start()
     * @see ReminderTimer#startNew(int, String, boolean)
     * @see ReminderTimer#startNew(int, String)
     * @see #ReminderTimer(int, String)
     */
    public ReminderTimer(int seconds, String name, boolean repeat) {
        this.id = UUID.randomUUID();
        this.timer = new Timer(true);
        this.seconds = seconds;
        this.name = name;
        this.repeat = repeat;
    }

    /**
     * Non-repeating timer constructor. Doesn't start the timer, you need to do that manually.
     * @param seconds seconds to wait before notifying that the time's up
     * @param name timer title
     * @see #start()
     * @see ReminderTimer#startNew(int, String, boolean)
     * @see ReminderTimer#startNew(int, String)
     * @see #ReminderTimer(int, String, boolean)
     */
    public ReminderTimer(int seconds, String name){
        this(seconds, name, false);
    }

    /**
     * Starts timer thread, stores timer as {@link #active} & displays appropriate message.
     */
    public void start() {
        ModConfig.getInstance().reminder.msgTypeStart.msgWithPre(
                () -> TextUtil.getWithFormat(
                        Strings.isNullOrEmpty(name) ?
                                Component.translatable("msg.notifmod.reminder.start.long.unnamed", TextUtil.getWithFormat(NumUtil.secToHMSString(seconds), ChatFormatting.YELLOW)) :
                                Component.translatable("msg.notifmod.reminder.start.long.named", TextUtil.getWithFormat(NumUtil.secToHMSString(seconds), ChatFormatting.YELLOW), TextUtil.getWithFormat(name, ChatFormatting.YELLOW)),
                        ChatFormatting.AQUA),
                () -> TextUtil.getWithFormat(Component.translatable("msg.notifmod.reminder.start.short"), ChatFormatting.AQUA)
        );
        if(seconds > 0){
            active.put(id, this);
            start = getCurrSecs();
            long t = seconds * 1000L;
            if(repeat) timer.scheduleAtFixedRate(new Task(), t, t);
            else timer.schedule(new Task(), t);
        }else new Task().run();
    }

    /**
     * Makes new timer & runs {@link #start()} on it.
     * @param seconds seconds to wait before notifying that the time's up
     * @param name timer title
     * @param repeat auto-repeat timer
     */
    public static void startNew(int seconds, String name, boolean repeat) {
        new ReminderTimer(seconds, name, repeat).start();
    }

    /**
     * Makes new non-repeating timer & runs {@link #start()} on it.
     * @param seconds seconds to wait before notifying that the time's up
     * @param name timer title
     */
    public static void startNew(int seconds, String name) {
        startNew(seconds, name, false);
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
     * When the time runs out, it'll loop, to check if the timer is active, see {@link #isActive()}.
     * If it never started, it'll be -1.
     * @return remaining seconds or -1 if it never ran
     */
    public int getRemaining() {
        return start < 0 ? -1 : (int) (seconds - (getCurrSecs() - start - 1) % seconds - 1);
    }

    public boolean isActive(){
        return active.containsKey(id);
    }

    public boolean isRepeating() {
        return repeat;
    }

    public boolean hasName(){
        return !Strings.isNullOrEmpty(name);
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
            if(!ThreadUtils.isMainThread()) {
                ThreadUtils.execOnMainThread(this);
                return;
            }
            ModConfig.Reminder settings = ModConfig.getInstance().reminder;
            settings.msgTypeDone.msgWithPre(
                    () -> TextUtil.getWithFormat(
                            Strings.isNullOrEmpty(name) ?
                                    Component.translatable("msg.notifmod.reminder.done.unnamed") :
                                    Component.translatable("msg.notifmod.reminder.done.named", TextUtil.getWithFormat(name, ChatFormatting.YELLOW)),
                            ChatFormatting.GREEN),
                    () -> Strings.isNullOrEmpty(name) ? TextUtil.getWithFormat(Component.translatable("msg.notifmod.reminder.done.unnamed"), ChatFormatting.GREEN) : TextUtil.getWithFormat(name, ChatFormatting.GREEN)
            );
            settings.playSound();
            if(!repeat) active.remove(id);
        }
    }
}
