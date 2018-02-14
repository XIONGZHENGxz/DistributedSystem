
/* EE360P Assignment 1
   Hasan Genc: hng298
   Jared Cormier: jcc4969
   Section: 15825
*/

import java.util.*;
import java.util.concurrent.*;

public class PMerge extends RecursiveAction {
    
    
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) {
      ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);
      
      PMerge mra = new PMerge(A, B, C, numThreads, 0, A.length, 0, B.length, 0);
      
      forkJoinPool.invoke(mra);
  }
  
      private int[] A;
      private int[] B;
      private int[] C;
      private int numThreads;
      private int a_start;
      private int a_end;
      private int b_start;
      private int b_end;
      private int c_start;
      
      public PMerge(int[] A, int[] B, int[] C, int numThreads, int a_start, int a_end, int b_start, int b_end, int c_start) {
          this.A = A;
          this.B = B;
          this.C = C;
          this.numThreads = numThreads;
          this.a_start = a_start;
          this.a_end = a_end;
          this.b_start = b_start;
          this.b_end = b_end;
          this.c_start = c_start;
      }
      
      @Override
      protected void compute() {
          int[] smaller;
          int[] bigger;

          int smaller_start, smaller_end;
          int bigger_start, bigger_end;

          if (a_end - a_start < b_end - b_start) {
              smaller = A;
              bigger = B;
              smaller_start = a_start;
              smaller_end = a_end;
              bigger_start = b_start;
              bigger_end = b_end;
          } else {
              smaller = B;
              bigger = A;
              smaller_start = b_start;
              smaller_end = b_end;
              bigger_start = a_start;
              bigger_end = a_end;
          }

          int smaller_len = smaller_end - smaller_start;
          int bigger_len = bigger_end - bigger_start;

          if (bigger_len <= 0)
              return;

          int mid_index = (bigger_start + bigger_end) / 2;
          int smaller_index = binarySearch(bigger[mid_index], smaller, smaller_start, smaller_end);
          int C_index = c_start + (mid_index-bigger_start) + (smaller_index-smaller_start);

          C[C_index] = bigger[mid_index];

          if (numThreads == 1) {
              PMerge task1 = new PMerge(bigger, smaller, C, 1, bigger_start, mid_index, smaller_start, smaller_index, c_start);
              PMerge task2 = new PMerge(bigger, smaller, C, 1, mid_index+1, bigger_end, smaller_index, smaller_end, C_index+1);
              
              task1.compute();
              task2.compute();
          } else {
              int numThreads1 = numThreads / 2 + (numThreads%2);
              int numThreads2 = numThreads / 2;
              
              PMerge task1 = new PMerge(bigger, smaller, C, numThreads1, bigger_start, mid_index, smaller_start, smaller_index, c_start);
              PMerge task2 = new PMerge(bigger, smaller, C, numThreads2, mid_index+1, bigger_end, smaller_index, smaller_end, C_index+1);
              
              task1.fork();
              task2.fork();
              
              task1.join();
              task2.join();
          }
      }
      
    public static int binarySearch(int a, int[] B, int b_start, int b_end) {
      int low = b_start;
      int high = b_end;

      while (low < high) {
          int mid = (low+high)/2;

          if (a <= B[mid])
              high = mid;
          else
              low = mid+1;
      }

      return low;
    }
}
