//UT-EID=hc23882

import java.util.concurrent.*;

@SuppressWarnings("serial")
public class PSort extends RecursiveAction{
	
	private static int[] A;
	private int begin;
	private int end;
	
	public PSort(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
	
	public static void baseSort(int[] A, int begin, int end) {
		for (int i = begin+1; i < end; ++i) {
			if (A[i-1] > A[i]) {
				for (int j = i; j > begin; --j) {
					if (A[j-1] > A[j]) {
						A[j-1] ^= A[j];
						A[j] ^= A[j-1];
						A[j-1] ^= A[j];
					}
				}
			}
		}
	}

	public static void parallelSort(int[] A, int begin, int end){
		PSort.A = A;
		PSort mainTask = new PSort(begin,end);
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(mainTask);
	}

	@Override
	protected void compute() {
		// base cases
		int length = end-begin;
		if (length < 2) { 
			return;
		}
		if (length <= 16) {
			baseSort(A,begin,end);
			return;
		}
		
		// find pivot
		int pivotI = begin+length/2;
		int pivotV = A[pivotI];
		if (A[begin] > pivotV && A[end-1] > pivotV) { // if median is not in the middle
			if (A[begin] > A[end-1]) {
				pivotI = end-1;
			}
			else {
				pivotI = begin;
			}
			pivotV = A[pivotI];
		}
		else if (A[begin] < pivotV && A[end-1] < pivotV) {
			if (A[begin] > A[end-1]) {
				pivotI = begin;
			}
			else {
				pivotI = end-1;
			}
			pivotV = A[pivotI];
		}
		
		// place pivot in correct location
		int counter = 0;
		for (int i = begin; i < end; ++i) {
			if (A[i] < pivotV) {
				counter++;
			}
		}
		int newpivotI = begin + counter;
		if (newpivotI != pivotI) {
			A[pivotI] ^= A[newpivotI];
			A[newpivotI] ^= A[pivotI];
			A[pivotI] ^= A[newpivotI];
			pivotI = newpivotI;
		}
		
		// place values on correct side of pivot
		int i = begin;
		int j = pivotI+1;
		while(i < pivotI && j < end) {
			if (A[i] >= pivotV && A[j] < pivotV) {
				A[i] ^= A[j];
				A[j] ^= A[i];
				A[i] ^= A[j];
				i++;
				j++;
			}
			else {
				if (A[i] < pivotV) {
					i++;
				}
				if (A[j] >= pivotV) {
					j++;
				}
			}
		}
		
		// sort above and below pivot
		PSort taskA = new PSort(pivotI+1,end);
		PSort taskB = new PSort(begin,pivotI);
		taskA.fork();
		taskB.compute();
		taskA.join();
	}
}
