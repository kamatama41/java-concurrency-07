package study;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * LogWriterに安定的なキャンセルを加える
 *
 * List 7-15
 */
public class LogService {
    private static final int CAPACITY = 10;

    private final BlockingQueue<String> queue;
    private final LoggerThread loggerThread;
    private final PrintWriter writer;
    private boolean isShutdown;
    private int reserveations;

    public LogService(Writer writer) {
        this.queue = new LinkedBlockingQueue<String>(CAPACITY);
        this.loggerThread = new LoggerThread();
        this.writer = new PrintWriter(writer);
    }

    public void start() {
        loggerThread.start();
    }

    public void stop() {
        synchronized (this) {
            isShutdown = true;
        }
        loggerThread.interrupt();
    }

    public void log(String msg) throws InterruptedException {
        /* シャットダウンチェックをアトミックにすることでqueue.putのブロック待ちを防ぐ */
        synchronized (this) {
            if(isShutdown) {
                throw new IllegalStateException("logger is shut down.");
            }
            ++reserveations;
        }
        queue.put(msg);
    }

    public class LoggerThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    try {
                        // シャットダウンチェックを同期化する
                        synchronized (this) {
                            if(isShutdown && reserveations == 0) {
                                break;
                            }
                        }
                        String msg = queue.take();
                        synchronized (this) { --reserveations; }
                        writer.println(msg);
                    } catch (InterruptedException e) {
                        /* 再試行する */
                    }
                }
            } finally {
                writer.close();
            }
        }
    }
}
