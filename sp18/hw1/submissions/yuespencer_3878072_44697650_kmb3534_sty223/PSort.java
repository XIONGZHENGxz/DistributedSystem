//UT-EID=kmb3534, sty223

import java.util.*;
import java.util.concurrent.*;

public class PSort {
  public static void parallelSort(int[] A, int begin, int end) {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    QuickSort qs = new QuickSort(A, begin, end, new Random ());
    forkJoinPool.invoke(qs);
  }
}

class QuickSort extends RecursiveTask<int[]> {
  int[] A;
  int begin, end, depth;
  Random generator;

  public QuickSort(int[] A, int begin, int end, Random generator) {
    this.A = A;
    this.begin = begin;
    this.end = end;
    this.generator = generator;
  }

  @Override
  protected int[] compute() {
    if (begin == end) {
      return A;
    }
    if (end - begin <= 16) {
      return seqInsertSort(A, begin, end);
    } else {
      int index = getSplitIndex(A, begin, end);
      QuickSort qs1 = new QuickSort(A, begin, index, this.generator);
      qs1.fork();
      QuickSort qs2 = new QuickSort(A, index + 1, end, this.generator);
      qs2.compute();
      qs1.join();

      return A;
    }
  }

  int getSplitIndex(int[] A, int begin, int end) {
    int pivot = begin + this.generator.nextInt (end - begin);
    int i = begin - 1;
    for (int j = begin; j < end; j++) {
      if (A[j] < A[pivot]) {
        int swap = A[++i];
        A[i] = A[j];
        A[j] = swap;
        if (i == pivot)
          pivot = j;
      }
    }
    if (A[pivot] < A[i + 1]) {
      int swap = A[pivot];
      A[pivot] = A[i + 1];
      A[i + 1] = swap;
    }
    return i + 1;
  }

  int[] seqInsertSort(int[] A, int begin, int end) {
    for (int i = begin + 1; i < end; i++) {
      int j = i - 1;
      int current = A[i];
      while (j >= begin && current < A[j]) {
        A[j + 1] = A[j];
        A[j] = current;
        j--;
      }
    }
    return A;
  }
}
