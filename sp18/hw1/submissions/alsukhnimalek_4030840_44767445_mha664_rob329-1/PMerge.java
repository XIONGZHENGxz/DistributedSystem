//UT-EID = mha664, rob329
// NOTE = The reason this code is so long is because we did the lab with the assumption that
// you can only use that number of threads (numThreads) in total, not at one time.
//

import java.util.*;
import java.util.concurrent.*;


public class PMerge{

    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){

        ArrayList<Future<Object>> futures = new ArrayList<>();
        int[] filledIndexes = new int[C.length];

        // create a thread to find positions of all elements in A
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        if(numThreads == 1){

            futures.add(pool.submit(new Merge(A, B, C, filledIndexes, 0, A.length, true)));

            try {
                for(int i = 0; i < futures.size(); i++){
                    futures.get(i).get();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // now that all of the positions have been filled by A, fill the remaining with B
            int i = 0;
            for(int j = 0; j < C.length && i < B.length; j++){
                if(filledIndexes[j] == 0){
                    C[j] = B[i];
                    i++;
                }
            }
        }
        else {

            int numberPerThreadA = A.length / ((numThreads / 2)  + (numThreads % 2));
            int numberPerThreadB = B.length / (numThreads / 2);
            int overflowA = A.length % ((numThreads / 2)  + (numThreads % 2));
            int overflowB = B.length % (numThreads / 2);

            if(!(numberPerThreadA == 0 && numberPerThreadB == 0)) {
                int i;
                for (i = 0; i < A.length - (numberPerThreadA + overflowA); i += numberPerThreadA) {
                    futures.add(pool.submit(new Merge(A, B, C, filledIndexes, i, i + numberPerThreadA, true)));
                }

                int i1;
                for (i1 = 0; i1 < B.length - (numberPerThreadB + overflowB); i1 += numberPerThreadB) {
                    futures.add(pool.submit(new Merge(B, A, C, filledIndexes, i1, i1 + numberPerThreadB, false)));
                }

                // final thread should take remaining elements
                futures.add(pool.submit(new Merge(A, B, C, filledIndexes, i, i + numberPerThreadA + overflowA, true)));
                futures.add(pool.submit(new Merge(B, A, C, filledIndexes, i1, i1 + numberPerThreadB + overflowB, false)));
            } else {
                for(int i = 0; i < A.length; i++){
                    futures.add(pool.submit(new Merge(A, B, C, filledIndexes, i, i + 1, true)));
                }

                for(int i = 0; i < B.length; i++){
                    futures.add(pool.submit(new Merge(B, A, C, filledIndexes, i, i + 1, false)));
                }
            }

            try {
                for(int i = 0; i < futures.size(); i++){
                    futures.get(i).get();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
    }

    protected static class Merge implements Callable<Object> {

        int[] A;
        int[] B;
        int[] C;
        int[] filledIndexes;
        int startIndex;
        int until;
        boolean isFirst;

        public Merge(int[] A, int[] B, int[] C, int[] filledIndexes, int startIndex, int until, boolean isFirst){

            this.A = A;
            this.B = B;
            this.C = C;
            this.filledIndexes = filledIndexes;
            this.startIndex = startIndex;
            this.until = until;
            this.isFirst = isFirst;
        }

        @Override
        public Object call() {

            for(int i = startIndex; i < until; i++){
                int index = findPosition(i);
                C[index] = A[i];
                filledIndexes[index] = 1;
            }

            return 0;
        }

        private int findPosition(int i){
            if(isFirst) {
                if (A[i] < B[0]) {
                    return i;
                }
            } else {
                if (A[i] <= B[0]) {
                    return i;
                }
            }

            for(int j = 0; j < B.length; j++){
                if(isFirst) {
                    if (A[i] < B[j]) {
                        return i + j;
                    }
                } else {
                    if (A[i] < B[j]) {
                        return i + j;
                    } else if (A[i] == B[j]) {
                        return i + j;
                    }
                }
            }
            return B.length + i;
        }
    }
}