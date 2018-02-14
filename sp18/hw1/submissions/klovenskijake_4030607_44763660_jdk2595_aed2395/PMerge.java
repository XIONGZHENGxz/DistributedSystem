//UT-EID=jdk2595, aed2395


import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newFixedThreadPool;


public class PMerge implements Runnable {
    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
        ExecutorService pool = newFixedThreadPool(numThreads);

        //Basically give every index in each array it's own thread
        for(int i = 0; i < A.length; i++) {
            pool.submit(new PMerge(A,B,C,0,i));
        }
        for(int i = 0; i < B.length; i++) {
            pool.submit(new PMerge(A,B,C,1,i));
        }
        pool.shutdown();
        while(!pool.isTerminated()){}   //If this isn't here the system is actually too fast

    }

    private int[] A;
    private int[] B;
    private int[] C;
    private int choice;
    private int index;

    private PMerge(int[] A, int[] B, int[] C, int choice, int index) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.choice = choice;
        this.index = index;
    }

    @Override
    public void run() {
        // We are guaranteed A and B will not have duplicates within themselves,
        // However they might have shared elements.
        // In that case the program arbitrarilly assigns A's duplicate to be second after B's
        if(choice == 0) { //Array A
            int place = Arrays.binarySearch(B,A[index]);
            if(place < 0) {
                place += 1;
                place *= -1;
            }
            else {
                place++;
            }
            place += index;
            C[place] = A[index];
        }
        else if(choice == 1) {  //Array B
            int place = Arrays.binarySearch(A,B[index]);
            if(place < 0) {
                place += 1;
                place *= -1;
            }
            place += index;
            C[place] = B[index];
        }
    }
}
