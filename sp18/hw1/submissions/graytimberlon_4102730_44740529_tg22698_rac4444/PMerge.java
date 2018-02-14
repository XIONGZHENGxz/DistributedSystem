//UT-EID=tg22698, rac4444


import java.util.*;
import java.util.concurrent.*;
import java.lang.Thread;

public class PMerge {
	
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads) {
		ArrayList<Thread> threadList = new ArrayList<Thread>();
		HashSet<Integer> indices = new HashSet<Integer>();
		
		int idxA = 0;
		while (idxA < A.length) {
			if (java.lang.Thread.activeCount() < numThreads) {
				final int idx = idxA;
				idxA++;
				Thread t = new Thread (new Runnable() {
					@Override
					public void run() {
						int idxB = binarySearch(B, A[idx]);
						int i = idx + idxB + 1;
						if (indices.contains(i)) i -= 1;
						indices.add(i);
						C[i] = A[idx];
					}
				});
				threadList.add(t);
				t.start();
				//System.out.println(java.lang.Thread.activeCount());
			}
		}
		int idxB = 0;
		while (idxB < B.length) {
			if (java.lang.Thread.activeCount() < numThreads) {
				final int idx = idxB;
				idxB++;
				Thread t = new Thread (new Runnable() {
					@Override
					public void run() {
						int idxA = binarySearch(A, B[idx]);
						int i = idx + idxA + 1;
						if (indices.contains(i)) i -= 1;
						indices.add(i);
						C[i] = B[idx];
					}
				});
				threadList.add(t);
				t.start();
				//System.out.println(java.lang.Thread.activeCount());
			}
		}

		try {
			for (Thread t : threadList)
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println(Arrays.toString(C));
	}
	
	public static int binarySearch(int[] x, int v) {
		int n = x.length;
		int beg = 0;
		int mid = 0;
		int end = n - 1;
		while (beg <= end) {
			mid = (beg + end) / 2;
			if (v == x[mid]) return mid;
			if (v < x[mid]) end = mid - 1;
			else beg = mid + 1;
		}
		return end;
	}
}