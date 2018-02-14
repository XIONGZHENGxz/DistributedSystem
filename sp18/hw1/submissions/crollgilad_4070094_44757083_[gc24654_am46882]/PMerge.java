//UT-EID= GC24654
//Names= Gilad Croll
//Andoni Mendoza
//UTIDs:
//gc24654
//AM46882
import java.util.*;
import java.util.concurrent.*;
import java.util.Arrays;

public class PMerge{
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		//arrays A and B are sorted
		//array C is the merged array
		Q1Thread.ATemp = A;
		Q1Thread.BTemp = B;
		Q1Thread.CTemp = C;
		int chunkSize = (A.length+B.length)/numThreads;
		int chunkRemainder = (A.length+B.length)%numThreads;
		int cur = 0;
		if (numThreads> A.length+B.length)
			numThreads = A.length+B.length;
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		for (int i=0; i<numThreads; i++){
			int last = cur+chunkSize;
			if (chunkRemainder>0){
				last++;
				chunkRemainder--;
			}
			
			Q1Thread f = new Q1Thread(cur, last);
			threadPool.submit(f);
			cur = last;
		}
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}


