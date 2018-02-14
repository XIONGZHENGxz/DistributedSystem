
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
