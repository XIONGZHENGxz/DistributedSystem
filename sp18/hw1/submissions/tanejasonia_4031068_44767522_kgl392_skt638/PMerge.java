//UT-EID= kgl392, skt638


import java.util.*;
import java.util.Arrays;
import java.util.concurrent.*;

// method utilizes merge sort algorithm where each thread takes a split of the merge sort
public class PMerge {
    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        merge_arrays(A, B, C);
        mergeSort(C, numThreads);
    }

    // main merge sort method that utilizes multiple threads
    // number of threads is split each time merge sort splits array until there are no more threads to be split
    // if there are no more threads to be split a simple merge sort is ran on the one thread until a sorted array is returned
    public static void mergeSort(int[] a, int numThreads) {
        if (numThreads <= 1) {
            mergeSort(a);
        } else if (a.length >= 2) {
            int[] left = Arrays.copyOfRange(a, 0, a.length / 2);
            int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);
            Thread t1 = new Thread(new Sort(left, numThreads / 2));
            Thread t2 = new Thread(new Sort(right, numThreads / 2));
            t1.start();
            t2.start();

            try {
                t1.join();
                t2.join();
            } catch (Exception e) {

            }

            merge(left, right, a);
        }
    }

    // merge sort merge method that will merge the two arrays left and right into array a
    public static void merge(int[] left, int[] right, int[] a) {
        int l = 0;
        int r = 0;
        for (int i = 0; i < a.length; i++) {
            if (r >= right.length || (l < left.length && left[l] < right[r])) {
                a[i] = left[l];
                l++;
            } else {
                a[i] = right[r];
                r++;
            }
        }

    }

    // standard merge sort with one thread
    public static void mergeSort(int[] a) {
        if (a.length >= 2) {
            int[] left = Arrays.copyOfRange(a, 0, a.length / 2);
            int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);

            // sort the halves
            mergeSort(left);
            mergeSort(right);

            // merge them back together
            merge(left, right, a);
        }
    }

    // merges A and B into array C by concatenating array A+B
    public static void merge_arrays(int[] a, int[] b, int[] c) {
        int length = a.length + b.length;
        for (int i = 0; i < length; i++) {
            if (i < a.length) {
                c[i] = a[i];
            } else {
                c[i] = b[i - a.length];
            }
        }
    }
}