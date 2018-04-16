package hw1;

import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Callable<int[]> {
    private final int[] A;
    private final int[] B;
    private int[] C;
    private int start;
    private int end;
    private boolean is_array_a;
    private static HashSet<Integer> elements = new HashSet<>();

    public PMerge(int[] A, int[] B, int[] C, int start, int end, boolean is_array_a) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.start = start;
        this.end = end;
        this.is_array_a = is_array_a;
    }

    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) {
        // arrays A and B are sorted
        // array C is the merged array
        try {
            ExecutorService es = Executors.newFixedThreadPool(numThreads);

            // split threads between A and B
            // determine number of tasks based on number of elements in each array
            // int num_tasks_a = A.length / (numThreads / 2);
            // int num_tasks_b = B.length / (numThreads / 2);

            int num_tasks = (int) Math.ceil((double) C.length / (double) numThreads);

            // for every group of elements, make a task
            // grouping elements from start to end ensures less overhead
            // and non-trivial tasks
            for (int i = 0; i < A.length; i += num_tasks) {
                if (i + num_tasks <= A.length) { // handle extra elements / un-even split
                    PMerge pm = new PMerge(A, B, C, i, i + (num_tasks - 1), true);
                    Future<int[]> pm1 = es.submit(pm);
                    pm1.get();
                } else {
                    PMerge pm = new PMerge(A, B, C, i, A.length, true);
                    Future<int[]> pm1 = es.submit(pm);
                    pm1.get();
                }
            }


            for (int i = 0; i < B.length; i += num_tasks) {
                if (i + num_tasks <= B.length) { // handle extra elements / un-even split
                    PMerge pm = new PMerge(A, B, C, i, i + (num_tasks - 1), false);
                    Future<int[]> pm1 = es.submit(pm);
                    pm1.get();
                } else {
                    PMerge pm = new PMerge(A, B, C, i, B.length, false);
                    Future<int[]> pm1 = es.submit(pm);
                    pm1.get();
                }

            }
            elements.clear();
            es.shutdown ();
        } catch (Exception e) { System.err.println (e); }
    }


    @Override
    public int[] call() throws Exception {
        // place every element from start to end in the final array
        if (is_array_a) {
            for (int i = start; i <= end; i++) {
                int index_found = binarySearch(A[i], B);
                C[i + index_found] = A[i];
            }
        } else {
            for (int i = start; i <= end; i++) {
                int index_found = binarySearch(B[i], A);
                C[i + index_found] = B[i];
            }
        }

        return C;
    }

    /* returns the index at which the number should be placed */
    private int binarySearch(int number, int[] arr) {
        int start = 0;
        int end = arr.length - 1;

        while (start <= end) {
            int index = start + ((end - start) / 2);
            if (number == arr[index]) {
                if (elements.contains(number)) { // check for duplicates
                    elements.add(number);
                    return index + 1;
                }
                elements.add(number);
                return index;
            } else if (number < arr[index]) {
                end = index - 1;
            } else {
                start = index + 1;
            }
        }
        elements.add(number);
        return start;
    }
}