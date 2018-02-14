//UT-EID= gpz68 and db37299


import java.util.*;
import java.util.concurrent.*;


public class PMerge extends Thread{
	
	int[] A;
	int[] B;
	int[] C;
	int numThreads;
	
	PMerge(int[] inA, int[] inB, int[] inC, int threadCount) {
		A = inA;
		B = inB;
		C = inC;
		numThreads = threadCount;
	}
	
	public class lilTask extends RecursiveTask<Void> {
		
		ArrayList<Integer> indices;
		
		lilTask(ArrayList<Integer> x) {
			indices = x;
		}

		@Override
		protected Void compute() {
			int num;
			Boolean arrayA;
			int index;
			for (int i = 0; i < indices.size(); i++) {
				index = indices.get(i);
				if (index < A.length) {
					arrayA = true;
					num = A[index];
				}
				else {
					arrayA = false;
					index = index - A.length;
					num = B[index];
				}
				
				
				// binary search instead of linear search
				int j = 0;
				Boolean found = false;
				if (arrayA) {
					while (j < B.length && !found) {
						if (B[j] > num) {
							found = true;
							j--;
						}
						j++;
					}
				}
				
				else {
					while (j < A.length && !found) {
						if (A[j] >= num) {
							found = true;
							j--;
						}
						j++;
					}
				}
				
				C[j + index] = num;
				
			}
			
			return null;
		}
		
	}
	
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		
		PMerge input = new PMerge(A, B, C, numThreads);
		int processors = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processors);
		
		int aLen = A.length;
		int bLen = B.length;
		int totalLen = aLen + bLen;
		int index = 0;
		int num = totalLen/numThreads;
		if (num <= 0) {
			num = 1;
		}
		while (index < totalLen) {
			ArrayList<Integer> indices = new ArrayList<Integer>();
			for (int i = 0; i < num; i++) {
				indices.add(index);
				index++;
			}
			
			if (totalLen-index < num) {
				while (index < totalLen) {
					indices.add(index);
					index++;
				}
			}
			lilTask task = input.new lilTask(indices);
			pool.submit(task);
		}
		
		while (!pool.isQuiescent()) {}
		return;
	}

}