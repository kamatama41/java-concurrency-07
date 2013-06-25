package study;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * インタラプションをつかってキャンセルする
 *
 * List 7-5
 */
public class PrimeProducer extends Thread {
    private final BlockingQueue<BigInteger> queue;

    public PrimeProducer(BlockingQueue<BigInteger> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            BigInteger p = BigInteger.ONE;
            while (!Thread.currentThread().isInterrupted()) {
                queue.put(p = p.nextProbablePrime());
                System.out.println("----- queue.put(" + p + ")");
            }
        } catch (InterruptedException e) {
            /* スレッドを終わらせる */
            System.out.println("----- thread interrupted.");
        }
        System.out.println("----- thread end.");
    }

    public void cancel() {
        super.interrupt();
        System.out.println("thread cancelled.");
    }

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<BigInteger>(10);
        int count = 0;
        PrimeProducer producer = new PrimeProducer(primes);
        producer.start();
        try {
            while (count < 50) {
                // 分かりやすいように100ミリ秒まつ
                TimeUnit.MILLISECONDS.sleep(100);
                System.out.println("primes.take(" + primes.take() + ")");
                count ++;
            }
        } finally {
            // ブロッキングが成功するように100ミリ秒まつ
            TimeUnit.MILLISECONDS.sleep(100);
            producer.cancel();
        }

    }
}
