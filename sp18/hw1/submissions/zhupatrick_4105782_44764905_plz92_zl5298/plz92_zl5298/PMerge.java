// zl5298
// plz92

import java.util.*;
import java.util.concurrent.*;

public class PMerge {
	public static void parallelMerge(int[]A, int[]B, int[]C, int numThreads) {
		// if numThreads is greater than the number of elements in A or B,
		// the lesser number of elements should be the new numThreads
		if(numThreads > A.length || numThreads > B.length) {
			numThreads = (A.length > B.length) ? B.length : A.length;
		}
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		ArrayList<Integer[]> apple = scheduleTasks(A, numThreads);
		ArrayList<Integer[]> bell = scheduleTasks(B, numThreads);
		
		/*for(int i = 0; i < apple.size(); i++) {
			System.out.println(Arrays.toString(apple.get(i)));
		}
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!");
		for(int i = 0; i < bell.size(); i++) {
			System.out.println(Arrays.toString(bell.get(i)));
		}*/
		
		for(int i = 0; i < numThreads; i++) {
			threadPool.submit(new PMergeTask(apple.get(i), bell.get(i), A, B, C, numThreads, i));
		}
		threadPool.shutdown();
		while(threadPool.isTerminated() == false);
	}
	
	public static ArrayList<Integer[]> scheduleTasks(int[] input, int numThreads) {
		ArrayList<Integer[]> integerList = new ArrayList<Integer[]>();
		
		int chunkSize, leftoverLength, begin, end;
		chunkSize = input.length / numThreads;
		leftoverLength = input.length;
		
		for(int index = 0; index < numThreads - 1; index++) {
			begin = input.length - leftoverLength;
			end = begin + chunkSize;
			Integer[] subArray = new Integer[chunkSize];
			for(int i = begin; i < end; i++) {
				subArray[i - begin] = input[i];
			}
			integerList.add(subArray);
			leftoverLength = leftoverLength - chunkSize;
		}
		Integer[] subArray = new Integer[leftoverLength];
		begin = input.length - leftoverLength;
		for(int i = begin; i < input.length; i++) {
			subArray[i - begin] = input[i];
		}
		integerList.add(subArray);

		return integerList;
	}
}