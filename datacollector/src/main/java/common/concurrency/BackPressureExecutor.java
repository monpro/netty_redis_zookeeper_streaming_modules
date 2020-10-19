package common.concurrency;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BackPressureExecutor implements ExecutorService {
    private static final Logger logger = LoggerFactory.getLogger(BackPressureExecutor.class);
    private final List<ExecutorService> executors;
    private final Partitioner partitioner;
    private Long rejectSleepMills = 1L;

    public BackPressureExecutor(String name, int executorNumber, int coreSize, int maxSize, int capacity, long rejectSleepMills) {
        Preconditions.checkArgument(executorNumber > 0, "executorNumber should be positive");
        Preconditions.checkArgument(coreSize > 0, "coreSize should be positive");
        Preconditions.checkArgument(maxSize > 0, "maxSize should be positive");
        Preconditions.checkArgument(capacity > 0, "capacity should be positive");
        this.rejectSleepMills = rejectSleepMills;
        this.executors = new ArrayList<>(executorNumber);
        for(int i = 0; i < executorNumber; i++) {
            ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(capacity);
            // when queue is full, use the abort policy, throw RejectedExecutionException
            this.executors.add(new ThreadPoolExecutor(
                    coreSize, maxSize, 0L, TimeUnit.MILLISECONDS,
                    queue,
                    new ThreadFactoryBuilder().setNameFormat(name + "-" + i + "-%d").build(),
                    new ThreadPoolExecutor.AbortPolicy()));
        }
        this.partitioner = new RoundRobinPartitionSelector(executorNumber);
    }

    private interface Partitioner {
        int getPartition();
    }

    // helper class implement RoundRobinPartition algorithm
    private static class RoundRobinPartitionSelector implements Partitioner {

        private final int partitions;
        private static PositiveAtomicCounter counter = new PositiveAtomicCounter();

        RoundRobinPartitionSelector(int partitions) {
            this.partitions = partitions;
        }

        @Override
        public int getPartition() {
            return counter.incrementAndGet() % partitions;
        }
    }
    // helper class
    private static class PositiveAtomicCounter {
        private final AtomicInteger atomicInteger;
        // largest number in java
        private static final int mask = 0x7FFFFFFF;

        PositiveAtomicCounter() {
            this.atomicInteger = new AtomicInteger(0);
        }

        public final int incrementAndGet() {
            final int result = atomicInteger.incrementAndGet();
            // always get a positive value
            return result & mask;
        }

        public int intValue() {
            return atomicInteger.intValue();
        }
    }



    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public void execute(Runnable command) {

    }
}
