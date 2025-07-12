package wtf.moonlight.util.misc;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Multithreading {
    private static final ScheduledExecutorService RUNNABLE_POOL = Executors.newScheduledThreadPool(3, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "Multithreading Thread " + counter.incrementAndGet());
        }
    });

    public static ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "Multithreading Thread " + counter.incrementAndGet());
        }
    });


    public static void schedule(Runnable r, long delay, TimeUnit unit) {
        Multithreading.RUNNABLE_POOL.schedule(r, delay, unit);
    }

    public static int getTotal() {
        return ((ThreadPoolExecutor) Multithreading.POOL).getActiveCount();
    }

    public static void runAsync(Runnable runnable) {
        POOL.execute(runnable);
    }
}