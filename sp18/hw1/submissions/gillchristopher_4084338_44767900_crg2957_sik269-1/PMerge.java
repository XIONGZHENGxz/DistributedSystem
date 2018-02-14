//UT-EID= crg2957, sik269
import java.util.*;
import java.util.concurrent.*;


public class PMerge {
    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < A.length; i++) {

            Merge task = new Merge(A, B, C, true, i);
            executorService.submit(task);
        }

        for (int i = 0; i < B.length; i++) {

            Merge task = new Merge(A, B, C, false, i);
            executorService.submit(task);
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}