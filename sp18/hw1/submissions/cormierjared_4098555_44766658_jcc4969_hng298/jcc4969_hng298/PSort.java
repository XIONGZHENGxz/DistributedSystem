
/* EE360P Assignment 1
   Hasan Genc: hng298
   Jared Cormier: jcc4969
   Section: 15825
*/

import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction {
	private int[] A;
	private int begin;
	private int end;
	private static ForkJoinPool pool;

	private PSort(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
	
	public static void parallelSort(int[] A, int begin, int end) {
		pool = new ForkJoinPool();
		PSort psort = new PSort(A, begin, end-1);
		pool.invoke(psort);
		pool.shutdown();
	}

	@Override
	protected void compute() {
		if((end-begin) < 16) {
			insertionSort(A, begin, end);
		}else {
			int pivot = partition(A, begin, end);
			PSort f1 = new PSort(A, begin, pivot-1);
			f1.fork();
			PSort f2 = new PSort(A, pivot+1, end);
			f2.compute();
			f1.join();
		}
	}
	
	private int partition(int[] A, int begin, int end) {
		int pivot = A[end];
		int i = begin-1;
		for(int j=begin; j<end; j++) {
			if(A[j] <=pivot) {
				i++;
				int temp = A[i];
				A[i] = A[j];
				A[j] = temp;
			}
		}
		int temp = A[i+1];
		A[i+1] = A[end];
		A[end] = temp;
		return i+1;
	}

	private void insertionSort(int[] A, int begin, int end) {
		for(int i=begin+1; i<end+1; i++) { 
			int temp = A[i];
			int j = i-1;
			while(j>=begin && A[j]>temp) {
				A[j+1] = A[j];
				j--;
			}
			A[j+1] = temp;
		}
	}
}

