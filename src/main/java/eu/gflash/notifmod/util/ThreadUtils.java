package eu.gflash.notifmod.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.thread.ThreadExecutor;

/**
 * Thread related utility functions.
 * @author Alex811
 */
public abstract class ThreadUtils {
    public static void execOnMainThread(ThreadExecutor<?> engine, Runnable runnable){
        if(engine.isOnThread()) runnable.run();
        else engine.execute(runnable);
    }

    public static void execOnMainThread(Runnable runnable){
        execOnMainThread(MinecraftClient.getInstance(), runnable);
    }
}
