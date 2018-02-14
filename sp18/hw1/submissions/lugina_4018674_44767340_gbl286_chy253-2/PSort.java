//UT-EID= chy253, gbl286


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{

    private int[] arr;
    private int begin;
    private int end;
    private int threshold;

    public PSort(int[] A, int begin, int end){
        arr = A;
        this.begin = begin;
        this.end = end;
        threshold = 16;
    }
    public static void parallelSort(int[] A, int begin, int end){
        // TODO: Implement your parallel sort function
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Number of processors: " + processors);
        PSort p = new PSort(A, begin, end);
        ForkJoinPool pool = new ForkJoinPool(processors);
        pool.invoke(p);
    }
    protected void compute() {
        if (end-begin <= threshold) {
            insertSort(begin,end);
            return;
        }
        int index = partition(arr, begin, end);
        PSort p1 = new PSort(arr, begin, index);
//        System.out.println("p1: " + begin + "-" + index + ": " + Arrays.toString(arr));
        p1.fork();
        PSort p2 = new PSort(arr, index, end);
//        System.out.println("p2: " + index + "-" + end+ ": " + Arrays.toString(arr));
        p2.compute();
        p1.join();

    }
    protected int partition(int[] arr, int begin, int end) {
        int i = begin, j = end-1;
        int tmp;
        int pivot = arr[(begin + end) / 2];
//        System.out.println("pivot: " + pivot);
        while (i <= j) {
            while (arr[i] < pivot)
                i++;
            while (arr[j] > pivot)
                j--;
            if (i <= j) { //remove equal?
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }
        return i;
    }
    protected void insertSort(int begin, int end){
//        System.out.println("IS: " + begin + "-" + end + ": " + Arrays.toString(arr));
        for(int i = begin+1; i<end; i++){
            int check = arr[i];
            int j = i-1;

            while(j>=0 && arr[j] > check){
                arr[j+1]=arr[j];
                j=j-1;
            }
            arr[j+1] = check;
        }
//        System.out.println("IS end: "+ Arrays.toString(arr));
    }
}
