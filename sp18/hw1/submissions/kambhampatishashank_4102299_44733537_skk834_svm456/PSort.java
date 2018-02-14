/* Shashank Kambhampati: skk834
 * Shrikara Murthy: svm456
 */
import java.util.concurrent.*;

public class PSort {
    private static ForkJoinPool pool = ForkJoinPool.commonPool();

    public static void parallelSort(int[] A, int begin, int end) {
        if(end - begin <= 16) {
            insertionSort(A, begin, end);
            return;
        }

        int pivotIndex = partition(A, begin, end);
        
        ForkJoinTask<?> left = pool.submit(() -> parallelSort(A, begin, pivotIndex));
        ForkJoinTask<?> right = pool.submit(() -> parallelSort(A, pivotIndex + 1, end));

        left.join();
        right.join();
    }

    // Quicksort implementation. Chooses left side as pivot and partitions.
    private static int partition(int[] A, int begin, int end) {
        int pivot = begin;
        int i = begin;

        for(int j = begin + 1; j < end; j++) {
            if(A[j] < A[pivot]) {
                // swap A[i + 1] and A[j]
                int tmp = A[i + 1];
                A[i + 1] = A[j];
                A[j] = tmp;

                i++;
            }
        }

        // swap A[pivot] and A[i]
        int tmp = A[i];
        A[i] = A[pivot];
        A[pivot] = tmp;

        return i;
    }

    // Uses insertion sort to sort A. Used for small sized arrays.
    private static void insertionSort(int[] A, int begin, int end) {
        for(int i = begin + 1; i < end; i++) {
            for(int j = i; j > begin && A[j] < A[j - 1]; j--) {
                // swap A[j] and A[j - 1]
                int tmp = A[j];
                A[j] = A[j - 1];
                A[j - 1] = tmp;
            }
        }
    }
}
