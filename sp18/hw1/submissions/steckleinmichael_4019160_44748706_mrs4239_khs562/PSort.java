//UT-EID=mrs4239
//UT-EID=khs562

import java.util.*;
import java.util.concurrent.*;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
	  int numProcessors = Runtime.getRuntime().availableProcessors();
	  RecursiveQuicksort sortAction = new RecursiveQuicksort(A, begin, end);
	  ForkJoinPool pool = new ForkJoinPool(numProcessors);
	  pool.invoke(sortAction);
	  pool.shutdown();
	  try {
		pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	  } catch (InterruptedException e) { System.exit(1); }
  }
}

class RecursiveQuicksort extends RecursiveAction {
	
	private int[] A;
	private int begin;
	private int end;

	public RecursiveQuicksort(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end-1; // is not inclusive
	}

	@Override
	protected void compute() {
		if (begin >= end) return;
		
		/* Quicksort Algorithm */
		int lowerWall = begin;
		int upperWall = end;
		int midpoint = (end + begin) / 2;
		int pivot = A[midpoint];

		// Sort/swap values on either side of pivot
		while (lowerWall <= upperWall) {
			// Find upper bound for lower wall
			while (A[lowerWall] < pivot)
				lowerWall++;

			// Find lower bound for upper wall
			while (A[upperWall] > pivot)
				upperWall--;
			
			// Swap out of place values
			if (lowerWall <= upperWall) {
				int temp = A[lowerWall];
				A[lowerWall] = A[upperWall];
				A[upperWall] = temp;
				lowerWall++;
				upperWall--;
			}
		}
		
		// Parallelize recursive part
		RecursiveQuicksort sortLeft = new RecursiveQuicksort(A, begin, upperWall+1);
		sortLeft.fork();
		RecursiveQuicksort sortRight = new RecursiveQuicksort(A, lowerWall, end+1);
		sortRight.compute();
	}
	
}
