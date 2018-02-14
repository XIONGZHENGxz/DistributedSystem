/*
 *  PSort.java
 *  EE 360P Homework 1
 *
 *  Created by Zain Modi and Ali Ziyaan Momin on 01/30/2018.
 *  EIDs: ZAM374 and AZM259
 *
 */

import java.util.concurrent.*;

public class PSort extends RecursiveTask<Void> {
    int[] A;
    int begin;
    int end;
    PSort(int[] A, int begin, int end){
        this.A = A;
        this.begin = begin;
        this.end = end;
    }
    //qsort

    int part(int A[], int begin, int end){
        int temp;
        int indx = begin - 1;
        int high_elem = A[end];
        for(int i = begin; i <= end-1; i++){
            if(A[i] <= high_elem){
                indx+=1;
                temp = A[indx];
                A[indx] = A[i];
                A[i] = temp;
            }
        }

        temp = A[indx+1];
        A[indx+1] = A[end];
        A[end] = temp;
        return indx+1;


    }

    void InsertSort(int []A, int begin, int end){
        int key;
        for(int i = begin+1; i < end; i++){

            for(int j = i; j > 0; j--){
                if(A[j]< A[j-1]){
                    key = A[j];     //switch aj aj-1
                    A[j] = A[j-1];
                    A[j-1] = key;
                }
            }
        }

    }

    protected Void compute(){
        if(end > begin){
            if(end+1 - begin <= 16){
               InsertSort(A, begin, end+1);
            }
            else {
                int part = part(A, begin, end);
                //System.out.println("REACHING PARALLEL SORT > 16 elem");
                PSort p1 = new PSort(A, begin, part - 1);
                p1.fork();
                p1.join();
                PSort p2 = new PSort(A, part + 1, end);
                p2.compute();

                //System.out.println("In some thread " + Thread.currentThread());
            }
        }

        return null;
    }

    public static void parallelSort(int[] A, int begin, int end) {
        int p = Runtime.getRuntime().availableProcessors();
        //System.out.println("Trying on following number of processors: " + p);
        PSort p1 = new PSort(A, begin, end-1);
        ForkJoinPool pool = new ForkJoinPool(p);
        pool.invoke(p1);
    }
}


