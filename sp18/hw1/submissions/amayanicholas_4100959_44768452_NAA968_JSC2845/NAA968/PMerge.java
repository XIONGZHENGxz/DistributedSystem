//UT-EID=naa968

import java.util.concurrent.*;

import static java.lang.Integer.max;

public class PMerge {

    private static ForkJoinPool pool;
    private static int MAX_THREADS;

    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        pool = new ForkJoinPool(numThreads);
        MAX_THREADS = numThreads;
        pool.invoke(new ParallelMerge(A, B, C, 0, 0, 0, A.length-1, B.length-1, C.length-1));
    }

    private static void merge(int[] A, int[] B, int[] C, int lowA, int lowB, int lowC, int highA, int highB, int highC) {

        int sizeA = highA - lowA + 1;
        int sizeB = highB - lowB + 1;

        if (sizeA < sizeB)
        {
            int[] tmpArray = A;
            A = B;
            B = tmpArray;

            int tmpInt = sizeA;
            sizeA = sizeB;
            sizeB = tmpInt;

            tmpInt = lowA;
            lowA = lowB;
            lowB = tmpInt;

            tmpInt = highA;
            highA = highB;
            highB = tmpInt;
        }

        if (sizeA <= 0) return;

        int midA = (highA + lowA) / 2;
        int midAPosInB = binarySearch(A[midA], B, lowB, highB);
        int midAPosInC = lowC + (midA - lowA) + (midAPosInB - lowB);
        synchronized (C) {
            C[midAPosInC] = A[midA];
        }

        RecursiveAction r1 = new ParallelMerge(A, B, C, lowA, lowB, lowC, midA-1, midAPosInB-1, midAPosInC);
        RecursiveAction r2 = new ParallelMerge(A, B, C, midA+1, midAPosInB, midAPosInC+1, highA, highB, highC);

        if (pool.getActiveThreadCount() >= MAX_THREADS) {
            merge(A, B, C, lowA, lowB, lowC, midA-1, midAPosInB-1, midAPosInC);
        } else {
            pool.invoke(r1);
        }

        if (pool.getActiveThreadCount() >= MAX_THREADS) {
            merge(A, B, C, midA+1, midAPosInB, midAPosInC+1, highA, highB, highC);
        } else {
            pool.invoke(r2);
        }
    }

    private static int binarySearch(int value, int[] a, int left, int right) {
        int low  = left;
        int high = max(left, right+1);
        int mid;
        while(low < high)
        {
            mid = (low + high) / 2;
            if (value <= a[mid]) high = mid;
            else                 low  = mid + 1;
        }
        return high;
    }

    private static class ParallelMerge extends RecursiveAction {

        static final int THRESHOLD = 16;

        private int[] A, B, C;
        private int lowA, lowB, lowC;
        private int highA, highB, highC;

        ParallelMerge(int[] A, int[] B, int[] C, int lowA, int lowB, int lowC, int highA, int highB, int highC) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.lowA = lowA;
            this.lowB = lowB;
            this.lowC = lowC;
            this.highA = highA;
            this.highB = highB;
            this.highC = highC;
        }

        @Override
        protected void compute() {
            merge(A, B, C, lowA, lowB, lowC, highA, highB, highC);
        }
    }
}
