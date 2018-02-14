//UT-EID=jdk2595, aed2395


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class PSort extends RecursiveAction{
    private int[] arr;
    private int start;
    private int end;

    private PSort(int[] a, int begin, int end) {
        this.arr = a;
        this.start = begin;
        this.end = end;
    }
    public static void parallelSort(int[] A, int begin, int end){
        // TODO: Implement your parallel sort function
        PSort p1 = new PSort(A,begin,end-1);
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        pool.invoke(p1);
        pool.shutdown();
        while(!pool.isTerminated()) {}
    }

    // Implement quick sort recursive/parallel style. If the portion of the array gets below 16 unit long it goes to
    // a simple implementation of insertion sort.
    @Override
    public void compute() {
        if(end - start <= 16) {
            insertSort();
        }
        else {

            if(this.start < this.end) {
                int point = partition();
                //split array in two and quicksort them
                PSort p1 = new PSort(this.arr, this.start, point - 1);
                PSort p2 = new PSort(this.arr, point + 1, this.end);

                p1.fork();
                p2.fork();
            }

        }
    }

    //Simple insert sort of the current objects given portion of the array
    private void insertSort() {
        for(int i = start + 1; i < end + 1; i++) {
            for(int j = i; j > 0 && arr[j-1] > arr[j];j--) {
                swap(j,j-1);
            }
        }
    }

    // Quick sort relies on a partioning algorithm. This is an implementation of the Lomuto partition scheme.
    private int partition() {
        int pivot = arr[end];
        int i = start - 1;
        for(int j = start; j < end; j++) {
            if(arr[j] < pivot) {
                i++;
                swap(i, j);
            }
        }
        if(arr[end] < arr[i+1]) {
            swap(i+1, end);
        }
        return i + 1;
    }

    private void swap(int ein, int zwei) {
        int placeholder = arr[ein];
        arr[ein] = arr[zwei];
        arr[zwei] = placeholder;
    }

}
