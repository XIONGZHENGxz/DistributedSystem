//UT-EID=naa968

import java.util.*;
import java.util.concurrent.*;

public class PSort {

    private static Random randGenerator = new Random();

    public static void parallelSort(int[] A, int begin, int end) {
        if (begin < 0 || begin >= A.length || end < begin || end > A.length) {
            System.err.println("Invalid Indices");
        } else {
            (new ForkJoinPool(Runtime.getRuntime().availableProcessors()))
                    .invoke(new QuickSort(A, begin, end-1));
        }
    }

    private static void quickSort(int[] A, int low, int high) {
        if (low < high) {
            int p = partition(A, low, high);
            quickSort(A, low, p);
            quickSort(A, p+1, high);
        }
    }

    private static int partition(int[] A, int low, int high) {
        int pivot = A[randRange(low, high)];
        int i = low-1;
        int j = high+1;

        while (i < j) {
            while (A[++i] < pivot);
            while (A[--j] > pivot);

            if (i < j) {
                synchronized (A) {
                    swap(A, i, j);
                }
            }
        }

        return j;
    }

    private static void swap(int[] A, int i, int j) {
        int temp = A[i];
        A[i] = A[j];
        A[j] = temp;
    }

    private static int randRange(int min, int max) {
        if (min > max) return -1;
        return randGenerator.nextInt(max - min + 1) + min;
    }

    private static class QuickSort extends RecursiveAction {

        private static final int THRESHOLD = 16;
        private int[] A;
        private int low, high;

        QuickSort(int[] A, int low, int high) {
            this.A = A;
            this.low = low;
            this.high = high;
        }

        @Override
        protected void compute() {
            if (low < high) {
                int p = partition(A, low, high);
                if (high - low - 1 <= THRESHOLD) {
                    quickSort(A, low, p);
                    quickSort(A, p+1, high);
                } else {
                    invokeAll(new QuickSort(A, low, p),
                              new QuickSort(A, p+1, high));
                }
            }
        }
    }
}
