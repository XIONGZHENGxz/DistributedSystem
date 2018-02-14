//UT-EID= kh33248, jlh6554

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class PSort extends RecursiveAction {
	
	final int begin;
	final int end;
	final int[] A;
	
	public PSort(int[] A, int begin, int end) {
		this.begin = begin;
		this.end = end;
		this.A = A;
	}
	
	// "main"
	public static void parallelSort(int[] A, int begin, int end) {
		// TODO: Implement your parallel sort function
		int processors = Runtime.getRuntime().availableProcessors();
		PSort ps = new PSort(A, begin, end);
		ForkJoinPool pool = new ForkJoinPool(processors);
		pool.invoke(ps);
		pool.shutdown();
	}
	
	public int partition() {
		int pivot = A[end - 1];
		int i = begin - 1;
		
		for (int j = begin; j < end - 1; j++) {
			if (A[j] <= pivot) {
				i++;
				swap(i, j);
			}
		}
		
		swap(i + 1, end - 1);
		
		return i + 1;
	}
	
	public void swap(int x, int y) {
		int temp = A[x];
		A[x] = A[y];
		A[y] = temp;
	}
	
	public void insertionSort() {
		for (int i = begin + 1; i < end; i++) {
			int j = i - 1;
			int save = A[i];
		
			
			while (j >= 0 && save < A[j]) {
				A[j + 1] = A[j];
				j--;
			}
			A[j + 1] = save;
		}
	}

	// "sort"
	@Override
	protected void compute() {
		// TODO Auto-generated method stub
		
		if (begin < end) {
			if (end - begin <= 16) {
				// do insertion sort on array A
				insertionSort();
			}
			else {
				// divide and conquer
				int indx = partition();
				PSort ps1 = new PSort(A, begin, indx);
				ps1.fork();
				PSort ps2 = new PSort(A, indx + 1, end);
				ps2.compute();
				ps1.join();

			}
		}
	}
}
