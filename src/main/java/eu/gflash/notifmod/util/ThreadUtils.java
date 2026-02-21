package eu.gflash.notifmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.BlockableEventLoop;

/**
 * Thread related utility functions.
 * @author Alex811
 */
public abstract class ThreadUtils {
    /**
     * Executes the {@code runnable} if currently on {@code engine}'s thread, otherwise queues it to run on it.
     * @param engine the {@link BlockableEventLoop} on whose thread to run on
     * @param runnable what to run on the thread
     * @see #execOnMainThread(Runnable)
     */
    public static void execOnThread(BlockableEventLoop<?> engine, Runnable runnable){
        if(engine.isSameThread()) runnable.run();
        else engine.execute(runnable);
    }

    /**
     * Executes the {@code runnable} if on the main thread, otherwise queues it to run on it.
     * Useful, for example, when we're on a networking related thread and need to play a sound, to avoid {@link java.util.ConcurrentModificationException} or similar.
     * @param runnable what to run on the main thread
     * @see #execOnThread(BlockableEventLoop, Runnable)
     */
    public static void execOnMainThread(Runnable runnable){
        execOnThread(Minecraft.getInstance(), runnable);
    }

    /**
     * Check if the current thread is the main thread.
     * @return true if on main thread
     */
    public static boolean isMainThread(){
        return Minecraft.getInstance().isSameThread();
    }
}
