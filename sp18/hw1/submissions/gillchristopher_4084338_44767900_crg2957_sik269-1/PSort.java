//UT-EID= crg2957, sik269
import java.util.*;
import java.util.concurrent.*;

public class PSort{
    public static void parallelSort(int[] A, int begin, int end){
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool forkJoinPool = new ForkJoinPool(processors);
        QuickSort task = new QuickSort(A, begin, end-1);

        forkJoinPool.invoke(task);
    }
}