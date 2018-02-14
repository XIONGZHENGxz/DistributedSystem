/*
 * Names:    Kayvon Khosrowpour, Megan Cooper
 * Class:    EE 360P
 * UT EIDs:   knk689, mlc4285
 * Homework: 1
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

// Class containing a parallel merge algorithm
public class PMerge{

	// merges the sorted arrays A, B into C
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) {
		
		if (A==null || B==null || numThreads <= 0) return; // invalid input
		
		ForkJoinPool pool = new ForkJoinPool(numThreads);
		List<SearchTask> listOfTasks = new ArrayList<>();
		
		// create and submit tasks, searching for A[i] in B
		for (int i = 0; i < A.length; i++) {
			SearchTask t = new SearchTask(A[i], false, B, i);
			listOfTasks.add(t);
			pool.submit(t);
		}
		
		// create and submit tasks, searching for B[i] in A
		for (int i = 0; i < B.length; i++) {
			SearchTask t = new SearchTask(B[i], true, A, i);
			listOfTasks.add(t);
			pool.submit(t);	
		}
		
		// get results of each task and write to C
		for (SearchTask t : listOfTasks) {
			t.join();
			try {
				Integer placementIndex = t.get();
				C[placementIndex.intValue()+t.getIndex()+1] = t.getX();
			} catch (InterruptedException e) { e.printStackTrace(); } 
			catch (ExecutionException e) { e.printStackTrace(); }
		}

	}
	
	@SuppressWarnings("serial")
	public static class SearchTask extends RecursiveTask<Integer> {

		private int x;    // what we're searching for
		private int Y[];  // array we're searching in
		private boolean checkRepeats; // if true, will subtract the return index by 1
		private int index; // index we'll add to index given by compute()
		
		public SearchTask(int x, boolean checkRepeats, int[] Y, int index) {
			this.x = x;
			this.checkRepeats = checkRepeats;		
			this.Y = Y;
			this.index = index;
		}
		
		public int getX() { return x; }
		public int getIndex() { return index; }
		
		// returns index of other array (e.g. if searching in B, returns index of B)
		@Override 
		protected Integer compute() {
			return relativeFloorBinarySearch(0, Y.length-1);
		}
		
		// lo is minIndex, hi is maxIndex (inclusive) i.e. NOT the length of Y
		// performs a relative floor binary search, where x is what is searched for
		// ex: {1, 4, 7, 9}
		//     if we search for 0, we return -1
		//     if we search for 4, we return 1
		//     if we search for 3 or 2 or 1, we return 0
		//     if we search for 9, 10, or 11, we return 3
		public int relativeFloorBinarySearch(int lo, int hi) {
			if (lo > hi) return hi; // return the lower index (the value that is less than x)

			int mid = (lo + hi) / 2;
			if (Y[mid] == x) {
				return (checkRepeats) ? mid-1 : mid; // if repeat portion and there's a match, subtract by 1
			}
			else if (x > Y[mid]) {
				int newLow = mid+1;
				if (newLow > Y.length) return newLow-1;			// edge of array reached
				return relativeFloorBinarySearch(newLow, hi);
			} else {
				int newHi = mid-1;
				if (newHi < 0) return -1;						// edge of array reached
				return relativeFloorBinarySearch(lo, newHi);
			}
		}
		
	}
  
}
