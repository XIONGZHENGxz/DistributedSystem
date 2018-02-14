//UT-EID=br24964, ajl3287

import java.util.*;

public class PMerge {

    public static int parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        ArrayList<Merger> mergers = new ArrayList<>();

        int totalLength = A.length + B.length;
        int step = numThreads > totalLength ? totalLength : numThreads;

        for (int i = 0; i < totalLength; i += step) {
            Merger merger = new Merger(A, B, C, i, i + step);
            merger.start();
            mergers.add(merger);
        }

        for (Merger merger : mergers) {
            try {
                merger.join();
            } catch (InterruptedException e) {
                System.err.println("PMerge.parallelMerge(): " + e.toString());
            }
        }

        return 0;
    }

    private static class Merger extends Thread {
        private int A[];
        private int B[];
        private int C[];
        private int start;
        private int end;

        Merger(int A[], int B[], int C[], int start, int end) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.start = start;
            this.end = end;
        }

        private int search(int arr[], int num) {
            int beg = 0;
            int end = arr.length - 1;
            int mid = (beg + end) / 2;

            while (beg < end) {
                if (arr[mid] == num)
                    break;
                if (num < arr[mid])
                    end = mid - 1;
                else
                    beg = mid + 1;
                mid = (beg + end) / 2;
            }

            return mid;
        }

        @Override
        public void run() {
            int toPlace[];
            int toFill[];

            for (int i = start; i < end; i++) {
                int idx = i;
                if (i < A.length) {
                    toPlace = A;
                    toFill = B;
                } else {
                    toPlace = B;
                    toFill = A;
                    idx -= A.length;
                }

                while (idx >= toPlace.length)
                    idx--;

                int num = toPlace[idx];
                int rank = search(toFill, num);

                // Account for duplicate values
                if (toFill[rank] == num && toFill == B)
                    idx++;
                else if (num > toFill[rank])
                    rank++;

                C[idx + rank] = num;
            }
        }
    }
}
