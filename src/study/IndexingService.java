package study;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.BlockingQueue;

/**
 * 毒薬によるシャットダウン
 *
 * List 7-17
 */
public class IndexingService {
    private static final File POISON = new File("");
    private final IndexerThread consumer = new IndexerThread();
    private final CrawlerThread producer = new CrawlerThread();

    private final BlockingQueue<File> queue;
    private final FileFilter fileFilter;
    private final File root;

    public IndexingService(BlockingQueue<File> queue, FileFilter fileFilter, File root) {
        this.queue = queue;
        this.fileFilter = fileFilter;
        this.root = root;
    }

    public void start() {
        producer.start();
        consumer.start();
    }

    public void stop() {
        producer.interrupt();
    }

    public void awaitTermination() {
        producer.interrupt();
    }

    /** コンシューマスレッド */
    class IndexerThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    File file = queue.take();
                    if(file == POISON) {
                        break;
                    } else {
                        indexFile(file);
                    }
                }
            } catch (InterruptedException e) {
                // 無視
                e.printStackTrace();
            }
        }
        private void indexFile(File file) {
            // FIXME
        }
    }

    /** プロデューサスレッド */
    class CrawlerThread extends Thread {
        @Override
        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) {
                /* finallyにいく */
            } finally {
                while (true) {
                    try {
                        queue.put(POISON);
                        break;
                    } catch (InterruptedException e1) {
                        /* 再試行する */
                    }
                }
            }
        }
        private void crawl(File root) throws InterruptedException{
            // FIXME
        }
    }
}
