//UT-EID=tg22698, rac4444


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<int[]> {
	private static final long serialVersionUID = 1L;
	final int[] A;
	final int begin;
	final int end;
	  
	PSort(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
	  
	protected int[] compute() {
		if (end - begin <= 16) {
			insertionSort(begin, end);
		} else {
			int pivot = partition();
			PSort left = new PSort(A, begin, pivot);
			left.fork();
			PSort right = new PSort(A, pivot + 1, end);
			right.compute();
		}
		return A;
		/*if ((n == 0)||(n == 1 )) return 1;
		PSort f1 = new PSort(n - 1);
		f1.fork();
		PSort f2 = new PSort(n - 2);
		return f2.compute() + f1.join();*/
	}
	
	private int partition() {
		int pivot = end-1;
		int current = begin;
		while (pivot > current) {
			if(A[current] >= A[pivot]) {
				int temp = A[pivot];
				A[pivot] = A[current];
				A[current] = A[pivot-1];
				A[pivot-1] = temp;
				pivot = pivot-1;
			} else {
				current++;
			}
		}
		return pivot;
	}
	
	private void insertionSort(int begin, int end) {
		int n = end - begin;
		for (int i = 1; i < n; i++) {
			for (int j = i; j>0 && A[j + begin]<A[j-1 + begin]; j--) {
				int temp = A[j + begin];
				A[j + begin] = A[j-1 + begin];
				A[j-1 + begin] = temp;
			}
		}
	}
	  
	public static void parallelSort(int[] A, int begin, int end){
		// TODO: Implement your parallel sort function 
	    int processors = Runtime.getRuntime().availableProcessors();
	    System.out.println("Number of processors: " + processors);
	    PSort f = new PSort(A, begin, end); //TODO
	    ForkJoinPool pool = new ForkJoinPool(processors);
	    pool.invoke(f);
	}
}