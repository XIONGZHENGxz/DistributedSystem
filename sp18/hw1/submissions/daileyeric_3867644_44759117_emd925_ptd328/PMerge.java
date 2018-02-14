
public class PMerge {
	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		// TODO: Implement your parallel merge function
		boolean longer;
		int element_count;
		if (A.length == 0 && B.length == 0)
			return;
		else if (A.length == 0 || B.length == 0) {
			longer = !(A.length == 0);
			element_count = longer ? A.length : B.length;
			int[] D = longer ? A : B;
			for (int i = 0; i < D.length; i++) {
				C[i] = D[i];
			}
			return;
		} else {
			if (A[0] >= B[0]) {
				longer = false;
				element_count = B.length;
			} else {
				longer = true;
				element_count = A.length;
			}
		}
		if (element_count < numThreads)
			numThreads = element_count;
		int mod = longer ? A.length % numThreads : B.length % numThreads;
		int size = longer ? A.length / numThreads : B.length / numThreads;
		AMerge[] a = new AMerge[numThreads];
		if (mod == 0) {
			if (longer) {
				for (int i = 0, j = 0; i < numThreads; i++, j += size) {
					a[i] = new AMerge(A, B, C, j, j + size - 1);
					// j += size;
				}
			} else {
				for (int i = 0, j = 0; i < numThreads; i++, j += size) {
					a[i] = new AMerge(B, A, C, j, j + size - 1);
					// j += size;
				}
			}
		} else {
			if (longer) {
				for (int i = 0, j = 0; i < numThreads; i++, j += size) {
					if (i == numThreads - 1)
						a[i] = new AMerge(A, B, C, j, j + size - 1 + mod);
					else
						a[i] = new AMerge(A, B, C, j, j + size - 1);
					// j += size;
				}
			} else {
				for (int i = 0, j = 0; i < numThreads; i++, j += size) {
					if (i == numThreads - 1)
						a[i] = new AMerge(B, A, C, j, j + size - 1 + mod);
					else
						a[i] = new AMerge(B, A, C, j, j + size - 1);
					// j += size;
				}
			}
		}
		for (int i = 0; i < a.length; i++) {
			a[i].start();
			try {
				a[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void actualMerge(int[] A, int[] B, int[] C, int index, int beginA, int beginB) {

	}
}

class AMerge extends Thread {
	int[] A; // will always be the longer of the two arrays
	int[] B;
	int[] C;
	int begin;
	int end;

	AMerge(int[] A, int[] B, int[] C, int begin, int end) {
		this.A = A;
		this.B = B;
		this.C = C;
		this.begin = begin;
		this.end = end;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int i, j, k;
		for (i = 0; A[begin] >= B[i] && i < B.length - 1; i++)
			;
		if (A[begin] >= B[i])
			i++;
		for (j = B.length - 1; A[end] < B[j] && j > 0; j--)
			;
		if (A[end] > B[j])
			j++;

		for (k = begin + i; i <= j && begin <= end && i < B.length && begin < A.length; k++) {
			System.out.println("A[begin] = " + A[begin] + " and B[i] = " + B[i] + " and i,j = " + i + j
					+ " and begin,end = " + begin + end);
			if (A[begin] > B[i]) {
				C[k] = B[i];
				i++;
				SimpleTestPmerge.printArray(C);
			} else {
				C[k] = A[begin];
				SimpleTestPmerge.printArray(C);
				begin++;
			}
		}
		for (; i <= j; i++, k++) {
			if (begin < A.length) {
				if (A[begin] > B[i]) {
					System.out.println("more B");
					C[k] = B[i];
				}
			} else {
				System.out.println("more B");
				C[k] = B[i];
			}
		}
		for (; begin <= end; begin++, k++) {
			if (i < B.length) {
				if (B[i] > A[begin]) {
					System.out.println("more A");
					C[k] = A[begin];
				}
			} else {
				System.out.println("more A");
				C[k] = A[begin];
			}
		}

		if (end == A.length - 1) {
			System.out.println("last B");
			for (; i < B.length; i++, k++) {
				C[k] = B[i];
			}
		}
		// i should be at the first index we want from B, and j should be at the last
		// index we want from B

	}

}
