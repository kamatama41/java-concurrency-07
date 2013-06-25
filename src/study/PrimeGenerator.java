package study;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 素数を生成するタスク
 *
 * List 7-1
 */
public class PrimeGenerator implements Runnable {
    private final List<BigInteger> primes = new ArrayList<BigInteger>();
    // volatile フィールドを使ってキャンセルのステートを保持する
    private volatile boolean cancelled;

    @Override
    public void run() {
        BigInteger p = BigInteger.ONE;
        while (!cancelled) {
            p = p.nextProbablePrime();
            synchronized (this) {
                primes.add(p);
            }
        }
    }

    public void cancel() { cancelled = true; }

    public synchronized List<BigInteger> get() {
        return new ArrayList<BigInteger>(primes);
    }


    /**
     * 素数を1秒間生成する
     *
     * List 7-2
     */
    public static void main(String[] args) throws InterruptedException {
        for (BigInteger prime : aSecondOfPrimes()) {
            System.out.println(prime);
        }
    }
    private static List<BigInteger> aSecondOfPrimes() throws InterruptedException {
        PrimeGenerator primeGenerator = new PrimeGenerator();
        new Thread(primeGenerator).start();
        try {
            // 1秒まつ
            TimeUnit.SECONDS.sleep(1);
        } finally {
            primeGenerator.cancel();
        }
        return primeGenerator.get();
    }
}
