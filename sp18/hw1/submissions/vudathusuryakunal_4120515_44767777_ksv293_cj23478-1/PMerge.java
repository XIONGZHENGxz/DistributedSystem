


import java.util.*;
import java.util.concurrent.*;

public class PMerge {
	private static ExecutorService es;
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		es = Executors.newFixedThreadPool(numThreads);
		int aThreads = numThreads/2;
		int bThreads = numThreads - numThreads/2;
		int chunkSizeA = A.length/aThreads + 1;
		int chunkSizeB = B.length/bThreads + 1;
		for (int t = 0; t < aThreads; t++) {
			int start = t * chunkSizeA;
			int end = Math.min(start + chunkSizeA, A.length);
			Task t1 = new Task(B, A, C, start, end);
 			es.execute(t1);
		   	         
		}
		for (int t = 0; t < bThreads; t++) {
		    int start = t * chunkSizeB;
		    int end = Math.min(start + chunkSizeB, B.length);
		            Task t2 = new Task(A, B, C, start, end);
		 			es.execute(t2);
		         
		}
		if (!es.isTerminated()) {
		}
		es.shutdownNow();
		}	
}