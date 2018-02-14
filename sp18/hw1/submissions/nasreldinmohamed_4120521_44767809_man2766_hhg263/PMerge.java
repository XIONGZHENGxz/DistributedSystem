//UT-EID= man2766, hhg263


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) {
        ExecutorService exec = Executors.newFixedThreadPool(numThreads);

        for(int i=0; i<A.length; i++){
            exec.submit(new Merge(A, B, C, true, i));
        }

        for(int i=0; i<B.length; i++){
            exec.submit(new Merge(A, B, C, false, i));
        }

        exec.shutdown();
        while(!exec.isTerminated()){
            try{
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
}
