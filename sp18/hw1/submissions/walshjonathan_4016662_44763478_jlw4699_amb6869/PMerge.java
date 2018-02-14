import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.*;

public class PMerge {

    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        Future[] fs = new Future[A.length];
        for (int i = 0; i < A.length; i++) {
            PMergeArray arr = new PMergeArray(A, B, C, i);
            Future<Boolean> f = threadPool.submit(arr);
            fs[i] = f;
        }
        try {
            for (Future f: fs) 
                f.get();
        } catch(Exception e){System . err . println  (e ); }
        
        threadPool.shutdown();
    }
}

class PMergeArray implements Callable<Boolean> {
    int[] A;
    int[] B;
    int[] C;
    int aIndex;

    public PMergeArray(int[] A, int[] B, int[] C, int aIndex) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.aIndex = aIndex;
    }
    
    public Boolean call() {
        int bIndex = binarySearch(0, B.length - 1, A[aIndex]);
        if (bIndex == -1)
            bIndex = B.length;

        int cIndex = aIndex + bIndex;

        if (aIndex == 0) {
            for (int i = 0; i < bIndex; i++) 
                C[i] = B[i];
        }

        C[cIndex] = A[aIndex];
        cIndex++;

        if (aIndex == A.length - 1) {
            while (bIndex < B.length) {
                C[cIndex] = B[bIndex];
                cIndex++;
                bIndex++;
            }
        }
        else {
            while (bIndex < B.length && B[bIndex] < A[aIndex + 1]) {
                C[cIndex] = B[bIndex];
                bIndex++;
                cIndex++;
            }
        }
        return true;
    }

    // Returns index of search if it is present in B[start...end]
    // else return -1
    int binarySearch(int start, int end, int search) {
        //if no element is smaller than start but still larger than search
        if (end==start){
            if(B[end]>search)
                return end;
            else //if no element in array is larger than search
                return -1;
        }
        if (end>=start) {
            int mid = start + (end - start)/2;
            //left subarray
            if (B[mid] > search)
                return binarySearch(start, mid, search);

            //right subarray
            return binarySearch(mid+1, end, search);
        }
        return -1;
    }
}