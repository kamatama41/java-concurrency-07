package study;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.*;

/**
 * ExecutorServiceを使うログサービス
 *
 * List 7-16
 */
public class LogExecutionService {
    private static final int TIMEOUT = 10;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    private final ExecutorService exec;
    private final PrintWriter writer;

    public LogExecutionService(Writer writer) {
        this.writer = new PrintWriter(writer);
        this.exec = Executors.newScheduledThreadPool(3);
    }

    public void start() {
    }

    public void stop() throws InterruptedException {
        try {
            exec.shutdown();
            exec.awaitTermination(TIMEOUT, UNIT);
        } finally {
            writer.close();
        }
    }

    public void log(String msg) throws InterruptedException {
        try {
            exec.execute(new WriteTask(msg));
        } finally {

        }
    }

    public class WriteTask implements Runnable {
        private final String msg;

        public WriteTask(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            writer.println(msg);
        }
    }
}
