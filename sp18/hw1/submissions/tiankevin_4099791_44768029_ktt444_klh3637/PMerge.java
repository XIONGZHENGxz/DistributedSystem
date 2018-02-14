//UT-EID= ktt444, klh3637


import java.util.*;
import java.util.concurrent.*;


public class PMerge {

	private static ForkJoinPool pool;
	
	private static class PMergeTask extends RecursiveAction{
		private int[] A,B;
		private static int[] C;
		private int start;
		
		PMergeTask(int[] A, int[] B, int[] C, int x){
			this.A = A;
			this.B = B;
			this.C = C;
			this.start = x;
			}
		
		@Override
		protected void compute() {
			
			if(A.length < B.length) {	
				/*SWAP*/
				int[] temp = A;
				A = B;
				B = temp;
		
			}if(A.length <= 0) {
				return;
			}
			
			int middle = A.length/2;
			int search = search(A[middle], B);
			int index = middle + search + start;
			C[index] = A[middle];
			
			int[] A2 = new int[middle];
			int[] A3 = new int[A.length - (middle+1)];
			int[] B2 = new int[search];
			int[] B3 = new int[B.length - search];
			
			System.arraycopy(A, 0, A2, 0, middle);
			System.arraycopy(A, middle+1, A3, 0, A.length-(middle+1));
			System.arraycopy(B, 0, B2, 0, search);
			System.arraycopy(B, search, B3, 0, B.length-search);
			
			invokeAll(new PMergeTask(A2,B2,C,start),
					new PMergeTask(A3,B3,C,index+1));
		}
		
		public int search(int x, int[] arr) {
			int count = 0;
			for(int i = 0; i < arr.length; i++) {
				if(x <= arr[i]) break;
				count++;
			}
			return count;
		}
		
//		public synchronized static void printArray(int[] A) {
//		    for (int i = 0; i < A.length; i++) {
//		      if (i != A.length - 1) {
//		        System.out.print(A[i] + " ");
//		      } else {
//		        System.out.print(A[i]);
//		      }
//		    }
//		    System.out.println();
//		  }
		
	}

	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
		pool = new ForkJoinPool(numThreads);
		ForkJoinTask<Void> job = pool.submit(new PMergeTask(A,B,C,0));
		job.join();
	}
	

}
