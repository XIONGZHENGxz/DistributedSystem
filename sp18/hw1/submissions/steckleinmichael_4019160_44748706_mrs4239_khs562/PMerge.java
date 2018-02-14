//UT-EID=mrs4239
//UT-EID=khs562

import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
	ExecutorService pool = Executors.newCachedThreadPool();
	assignTasks(pool, A, B, C, numThreads);
	pool.shutdown();
	try {
		pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	} catch (InterruptedException e) { System.exit(1); }
  }

  private static void assignTasks(ExecutorService pool, int[] arr1, int[] arr2, int[] dest, int numThreads) {
	double stride = ((double)(arr1.length + arr2.length)) / numThreads;
	for (int i = 0; i < numThreads; i++) {
		int startIndex = (int)(i*stride);
		int endIndex = (int)((i+1)*stride);
		if (endIndex <= arr1.length) { // completely contained in arr1
			pool.submit(new SequentialMerge(arr1,startIndex,endIndex, arr2,0,0, dest));
		} else if (startIndex >= arr1.length) { // completely contained in arr2
			pool.submit(new SequentialMerge(arr1,0,0, arr2,(startIndex-arr1.length),(endIndex-arr1.length), dest));
		} else { // (startIndex is before arr1-arr2-split and endIndex is after), straddles arr1 & arr2
			pool.submit(new SequentialMerge(arr1,startIndex,arr1.length, arr2,0,(endIndex-arr1.length), dest));
		}
	}
  }
}

class SequentialMerge implements Runnable {

	private int[] A;
	private int beginA;
	private int endA;
	private int[] B;
	private int beginB;
	private int endB;
	private int[] C;

	public SequentialMerge(int[] A, int beginA, int endA, int[] B, int beginB, int endB, int[] C) {
		this.A = A;
		this.beginA = beginA;
		this.endA = endA;
		this.B = B;
		this.beginB = beginB;
		this.endB = endB;
		this.C = C;
	}

	@Override
	public void run() {
		// merge entries from A
		for (int i = beginA; i < endA; i++) {
			mergeEntry(A, i, B, C, false);
		}
		// merge entries from B
		for (int i = beginB; i < endB; i++) {
			mergeEntry(B, i, A, C, true);
		}
	}

	private static void mergeEntry(int[] src, int index, int[] other, int[] dest, boolean takeLowerIndex) {
		int value = src[index];
		int destIndex = index + binarySearchIndex(other, value);
		if (takeLowerIndex && binarySearchContains(other, value)) // duplicate found
			destIndex--; // take lower index
		dest[destIndex] = value;
	}

	/**
	 * Returns the index where value is '<=' all values left of it in arr, and
	 * the index and all values right a '>' value.
	 * @param arr
	 * @param value
	 * @return an index
	 */
	private static int binarySearchIndex(int[] arr, int value) {
		int l = 0;
		int r = arr.length;
		while (l != r) {
				int midpoint = (l+r)/2;
				if (value < arr[midpoint])
						r = midpoint;
				else // value >= arr[midpoint]
						l = midpoint+1;
		}
		return l;
	}
	
	private static boolean binarySearchContains(int[] arr, int value) {
		int l = 0;
		int r = arr.length-1;
		while (l <= r) {
				int midpoint = (l+r)/2;
				if (value == arr[midpoint])
					return true; // found
				else if (value < arr[midpoint])
						r = midpoint-1;
				else // value > arr[midpoint]
						l = midpoint+1;
		}
		return false;
	}
	
}
