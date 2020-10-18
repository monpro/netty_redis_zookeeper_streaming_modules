package common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ExecutorHelper {

    public static ExecutorService createExecutor(int nThreads, String threadNamePrefix) {
        return Executors.newFixedThreadPool(nThreads, threadNameThreadFactory(threadNamePrefix));
    }

    public static ThreadFactory threadNameThreadFactory(String threadNamePrefix) {
        return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
    }

}
