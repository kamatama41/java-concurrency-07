package study;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * シャットダウンのサポートの無いプロデューサー・コンシューマ型のログサービス
 *
 * List 7-13
 */
public class LogWriter {
    private static final int CAPACITY = 10;
    private final BlockingQueue<String> queue;
    private final LoggerThread logger;

    public LogWriter(Writer writer) {
        this.logger = new LoggerThread(writer);
        this.queue = new LinkedBlockingDeque<String>(CAPACITY);
    }

    public void start() {
        logger.start();
    }

    public void log(String msg) throws InterruptedException {
        queue.put(msg);
    }

    /** List 7-14 ログサービスにシャットダウンのサポートを加える不安定な方法 */
    private boolean shutdownRequested = false;
    public void logBad(String msg) throws InterruptedException {
        if(!shutdownRequested) {
            queue.put(msg);
        } else {
            throw new IllegalStateException("logger is shut down.");
        }
    }


    public class LoggerThread extends Thread {
        private final PrintWriter writer;

        public LoggerThread(Writer writer) {
            this.writer = new PrintWriter(writer);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    writer.println(queue.take());
                }
            } catch (InterruptedException e) {
                // 無視
                e.printStackTrace();
            } finally {
                writer.close();
            }
        }
    }
}
