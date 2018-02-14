/* Brendan Ngo bdn378
 * Neil Charles nc23228
 * */

import java.util.Arrays;
import java.util.concurrent.*;
public class PMerge {
	public static ExecutorService threadPool;
	public static boolean[] Cused;
	
	//arrays A and B are sorted//array C is the merged array// your implementation goes here.}}
	public static int parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		Cused = new boolean[A.length+B.length];
		
		threadPool = Executors.newFixedThreadPool(numThreads);
		
		for(int i = 0; i < A.length; i++) {
			threadPool.submit(new Merge(A[i], i, B, C));
		}
		for(int i = 0; i < B.length; i++) {
			threadPool.submit(new Merge(B[i], i, A, C));
		}
		threadPool.shutdown();
		return 0;
	}
	/*Check index location to see if there is a repeat
	 * 
	 * */
	public static synchronized void addElement(int index, int value, int[] finalArr) {
		if (Cused[index] == true) {
			index++;
		}
		finalArr[index] = value;
		Cused[index] = true;
	}
}

class Merge implements Callable<Integer> {
	int[] secondArr, finalArr;
	int firstInt, ind;
	
	public Merge(int firstInt, int ind, int[] secondArr, int[] finalArr) {
		this.firstInt = firstInt;
		this.secondArr = secondArr;
		this.finalArr = finalArr;
		this.ind = ind;
	}
	
	@Override
	public Integer call() throws Exception {
		int secondIndex = returnIndex(0, secondArr.length-1, firstInt);
		int finalIndex = ind+secondIndex;
		PMerge.addElement(finalIndex, firstInt, finalArr);

		return null;
	}	
	
	public int returnIndex(int start, int end, int value) {
		int mid = 0;
		while (start <= end) {
			mid = start + (end-start)/2;
			
			if (secondArr[mid] == value) {
				return mid;
			}
			
			if (secondArr[mid] > value) {
				end = mid - 1;
			}
			else {
				start = mid + 1;
			}
		}
		if (start == (mid + 1)) {
			return mid+1;
		}
		else {
			return mid;
		}
	}
}