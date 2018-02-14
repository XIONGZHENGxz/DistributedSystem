//UT-EID= ks43639


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
    
		Set<Integer> duplicate = new HashSet<Integer>();
				
		int indexA = 0;
		int indexB = 0;
		
		while(indexA < A.length && indexB < B.length) {
			if(A[indexA] == B[indexB]) {
				duplicate.add(A[indexA]);
				indexA++;
				indexB++;
				
			} else {
				if(A[indexA] < B[indexB]) {
					indexA++;
				} else {
					indexB++;
				}
			}
		}
		
		ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);
		
		forkJoinPool.invoke(new MergeRecursiveAction(A, B, C, A.length, 0, duplicate, 1));
		forkJoinPool.invoke(new MergeRecursiveAction(B, A, C, B.length, 0, duplicate, 2));
		
		
	}
}