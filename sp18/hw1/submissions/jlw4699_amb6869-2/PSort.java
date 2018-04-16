import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import javax.lang.model.util.ElementScanner6;

public class PSort {
    public static void parallelSort(int[] A, int begin, int end) {
        int p = 2;//Runtime.getRuntime().availableProcessors();
        PSortArray arr = new PSortArray(A, begin, end);
        ForkJoinPool pool = new ForkJoinPool(p);
        pool.invoke(arr);
    }
}

class PSortArray extends RecursiveTask<Integer>{
    int[] A;
    int begin;
    int end;

    public PSortArray(int[] A, int begin, int end) {
        this.A = A;
        this.begin = begin;
        this.end = end - 1;
        //for (int i = this.begin; i <= this.end; i++)
         //   System.out.print(A[i] + " ");
        //System.out.println();
    }

    protected Integer compute() {
        if (end - begin < 16)
            return sequentialSort();
        else
            return parallelSort();

        
    }

    private Integer sequentialSort() {
        for (int i = begin; i <= end - 1; i++) {
            int min = i;
            for (int j = i + 1; j <= end; j++) {
                if (A[j] < A[min])
                    min = j;
            }
            int temp = A[min];
            A[min] = A[i];
            A[i] = temp;
        }
        return 1;
    }

    private Integer parallelSort() {
        int pivotLoc = end;
        int i = begin;
        while (i < pivotLoc) {
            if (A[i] > A[pivotLoc]) {
                int temp = A[i];
                A[i] = A[pivotLoc - 1];
                A[pivotLoc - 1] = A[pivotLoc];
                A[pivotLoc] = temp;
                pivotLoc -= 1;
            }
            else 
                i += 1;
        }

        PSortArray arrLeft = new PSortArray(A, begin, pivotLoc);
        PSortArray arrRight = new PSortArray(A, pivotLoc + 1, end + 1);
        arrLeft.fork();
        arrRight.fork();
        arrLeft.join();
        arrRight.join();
        return 1;
    }
}