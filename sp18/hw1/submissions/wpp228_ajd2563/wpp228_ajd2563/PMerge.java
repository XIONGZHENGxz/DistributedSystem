
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;


public class PMerge {

    private static ForkJoinPool pool;

    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        pool = new ForkJoinPool(numThreads);

        ForkJoinTask taskA = ForkJoinTask.adapt(new PMergeSplitTask(A, B, C, 0));
        ForkJoinTask taskB = ForkJoinTask.adapt(new PMergeSplitTask(B, A, C, 1));
        
        pool.execute(taskA);
        pool.execute(taskB);

        taskA.join();
        taskB.join();
    }
}
