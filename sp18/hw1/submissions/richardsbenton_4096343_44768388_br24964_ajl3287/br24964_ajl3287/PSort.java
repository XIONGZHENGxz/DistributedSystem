//UT-EID=br24964, ajl3287

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class PSort {

    public static void parallelSort(int[] A, int begin, int end) {
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool threadPool = new ForkJoinPool(processors);
        Sorter sort = new Sorter(A, begin, end);
        threadPool.invoke(sort);
    }

    public static void parallelSort(int[] A) {
        parallelSort(A, 0, A.length);
    }

    private static class Sorter extends RecursiveAction {
        private int[] A;
        private int beg;
        private int end;

        Sorter(int[] A, int beg, int end) {
            this.A = A;
            this.beg = beg;
            this.end = end - 1;
        }

        private void insertionSort(int[] A, int beg, int end) {
            for (int i = beg + 1; i <= end; i++) {
                int j = i;
                while (j > 0 && A[j] < A[j - 1]) {
                    int temp = A[j];
                    A[j] = A[j - 1];
                    A[j - 1] = temp;
                    j--;
                }
            }
        }

        private int partition(int[] A, int beg, int end) {
            int pivot = A[end];
            int wall = beg - 1;
            for (int i = beg; i < end; i++) {
                if (A[i] < pivot) {
                    wall++;
                    //swap with element
                    int temp = A[wall];
                    A[wall] = A[i];
                    A[i] = temp;
                }
            }
            wall++;
            A[end] = A[wall];
            A[wall] = pivot;
            return wall;
        }

        @Override
        protected void compute() {
            if (end - beg <= 16) {
                insertionSort(A, beg, end);
                return;
            }

            int wall = partition(A, beg, end);
            while (wall <= beg + 1 && beg < end - 1) {
                beg++;
                wall = partition(A, beg, end);
            }


            ForkJoinTask left = new Sorter(A, beg, wall).fork();
            new Sorter(A, wall - 1, end).compute();
            left.join();
        }
    }
}
