package study;

import java.util.concurrent.*;

/**
 * コード片置き場
 */
public class SandBox {

    public class Task {
    }

    /** List 7-7 */
    public Task getNextTask(BlockingQueue<Task> queue) {
        boolean interrupted = false;
        try {
            while(true) {
                try {
                    return queue.take();
                } catch (InterruptedException e) {
                    interrupted = true;
                    // このままループを続けて、再試行する
                }
            }
        } finally {
            if(interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** List 7-8 これをやってはダメ */
    private static final ScheduledExecutorService cancelExec = Executors.newScheduledThreadPool(3);

    public static void timedRunNG(Runnable r, long timeout, TimeUnit unit) {
        final Thread taskThread = Thread.currentThread();
        cancelExec.schedule(new Runnable() {
            @Override
            public void run() {
                taskThread.interrupt();
            }
        }, timeout, unit);
        r.run();
    }

    /** List 7-9 専用スレッドの中でタスクにインタラプトする */
    public static void timedRunOK(final Runnable r, long timeout, TimeUnit unit) throws InterruptedException {
        class RethrowableTask implements Runnable {
            private volatile Throwable t;
            @Override
            public void run() {
                try { r.run(); }
                catch (Throwable t) { this.t = t; }
            }
            void rethrow() {
                if(t != null) {
                    throw launderThrowable(t);
                }
            }
        }
        RethrowableTask task = new RethrowableTask();
        final Thread taskThread = new Thread(task);
        taskThread.start();
        // タスクがタイムアウトするようにスケジューリング
        cancelExec.schedule(new Runnable() {
            @Override
            public void run() {
                taskThread.interrupt();
            }
        }, timeout, unit);
        // インタラプトに対応しない場合の保険？？
        taskThread.join(unit.toMillis(timeout));
        // 例外を投げていたらthrowする
        task.rethrow();
    }

    private static RuntimeException launderThrowable(Throwable t) {
        return new RuntimeException(t);
    }

    /** List 7-10 Futureを使ってタスクをキャンセルする */
    private static final ScheduledExecutorService taskExec = Executors.newScheduledThreadPool(3);

    public static void timedRunOK2(Runnable r, long timeout, TimeUnit unit) throws InterruptedException{
        Future<?> task = taskExec.submit(r);
        try {
            task.get(timeout, unit);
        } catch (TimeoutException e) {
            // タスクはfinallyブロックでキャンセルされる
            e.printStackTrace();
        } catch (ExecutionException e) {
            // タスクの中で投げられた例外：再投する
            throw launderThrowable(e);
        } finally {
            // タスクが既に完了していたら無害
            task.cancel(true); // 実行中ならインタラプトする
        }
    }
}
