//UT-EID= nww295


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<Integer> {
    public int[] B;
    public int start;
    public int finish;

    public PSort(int A[], int begin, int end){
        this.B = A;
        this.start = begin;
        this.finish = end;
    }
    public static void parallelSort(int[] A, int begin, int end){
      int processors = Runtime.getRuntime().availableProcessors();
      System.out.println("Number of Processors: " + processors);
      PSort sort = new PSort(A,begin,end-1);
      ForkJoinPool pool = new ForkJoinPool(processors);
      int result = pool.invoke(sort);
      System.out.print("Result: ");
      for(int i = 0; i < sort.B.length; i++) {
          System.out.print(" " + sort.B[i]);
      }
      System.out.println();
    }

    protected Integer compute() {
        if(start < finish) {
            int split = partition(B, start, finish);
            PSort s1 = new PSort(B, start, split - 1);
            PSort s2 = new PSort(B, split + 1, finish);
            int one = s1.compute();
            int two = s2.compute();
        }
        return 0;
    }


    protected int partition(int[] A, int begin, int end) {
        int pivot = A[end];
        int i = begin-1;

        for(int j = begin; j <= end-1; j++) {
            if(A[j] <= pivot) {
                i++;
                int temp = A[j];
                A[j] = A[i];
                A[i] = temp;
            }
        }
        int temp = A[end];
        A[end] = A[i+1];
        A[i+1] = temp;
        return i+1;
    }
}
