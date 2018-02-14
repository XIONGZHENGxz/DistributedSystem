
//UT-EID=kc28535, ahp473
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class QSort extends RecursiveTask<Void> {
	int[] A;
	int begin;
	int end;

	public QSort(int[] A, int begin, int end) {
		this.A = A;
		this.begin = begin;
		this.end = end;
	}

	@Override
	protected Void compute() {
		// TODO Auto-generated method stub
		if (begin == end)
			return null;

		if (end - begin <= 16) {
		insertSort(A, begin, end);
		}

		if (begin < end) {
			int pi = partition(A, begin, end);

			QSort q1 = new QSort(A, begin, pi - 1);
			q1.fork();
			QSort q2 = new QSort(A, pi + 1, end);
			q2.fork();
			q1.join();
			q2.join();
		}

		return null;
	}

	int partition(int[] A, int low, int high) {
		if (high == A.length)
			high--;

		int pivot = A[high];
		int i = low - 1;
		for (int j = low; j < high; j++) {
			if (A[j] <= pivot) {
				i++;
				swap(A, i, j);
			}
		}

		swap(A, i + 1, high);
		return i + 1;
	}

	public static void insertSort(int[] A, int begin, int end) {
		for (int i = begin + 1; i < end; i++) {
			int j = i;
			while (j > 0 && A[j - 1] > A[j]) {
				swap(A, j - 1, j);
				j--;
			}
		}
	}

	public static void swap(int[] A, int i, int j) {
		int temp = A[i];
		A[i] = A[j];
		A[j] = temp;
	}
}
