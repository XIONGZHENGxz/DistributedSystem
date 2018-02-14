
//UT-EID1= av28837
//UT-EID2= rg36763

import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<ArrayList<Integer>> {
	ArrayList<Integer> A;
	int begin;
	int end;

	public static void parallelSort(int[] A, int begin, int end) {
		ArrayList<Integer> AL = new ArrayList<Integer>();

		for (int i = begin; i < end; i++) {
			AL.add(A[i]);
		}

		PSort ps = new PSort();
		ps.A = AL;
		ps.begin = begin;
		ps.end = end;

		ForkJoinPool pool = new ForkJoinPool();
		ArrayList<Integer> result = pool.invoke(ps);

		for (int i = begin, k=0; i < end; i++, k++) {
			A[i] = result.get(k);
		}
	}

	@Override
	protected ArrayList<Integer> compute() {
		int arraySize = A.size();

		// Insert sort for array size <= 16
		if (arraySize <= 16) {
			for (int i = 0; i < A.size(); i++) {
				int k = i;
				while ((k > 0) && (A.get(k - 1) > A.get(k))) {
					int temp = A.get(k);
					A.set(k, A.get(k - 1));
					A.set(k - 1, temp);
					k--;
				}
			}

			return A;
		}

		else {
			ArrayList<Integer> A1 = new ArrayList<Integer>();
			ArrayList<Integer> A2 = new ArrayList<Integer>();

			// Choosing pivot element
			int pivot = A.get(A.size() / 2);
			for (int i = 0; i < A.size(); i++) {
				if (A.get(i) >= pivot) {
					A1.add(A.get(i));
				} else {
					A2.add(A.get(i));
				}
			}

			PSort ps1 = new PSort();
			ps1.A = A1;
			ps1.begin = 0;
			ps1.end = A1.size();
			ps1.fork();

			PSort ps2 = new PSort();
			ps2.A = A2;
			ps2.begin = 0;
			ps2.end = A2.size();

			ArrayList<Integer> result = ps2.compute();
			result.addAll(ps1.join());

			return result;

		}
	}
}
