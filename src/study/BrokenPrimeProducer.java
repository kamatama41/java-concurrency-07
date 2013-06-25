package study;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * プロデューサーブロックに閉じ込められたままになる不確実なキャンセル
 * これをやってはいけません
 *
 * List 7-3
 */
@Deprecated
public class BrokenPrimeProducer extends Thread {
    private final BlockingQueue<BigInteger> queue;
    private volatile boolean cancelled = false;

    BrokenPrimeProducer(BlockingQueue<BigInteger> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            BigInteger p = BigInteger.ONE;
            while (!cancelled) {
                queue.put(p = p.nextProbablePrime());
                System.out.println("----- queue.put(" + p + ")");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        cancelled = true;
        System.out.println("----- thread cancelled.");
    }

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<BigInteger>(10);
        int count = 0;
        BrokenPrimeProducer producer = new BrokenPrimeProducer(primes);
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
