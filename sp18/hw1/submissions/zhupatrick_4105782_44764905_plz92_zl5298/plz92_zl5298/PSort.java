/*
 * @file PSort.java
 * ----------
 * @author Ze Lyu, Patrick Zhu
 * zl5298, plz92
 * Feb 5, 2018
 */

import java.util.concurrent.ForkJoinPool;

public class PSort {
    
    /**
     * Description: API for this class to initialize quick sort on an array.
     * @param A: the array to be sorted.
     * @param begin: index of the beginning of range in the array to be sorted.
     * @param end: index of the end of range in the array to be sorted.
     */
    public static void parallelSort(int[] A, int begin, int end) {
        // create a ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        // create a quick sort task
        QuickSort task = new QuickSort(A, begin, end);
        // invoke the task in the ForkJoinPool
        forkJoinPool.invoke(task);
    }
    
}
