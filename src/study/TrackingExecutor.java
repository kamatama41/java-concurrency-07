package study;

import java.util.*;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * キャンセルされたタスクをシャットダウン後に記録するExecutorService
 *
 * List 7-21
 */
public class TrackingExecutor extends AbstractExecutorService {
    private final ExecutorService exec;
    private final Set<Runnable> tasksCancelledAtShutdown = Collections.synchronizedSet(new HashSet<Runnable>());

    public TrackingExecutor(ExecutorService exec) {
        this.exec = exec;
    }

    public List<Runnable> getCancelledTasks() {
        if(!exec.isTerminated()) {
            throw new IllegalStateException("task is not terminated.");
        }
        return new ArrayList<Runnable>(tasksCancelledAtShutdown);
    }

    @Override
    public void execute(final Runnable command) {
        if(!exec.isTerminated()) {
            throw new IllegalStateException("Service is not terminated.");
        }
        exec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    command.run();
                } finally {
                    if(isShutdown() && Thread.currentThread().isInterrupted()) {
                        tasksCancelledAtShutdown.add(command);
                    }
                }
            }
        });
    }

    @Override
    public void shutdown() {
        exec.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return exec.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return exec.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return exec.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return exec.awaitTermination(timeout, unit);
    }

}
