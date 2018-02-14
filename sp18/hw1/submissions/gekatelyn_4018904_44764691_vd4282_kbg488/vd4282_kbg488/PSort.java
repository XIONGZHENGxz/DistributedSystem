//UT-EID=vd4282, kbg488

import java.util.*;
import java.util.concurrent.*;

public class PSort{

    public static void parallelSort(int[] A, int begin, int end){
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        Sort s1 = new Sort(A, begin, end);
        pool.invoke(s1);
    }
}

class Sort extends RecursiveAction {

    int[] A;
    int begin, end;

    public Sort(int[] a, int begin, int end){
        this.A = a;
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected void compute() {
        if (begin == end) {  //empty array
            return;
        } else if (end - begin < 16) {	//array of size less than 16
            insertionSort(A);
        } else {
            int pivot = quickSort(A, begin, end);

            Sort s2 = new Sort(A, begin, pivot);
            s2.fork();
            Sort s3 = new Sort(A, pivot + 1, end);

            s3.compute();
            s2.join();
        }
    }

    // insertion sort
    public void insertionSort(int[] a) {
        for (int i = begin; i < end; i++) {
            int key = a[i];
            int j = i-1;

            while (j >= 0 && a[j] > key) {
                a[j+1] = a[j];
                j = j - 1;
            }
            a[j+1] = key;
        }
    }

    public int quickSort(int[] a, int p, int r) {
        int i = p - 1;
        int x = a[r-1];
        for (int j = p; j < r-1; j++) {
            if (a[j] < x) {
                i++;
                swap(a, i, j);
            }
        }
        i++;
        swap(a, i, r-1);
        return i;
    }

    public void swap(int[] swap, int a, int b) {
        int temp = swap[a];
        swap[a] = swap[b];
        swap[b] = temp;
    }
}