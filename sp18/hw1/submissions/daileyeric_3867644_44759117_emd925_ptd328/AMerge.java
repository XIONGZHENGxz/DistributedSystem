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
		//System.out.println("Thread " + Thread.currentThread().getId() + " is starting: " + begin + ", " + end);
		// TODO Auto-generated method stub
		int i, j, k;
		for (i = 0; A[begin] > B[i] && i < B.length - 1; i++)
			;
		if (A[begin] > B[i])
			i++;
		for (j = B.length - 1; A[end] <= B[j] && j > 0; j--)
			;
		if (A[end] > B[j] && j < B.length - 1)
			j++;

		for (k = begin + i; i <= j && begin <= end && i < B.length && begin < A.length; k++) {
			//System.out.println("A[begin] = " + A[begin] + " and A[end] = " + A[end] + " and begin,end = " + begin + end);
			//System.out.println("B[i] = " + B[i] + " and B[j] = " + B[j] + " and i,j = " + i + j);
			if (A[begin] > B[i]) {
				C[k] = B[i];
				i++;
				//SimpleTestPmerge.printArray(C);
			} else {
				C[k] = A[begin];
				//SimpleTestPmerge.printArray(C);
				begin++;
			}
		}
		for (; i < B.length; i++, k++) {
			if (begin < A.length) {
				if (A[begin] >= B[i]) {
					//System.out.println("more B");
					C[k] = B[i];
				}
			} else {
				//System.out.println("more B");
				C[k] = B[i];
			}
		}
		for (; begin <= end; begin++, k++) {
			if (i < B.length) {
				if (B[i] > A[begin]) {
					//System.out.println("more A");
					C[k] = A[begin];
				}
			} else {
				//System.out.println("more A");
				C[k] = A[begin];
			}
		}

		if (end == A.length - 1) {
			//System.out.println("last B");
			for (; i < B.length; i++, k++) {
				C[k] = B[i];
			}
		}
		// i should be at the first index we want from B, and j should be at the last
		// index we want from B
		//SimpleTestPmerge.printArray(A);
		//SimpleTestPmerge.printArray(B);
		//SimpleTestPmerge.printArray(C);
		//System.out.println("Thread " + Thread.currentThread().getId() + " is done");
	}

}
