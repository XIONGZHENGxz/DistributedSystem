import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PSort extends RecursiveTask<int[]>{
    final int[] arr;
    int begin, end;

    PSort(int[] arr, int begin, int end) {
        this.arr = arr;
        this.begin = begin;
        this.end = end;
    }
    public static void parallelSort(int[] A, int begin, int end) {
        int processors = Runtime.getRuntime().availableProcessors();
        PSort ps = new PSort(A, begin, end - 1);

        if (A.length <= 16) {
            // insertion sort
            int[] result = ps.insertionSort();
            System.out.println("Result: " + Arrays.toString(result));
        } else {
            // quick sort
            ForkJoinPool pool = new ForkJoinPool(processors);
            int[] result = pool.invoke(ps);
            System.out.println("Result: " + Arrays.toString(result));
        }
    }

    private int[] insertionSort() {
        for (int i = 0; i < arr.length; i++) {
            int current = arr[i];
            int currentIndex = i;

            for (int j = i - 1; j >= 0; j--) {
                if (current > arr[j]) {
                    break;
                } else {
                    swap(currentIndex , j);
                    currentIndex--;
                }
            }
        }
        return arr;
    }

    @Override
    protected int[] compute() {
        if (begin >= end) return arr;

        int pivot = arr[end];
        int wall = begin - 1;

        for (int i = begin; i <= end; i++) {
            if (arr[i] < pivot) {
                wall++;
                swap(wall, i);
            }
        }

        wall++;
        swap(wall, end);

        PSort ps1 = new PSort(arr, begin, wall - 1);
        ps1.fork();

        PSort ps2 = new PSort(arr, wall + 1, end);
        ps2.compute();
        ps1.join();

        return arr;
    }

    void swap(int indexA, int indexB) {
        int temp = arr[indexB];
        arr[indexB] = arr[indexA];
        arr[indexA] = temp;
    }
}
