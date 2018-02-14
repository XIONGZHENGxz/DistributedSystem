//UT-EID= man2766, hhg263

import com.sun.javafx.runtime.async.BackgroundExecutor;

import java.util.concurrent.*;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{
    int begin;
    int end;
    int[] A;

  PSort(int[] A, int begin, int end) {
      this.begin = begin;
      this.end = end;
      this.A = A;
  }

  public static void parallelSort(int[] A, int begin, int end) {
      int processors = Runtime.getRuntime().availableProcessors();
      PSort p = new PSort(A, begin, end-1);
      ForkJoinPool pool = new ForkJoinPool(processors);
      pool.invoke(p);
  }

  public static void insertionSort(int[] A, int n) {
    if (n > 0) {
      insertionSort(A, n-1);
      int x = A[n];
      int j = n-1;

      while (j >= 0 && A[j] > x) {
        A[j+1] = A[j];
        j--;

      }

      A[j+1] = x;
    }

  }

  public static void quickSort(int[] A, int begin, int end) {
      if (begin < end) {
          int p = partition(A, begin, end);
          quickSort(A, begin, p-1);
          quickSort(A, p+1, end);

      }

  }

  public static int partition(int[]A, int lo, int hi) {
      int pivot = A[hi];
      int i = lo-1;
      for (int j = lo; j < hi; j++) {
          if (j < hi+1 && A[j] < pivot) {
              i++;
              Point temp = swap(A[i], A[j]);
              A[i] = temp.x;
              A[j] = temp.y;
          }
      }

      if (A[hi]< A[i+1]) {
          Point temp = swap(A[i+1], A[hi]);
          A[i+1] = temp.x;
          A[hi] = temp.y;

      }

      return i+1;
  }

  public static Point swap(int a, int b) {
      return new Point(b, a);

  }

    @Override
    protected void compute() {
        if (begin < end) {
            int p = partition(A, begin, end);
            PSort p1 = new PSort(A, begin, p-1);
            PSort p2 = new PSort(A, p+1, end);
            invokeAll(p1, p2);

        }
    }
}
