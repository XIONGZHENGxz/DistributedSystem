//UT-EID= CV7999, bsk524


import java.util.*;
import java.util.concurrent.*;

public class PMerge{
    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        if(numThreads <= 0){
            return;
        }
        if(A.length == 0){
            C = B;
            return;
        }
        if(B.length == 0){
            C = A;
            return;
        }
        if(numThreads > A.length){
            numThreads = A.length;
        }
        int[] splitA = new int[numThreads];
        int[] splitB = new int[numThreads];

        splitA = splitArrayA(A, splitA, numThreads);
        ConcurrentMerge CM = new ConcurrentMerge(A, B, splitA, splitB, C, 0);
        ForkJoinPool threadPool = new ForkJoinPool(numThreads);
        threadPool.invoke(CM);
    }

    /*
     * This method should take in array A and enter in the
     * LAST index of each "subarray" into dividedA. For instance:
     *
     * dividedA[0] contains the last index of array A that thread0 would
     * process (so it would look at the range:= 0 to dividedA[0] inclusive)
     */
    private static int[] splitArrayA(int[] A, int[] dividedA, int num) {
        int divide = A.length/num;
        if(A.length % num != 0 && num % 2 == 1){
            divide++;
        }
        int i = -1;
        int count = 0;
        while(count < dividedA.length){
            dividedA[count] = i + divide;
            count++;
            i+=divide;
        }
        if(num % 2 == 1){
            dividedA[count - 1] = A.length - 1;
        }
        return dividedA;
    }
}