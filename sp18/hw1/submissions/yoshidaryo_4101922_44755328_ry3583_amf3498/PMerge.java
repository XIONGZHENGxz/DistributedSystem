/*
 * Ali Fakhreddine
 * amf3498
 *
 * Ryo Yoshida
 * ry3583
 *
 */


import java.util.*;
import java.util.concurrent.*;

public class PMerge implements Runnable {
    private int[] A;
    private int[] B;
    private int[] C;
    private int num;
    private boolean isA;

    public PMerge(int[] A, int[] B, int[] C, int num, boolean isA) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.num = num;
        this.isA = isA;
    }

    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) {
        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < A.length; i ++) {
            threadPool.execute(new PMerge(A, B, C, A[i], true));
        }
        for (int i = 0; i < B.length; i ++) {
            threadPool.execute(new PMerge(A, B, C, B[i], false));
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run()  {
        int count = 0;
        for (int i = 0; i < A.length; i++) {
            if (A[i] < num) {
                count++;
            }
        }
        for (int j = 0; j < B.length; j++) {
            if (B[j] < num || (B[j] <= num && isA)) {
                count++;
            }
        }
        C[count] = num;
    }
}