//UT-EID=apl652, rs48368


import java.util.concurrent.*;
public class PSort{

  public static void parallelSort(int[] A, int begin, int end) {
      int numProcessors = Runtime.getRuntime().availableProcessors();
      ForkJoinPool pool = new ForkJoinPool(numProcessors);
      Quicksort quick = new Quicksort(A, begin, end);
      pool.invoke(quick);
  }

    /**
     * Quicksort class for encapsulation and use of ForkJoinPool
     */
  private static class Quicksort extends RecursiveAction {
      final int begin, end;
      private int[] array;

      public Quicksort(int[] A, int begin, int end) {
          this.array = A;
          this.begin = begin;
          this.end = end;
      }

      protected void compute() {
          if (end - begin <= 16) {
              insertionSort(begin, end);
              return;
          }

          int mid = partition();
          Quicksort left = new Quicksort(array, begin, mid);
          left.fork();
          Quicksort right = new Quicksort(array, mid + 1, end);
          right.compute();

          left.join();
      }

      /**
       * Partitions this.array using the last value as a pivot
       * @return index of the pivot after partitioning
       */
      protected int partition() {
          int pivot = array[end - 1];
          int low = begin - 1;

          for (int i = begin; i <= end - 1; i++) {
              if (array[i] < pivot) {
                  low++;
                  swap(low, i);
              }
          }
          swap(low+1, end-1);
          return (low + 1);
      }

        /**
         * Swaps index1 with index2 in this.array
         * @param index1
         * @param index2
         */
      protected void swap(int index1, int index2) {
          int temp = array[index1];
          array[index1] = array[index2];
          array[index2] = temp;
      }

        /**
         * Performs insertion sort between [begin, end) of this.array
         * @param begin
         * @param end
         */
      protected void insertionSort(int begin, int end) {
          int i = begin + 1;

          while (i < end) {
              int val = array[i];
              int j = i - 1;

              while (j >= begin && array[j] > val) {
                  swap(j, j+1);
                  j--;
              }
              i++;
          }
      }
  }
}
