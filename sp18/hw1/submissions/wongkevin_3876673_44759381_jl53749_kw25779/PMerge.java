//UT-EID= jl53749, kw25779


import java.util.*;
import java.util.concurrent.*;


public class PMerge extends RecursiveAction{
	int index;
	int[] A;
	int[] B;
	int[] C;
	int numThreads;
	boolean AorB;

	PMerge (int index, int[] A, int[] B, int[] C, int numThreads) {
		this.index = index;
		this.A = A;
		this.B = B;
		this.C = C;
		this.numThreads = numThreads;
	}
	PMerge (boolean AorB, int index, int[]A, int[]B, int[]C) {
		this.AorB = AorB;
		this.index = index;
		this.A = A;
		this.B = B;
		this.C = C;
	}
	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		// TODO: Implement your parallel merge function


		//this is testing the basic functionality
	  /*
	  //not parallel (testing functionality)
	  int Aind = 0 ;
	  int Bind = 0 ;
	  while (Aind != A.length && Bind != B.length) {
		  if (A [Aind] <= B [Bind]) {
			  C [Aind + Bind] = A [Aind];
			  Aind++;
		  }
		  else {
			  C [Aind + Bind] = B [Bind];
			  Bind++;
		  }
	  }
	  while (Aind != A.length){
		  C [Aind+Bind] = A[Aind];
		  Aind++;
	  }
	  while (Bind != B.length) {
		  C [Aind+Bind] = B[Bind];
		  Bind++;
	  }
	  */
		ForkJoinPool pool = new ForkJoinPool(numThreads);

		for (int i = 0 ; i < A.length ; i ++ ) {
			PMerge merger = new PMerge(true, i, A, B, C);
			pool.invoke(merger);
		}
		for (int i = 0 ; i < B.length ; i ++) {
			PMerge merger = new PMerge(false, i, A, B, C);
			pool.invoke(merger);
		}
		//System.out.println(A.length + B.length + " " + Runtime.getRuntime().availableProcessors());
		//prints out final array
	  /*
	  for (int i = 0 ; i < C.length ; i++){
		  System.out.print(C[i] + " ");
	  }
	  System.out.println();
	  */
	}

	@Override
	protected void compute() {
		// TODO Auto-generated method stub

		if (AorB) { // sorting somethign in list A
			int beg = 0;
			int end = B.length - 1;
			int mid = 0;
			while (end >= beg) {
				mid = (end+beg) / 2;
				if (A[index] < B[mid]) {
					end = mid - 1;
				}
				else if ( A[index] == B[mid]) {
					C[index + mid] = A[index];
					break;
				}
				else {
					beg = mid + 1 ;
				}
			}
			C[index+end+1] = A[index];
		}
		else {
			int beg = 0;
			int end = A.length - 1;
			int mid = 0;
			while (end >= beg) {
				mid = (end+beg) / 2;
				if (B[index] < A[mid]) {
					end = mid - 1;
				}
				/*
				else if ( B[index] == A[mid]) {
					C[index + mid+1] = B[index];
				}
				*/
				else { //if they are equal you move mid as far up as possible
					beg = mid + 1 ;
				}
			}
			C[index+end+1] = B[index];
		}

	}
}