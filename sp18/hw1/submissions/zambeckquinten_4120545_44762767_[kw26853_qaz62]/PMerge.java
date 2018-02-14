//UT-EID=kw26853
//UT-EID=qaz62

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PMerge implements Runnable {
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		ArrayList<Callable<Object>> mergeTasks = new ArrayList<>();

		for(int i = 0; i < A.length; i += 1) {
			mergeTasks.add(Executors.callable(new PMerge(i, A, B, C)));
		}

		for(int i = 0; i < B.length; i += 1) {
			mergeTasks.add(Executors.callable(new PMerge(i, B, A, C)));
		}

		try {
			exec.invokeAll(mergeTasks);

			System.out.println("Done!");
			System.out.println(Arrays.toString(C));
		} catch(Exception e) {

		}

	}

	int index;

	int[] origin;
	int[] other;
	int[] dest;

	// find insert the value at origin[index] into dest
	public PMerge(int index, int[] origin, int[] other, int[] dest) {
		this.index = index;
		this.origin = origin;
		this.other = other;
		this.dest = dest;
	}

	public void run() {
		this.dest[index + binarySearch(origin[index], other)] = origin[index];
	}

	// return position of needle in haystack
	public int binarySearch(int needle, int[] haystack) {
		int lower = 0;
		int higher = haystack.length - 1;

		while(lower <= higher) {
			int mid = (lower + higher) / 2;

			if(haystack[mid] < needle) {
				lower = mid + 1;
			} else if(haystack[mid] > needle) {
				higher = mid - 1;
			} else if(haystack[mid] == needle){
				return mid;
			}
		}

		return lower; // not found, return index
	}

	public Void compute() {
		return null;
	}
}
