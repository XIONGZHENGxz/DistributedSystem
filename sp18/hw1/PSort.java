

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class PSort {
	public static void parallelSort(int[] A, int begin, int end) {
		// TODO: Implement your parallel sort function
		ASort a = new ASort(A, begin, end - 1);
		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		pool.invoke(a);
	}
}

class ASort extends RecursiveAction {
	static int[] A;
	int begin;
	int end;

	ASort(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end;
	}

	@Override
	protected void compute() {
		// TODO Auto-generated method stub
		if (end - begin <= 16) {
			insertionSort(begin, end + 1);
		} else {
			int p = partition(begin, end);
			// System.out.println("partition: (" + p + ", " + A[p] + ")");
			// System.out.print("after partition: ");
			// printPiece(A, begin, p);
			// printPiece(A, p, end);
			ASort a1 = new ASort(A, begin, p - 1);

			ASort a2 = new ASort(A, p + 1, end);
			a1.fork();
			a1.join();
			a2.fork();
			a2.join();

		}

	}

	private void insertionSort(int begin2, int end2) {
		// TODO Auto-generated method stub
		// System.out.println(Thread.currentThread().getName() + "- begin -" + begin2 +
		// " - end - " + end2);

		int i = 1;
		while (i < end2 - begin2) {
			int j = i;
			while (j > 0 && A[begin2 + j - 1] > A[begin2 + j]) {
				swap(j + begin2, begin2 + j - 1);
				j--;
			}
			i++;
		}
	}

	public static void printPiece(int[] A, int begin, int end) {
		for (int i = begin; i < end; i++) {
			if (i != end - 1) {
				System.out.print(A[i] + " ");
			} else {
				System.out.print(A[i]);
			}
		}
		System.out.println();
	}

	private static int partition(int begin, int end) {
		// System.out.print("array for partition: ");
		// printPiece(A, begin, end);
		int pivot = A[end];
		int i = begin - 1;
		for (int j = begin; j <= end - 1; j++) {
			if (A[j] <= pivot) {
				i++;
				swap(i, j);
			}
		}

		swap(i + 1, end);
		return i + 1;
	}

	private static void swap(int i, int j) {
		// TODO Auto-generated method stub
		int temp = A[i];
		A[i] = A[j];
		A[j] = temp;
	}
}