package common.concurrency;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeoutHandler {

    private static ScheduledExecutorService timer = createdScheduledThreadPool("timer", 2);

    private static ScheduledExecutorService createdScheduledThreadPool(String name, int threadNum) {
        return Executors.newScheduledThreadPool(threadNum,
                new ThreadFactoryBuilder().setNameFormat(name + "-%d").build());
    }

    public static <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit timeUnit) {
        CompletableFuture<T> result = new CompletableFuture<>();
        timer.schedule(() -> result.complete(null), timeout, null);
        return result;
    }
}
