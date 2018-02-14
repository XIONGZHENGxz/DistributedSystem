//UT-EID= arg3532


import java.util.*;
import java.util.concurrent.*;


public class PMerge extends RecursiveTask<Void>{
    int[] A;
    int[] B;
    int[] C;
    boolean isInA;
    int indexToMove;

    public PMerge(int[] A, int[] B, int[] C, boolean isInA, int index){
        this.A = A;
        this.B = B;
        this.C = C;
        this.isInA = isInA;
        this.indexToMove = index;
    }

    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        for(int i = 0; i < A.length; i++){
            PMerge p = new PMerge(A, B, C, true, i);
            pool.execute(p);
        }
        for(int i = 0; i < B.length; i++){
            PMerge p = new PMerge(A, B, C, false, i);
            pool.execute(p);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void compute() {
        int indexInC = indexToMove;
        if(isInA){
            for(int i = 0; i < B.length; i++){
                if(A[indexToMove] < B[i]){
                    indexInC += i;
                    C[indexInC] = A[indexToMove];
                    return null;
                }
            }
            indexInC += B.length;
            C[indexInC] = A[indexToMove];
        }
        else {
            for(int i = 0; i < A.length; i++){
                if(B[indexToMove] < A[i]){
                    indexInC += i;
                    C[indexInC] = B[indexToMove];
                    return null;
                }
            }
            indexInC += A.length;
            C[indexInC] = B[indexToMove];
        }
        return null;
    }
}