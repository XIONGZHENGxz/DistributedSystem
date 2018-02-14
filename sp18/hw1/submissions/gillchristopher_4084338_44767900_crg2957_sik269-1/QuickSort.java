import java.util.concurrent.RecursiveAction;

public class QuickSort extends RecursiveAction{
    private int[] A;
    private int left;
    private int right;

    public QuickSort(int[] A, int left, int right) {
        this.A = A;
        this.left = left;
        this.right = right;
    }

    @Override
    protected void compute() {
        if (left < right) {
            if (right - left < 16) {
                insertionSort(left, right);
            }
            else {
                int pivotIndex = partition(A, left, right);

                QuickSort task1 = new QuickSort(A, left, pivotIndex - 1);
                QuickSort task2 = new QuickSort(A, pivotIndex + 1, right);

                invokeAll(task1, task2);
            }
        }
    }

    int partition(int[] A, int left, int right) {
        int pivotValue = A[right];
        int i = left - 1;
        for (int j = left; j < right; j++) {
            if (A[j] < pivotValue) {
                i = i + 1;
                swap(i, j);
            }
        }

        if (A[right] < A[i + 1]) {
            swap(i + 1, right);
        }
        return i + 1;
    }

    void swap(int left, int right) {

        int valueHolder = A[left];

        A[left] = A[right];
        A[right] = valueHolder;
    }

    void insertionSort(int left, int right) {
        int i = 1;
        while (i < right - left + 1) {
            int j = i + left;
            while (j > 0 && A[j-1] > A[j]) {
                swap(j, j-1);
                j = j-1;
            }
            i = i + 1;
        }
    }
}