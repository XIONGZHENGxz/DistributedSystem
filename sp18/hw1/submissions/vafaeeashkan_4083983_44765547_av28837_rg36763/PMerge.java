
//UT-EID1= av28837
//UT-EID2= rg36763

import java.util.*;
import java.util.concurrent.*;

public class PMerge extends Thread {
	ArrayList<Integer> list = new ArrayList<>();
	static int[] A;
	static int[] B;
	static int[] C;

	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {

		ArrayList<PMerge> threads = new ArrayList<PMerge>();
		PMerge.A = A;
		PMerge.B = B;
		PMerge.C = C;

		for (int i = 0; i < numThreads; i++) {
			threads.add(new PMerge());
		}

		// Compiling list of numbers each thread should analyze
		for (int i = 0; i < C.length; i++) {
			threads.get(i % numThreads).list.add(i);
		}

		// Starting all threads
		for (int i = 0; i < numThreads && i < C.length; i++) {
			threads.get(i).start();
		}

		// Waiting for all threads to die
		for (int i = 0; i < numThreads && i < C.length; i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
			}
		}

	}

	public void run() {

		for (int i = 0; i < list.size(); i++) {
			int n = list.get(i);
			int index, element;

			if (n < A.length) {
				index = n;
				element = A[n];

				for (int j = 0; j < B.length && element > B[j]; j++) {
					index++;
				}
			}

			else {
				index = n - A.length;
				element = B[index];

				for (int j = 0; j < A.length && element >= A[j]; j++) {
					index++;
				}
			}

			C[index] = element;
		}

	}
}
