
//UT-EID= mjo588
//UT-EID= ta9887

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@SuppressWarnings("serial")
public class PSort extends RecursiveAction {
	private int[] array;
	private int begin;
	private int end;

	public PSort(int[] array, int begin, int end) {
		super();
		this.array = array;
		this.begin = begin;
		this.end = end;
	}

	public static void parallelSort(int[] A, int begin, int end) {
		int processors = Runtime.getRuntime().availableProcessors();
		PSort p = new PSort(A, begin, end);
		ForkJoinPool pool = new ForkJoinPool(processors);
		pool.invoke(p);
	}

	@Override
	protected void compute() {
		if ((end - begin) <= 16) {
			insertionSort(array, begin, end);
			return;
		}

		int pivot = partition(array, begin, end - 1);
		PSort p1 = new PSort(array, begin, pivot);
		PSort p2 = new PSort(array, pivot + 1, end);
		p1.fork();
		p2.fork();
		p1.join();
		p2.join();

		return;
	}

	// Wikipedia Lomuto Partition Scheme Pseudocode
	private static int partition(int[] A, int begin, int end) {
		int pivot = A[end];
		int x = begin - 1;
		for (int y = begin; y < end; y++) {
			if (A[y] < pivot) {
				x = x + 1;
				swap(A, x, y);
			}
		}
		swap(A, x + 1, end);
		return x + 1;
	}

	private static void insertionSort(int[] A, int begin, int end) {
		if ((end - begin) <= 1)
			return;
		for (int x = begin + 1; x < end; x++) {
			int y = x;
			while (y > begin && A[y - 1] > A[y]) {
				swap(A, y, y - 1);
				y--;
			}
		}
	}

	private static void swap(int[] A, int x, int y) {
		int temp = A[x];
		A[x] = A[y];
		A[y] = temp;
	}
}