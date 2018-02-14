//UT-EID=kw26853
//UT-EID=qaz62

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PSort extends RecursiveTask<Void> {
	public static void parallelSort(int[] A, int begin, int end) {
		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		pool.invoke(new PSort(A, begin, end));
	}

	private int[] A;
	private int begin;
	private int end;

	PSort(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end;
	}

	protected Void compute() {
		if(begin == end) {
			return null;
		}

		if(end - begin <= 16) {
			insertionSort(this.A, begin, end);
			return null;
		}

		int partition = partition(A, begin, end);
		PSort p1 = new PSort(A, begin, partition);
		p1.fork();
		PSort p2 = new PSort(A, partition + 1, end);
		p2.compute();

		p1.join();

		return null;
	}

	private int partition(int[] A, int begin, int end) {
		int pivot = A[end - 1];
		int i = begin - 1;

		for(int j = begin; j < end; j += 1) {
			if(A[j] < pivot) {
				i += 1;
				int temp = A[i];
				A[i] = A[j];
				A[j] = temp;
			}
		}

		if(A[end-1] < A[i + 1]) {
			int temp = A[i + 1];
			A[i + 1] = A[end-1];
			A[end-1] = temp;
		}

		return i + 1;
	}

	private void insertionSort(int[] A, int begin, int end) {
		int i = begin + 1;
		while(i < end) {
			int j = i;
			while(j > 0 && A[j-1] > A[j]) {
				int temp = A[j];
				A[j] = A[j-1];
				A[j-1] = temp;
				j -= 1;
			}

			i += 1;
		}
	}
}
