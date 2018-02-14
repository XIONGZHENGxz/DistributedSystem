//UT-EID=kbg488
//UT-EID=vd4282

import java.util.*;
import java.util.concurrent.*;

public class PMerge{
    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){

        int numbers = (int) Math.ceil((double)C.length / (double)numThreads);
        int begin = 0;

        for (int i = 0; i < numThreads; i++) {
            Merge m = new Merge(A, B, C, begin, numbers);
            Thread t = new Thread(m);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            begin += numbers;
        }
    }
}

class Merge implements Runnable {

    int[] A;
    int[] B;
    int[] C;
    int begin;
    int numbers;

    public Merge(int[] A, int[] B, int[] C, int begin, int numbers) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.begin = begin;
        this.numbers = numbers;
    }

    public void run() {
        for (int i = 0; i < numbers; i++) {
            if (begin < A.length) {
                //Array A
                int index = begin;
                int num = A[begin];  //number

                index += binarySearch(B, num, true);
                C[index] = num;

            } else if (begin < C.length){
                //Array B
                int index = begin - A.length;
                int num = B[index];

                index += binarySearch(A, num, false);
                C[index] = num;
            }
            begin += 1;
        }
    }

    public int binarySearch(int[] D, int value, boolean checkRepeat) {
        int low = 0, high = D.length;
        int mid = low + (high - low) / 2;
        while (low < high) {
            mid = low + (high - low) / 2;
            if (D[mid] == value) {
                if (checkRepeat) {
                    return mid + 1;
                }
                return mid;
            } else if (D[mid] > value) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        while(D[mid] > value) {
            mid -= 1;
            if (mid < 0)
                break;
        }

        return mid + 1;
    }
}