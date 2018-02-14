package ee360p_hw1;

//UT-EID=dzc88 and ass962

import java.util.*;
import java.util.concurrent.*;

public class PMerge extends RecursiveTask<Void> {
	final int[] A, B, C;
	final int pos;

	public PMerge(int[] A, int[] B, int[] C, int pos) {
		this.A = A;
		this.B = B;
		this.C = C;
		this.pos = pos;
	}
	
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		PMerge[] processes = new PMerge[C.length];
		
		ForkJoinPool pool = new ForkJoinPool(numThreads);
		
		for (int i = 0; i < C.length; i++) {
			processes[i] = new PMerge(A, B, C, i);
			pool.execute(processes[i]);
		}
		
		pool.shutdown();
		
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected Void compute() {
		C[pos] = merge(pos);
		return null;
	}
	
	protected int merge(int pos) {
		int i = 0, j = 0, index = -1, number = -1;
		
		while (i < A.length && j < B.length && index < pos) {
			if (A[i] <= B[j]) {
				number = A[i];
				i++;
			}
			else {
				number = B[j];
				j++;
			}
			index++;
		}
		
		while (i < A.length && index < pos) {
			number = A[i];
			i++;
			index++;
		}
		
		while (j < B.length && index < pos) {
			number = B[j];
			j++;
			index++;
		}
		return number;
	}
}