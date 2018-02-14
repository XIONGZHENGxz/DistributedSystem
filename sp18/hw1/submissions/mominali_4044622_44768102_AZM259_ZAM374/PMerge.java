/*
 *  PMerge.java
 *  EE 360P Homework 1
 *
 *  Created by Ali Ziyaan Momin and Zain Modi on 01/30/2018.
 *  EIDs: AZM259 and ZAM374
 *
 */

import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Runnable {

    private int[] A;
    private int[] B;
    private static int[] C;
    private boolean bool;
    private int idx;
    private int range;


    /**
     * Private CONSTRUCTOR for PMerge.
     * @param A: int[]
     * @param B: int[]
     * @param C: int[]
     * @param bool: boolean
     * @param idx: int
     * @param range: int
     **/
    private PMerge(int[] A, int[] B, int[] C, boolean bool, int idx, int range) {
        this.A = (int[]) A.clone();
        this.B = (int[]) B.clone();
        this.C = C;
        this.bool = bool;
        this.idx = idx;
        this.range = range;
    }


    /**
     * This private methods calculates the number of elements in arr smaller than x.
     * @param arr: int[]
     * @param x: int
     * @return int
     **/
    private int predecessors(int[] arr, int x) {
        int i;
        for(i = 0; i < arr.length; i ++){
            if(arr[i] > x){
                break;
            }
            if(arr[i] == x){
                if(this.bool){
                    i ++;
                    break;
                }
                else{
                    break;
                }
            }
        }
        return i;
    }


    /**
     * This is the method that will run when a thread is created using this class.
     * This public method finds the right location to place the indexes that were
     * assigned to it in array C.
     * @return void
     **/
    public void run() {
        while(this.range > 0){
            this.C[this.idx + predecessors(this.bool ? this.A : this.B,
                            this.bool ? this.B[this.idx] : this.A[this.idx])]
                            = this.bool ? this.B[this.idx] : this.A[this.idx];
            this.idx ++;
            this.range --;
        }
    }


    /**
     * This private method is only called if only one thread is available
     * to perform merge.
     * @param A: int[]
     * @param B: int[]
     * @param C: int[]
     * @return void
     **/
    private static void singleThreadMerge(int[] A, int[] B, int[] C) {
        int h = 0, i = 0, j = 0;
        while(i < A.length || j < B.length) {
            if(i == A.length) {
                C[h ++] = B[j ++];
            } else if(j == B.length) {
                C[h ++] = A[i ++];
            } else {
                if(A[i] < B[j]) C[h ++] = A[i ++];
                else C[h ++] = B[j ++];
            }
        }
    }


    /**
     * This private methods calculates the range of indexes a thread will
     * execute upon.
     * @param a: boolean[]
     * @param currIndex: int
     * @return int
     **/
    private static int getRange(boolean[] a, int currIndex){
        int count = 1;
        currIndex ++;
        while(currIndex < a.length){
            if(a[currIndex] == true){
                if(a[currIndex - 1] == false){
                    return count;
                }
                else{
                    return 1;
                }
            }
            currIndex ++;
            count ++;
        }
        return count;
    }


    /**
     * This public method sets ups threads to perform a parallel merge of two
     * sorted arrays (A and B) into one array (C).
     * @param A
     * @param B
     * @param C
     * @param numThreads
     * @return void
     */
    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {

        if(numThreads < 1){
            return;
        }

        if(numThreads == 1){
            singleThreadMerge(A, B, C);
            return;
        }

        if(numThreads > C.length){
            numThreads = C.length;
        }

        boolean atemp[] = new boolean[A.length];
        int atempidx = 0;
        boolean adone = false;
        int aincrementer = (int) Math.round((float)C.length / (float)B.length);

        boolean btemp[] = new boolean[B.length];
        int btempidx = 0;
        boolean bdone = false;
        int bincrementer = (int) Math.round((float)C.length / (float)A.length);

        int threadCount = numThreads;

        boolean alternator = A.length > B.length;


        /* The following for-loop optimally assigns indexes and ranges for threads
           given the number of threads. */
        for (int i = 0; i < threadCount;){
            if(alternator && !adone){
                //work on A
                if(!(atempidx < atemp.length)){
                    atempidx = atempidx % atemp.length;
                    while(atemp[atempidx]) {
                        if(atempidx >= atemp.length){
                            adone = true;
                        }
                        atempidx++;
                        atempidx = atempidx % atemp.length;
                    }
                    continue;
                }
                else{
                    atemp[atempidx] = true;
                    atempidx += aincrementer + 1;
                    i ++;
                }
            }
            else if(!alternator && !bdone){
                //work on B
                if(!(btempidx < btemp.length)){
                    btempidx = btempidx % btemp.length;
                    while(btemp[btempidx]) {
                        if(btempidx >= btemp.length){
                            bdone = true;
                        }
                        btempidx++;
                        btempidx = btempidx % btemp.length;
                    }
                    continue;
                }
                else{
                    btemp[btempidx] = true;
                    btempidx += bincrementer + 1;
                    i ++;
                }
            }

            alternator = !alternator;
        }


        /* The following while loop starts all threads that will work on array A. */
        if(atemp[0]) {
            int i = 0;
            while (i < atemp.length) {
                int range = getRange(atemp, i);
                Thread ta = new Thread(new PMerge(A, B, C, false, i, range));
                ta.start();
                try {
                    ta.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i = i + range;
            }
        }

        /* The following while loop starts all threads that will work on array B. */
        if(btemp[0]) {
            int j = 0;
            while (j < btemp.length) {
                int range = getRange(btemp, j);
                Thread tb = new Thread(new PMerge(A, B, C, true, j, range));
                tb.start();
                try {
                    tb.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                j = j + range;
            }
        }

    }
}
