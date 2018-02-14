//UT-EID= gg25488, dk23765


import java.util.concurrent.*;

public class PSort{
	private static class Quick extends RecursiveTask<Integer>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int[] A;
		private int begin;
		private int end;

		public Quick(int[] A_, int begin_, int end_){
			A = A_;
			begin = begin_;
			end = end_;
		}

		@Override
		protected Integer compute(){
			if(end - begin <= 15){										// do insert sort
				for(int i = begin + 1; i <= end; i++){
					int j = i;
					while((j > begin) && (A[j - 1] > A[j])){			// shift next element down if larger
						int hold = A[j];
						A[j] = A[j - 1];
						A[j - 1] = hold;
						j--;
					}
				}
				return 1;
			}
			int mid_val = A[begin];
			int i = begin - 1;
			int j = end + 1;
			while(true) {
				do {
					i++;
				}while(A[i] < mid_val);
				
				do{
					j--;
				}while(A[j] > mid_val) ;
				
				if(i >= j) {
					mid_val = j;
					break;
				}
				
				int hold = A[i];
				A[i] = A[j];
				A[j] = hold;
			}
			Quick left = new Quick(A, begin, mid_val);			// perform quicksort on left subarray
			left.fork();
			Quick right = new Quick(A, mid_val+1, end);			// perform quicksort on right subarray
			right.compute();											// fork and wait on right
			left.join();												// wait on left
			return 1;
		}
	}


  	public static void parallelSort(int[] A, int begin, int end){
  		if(A == null)
  			return;

  		ForkJoinPool pool = new ForkJoinPool(2);						// use 2 threads
  		pool.invoke(new Quick(A, begin, end-1));
  		pool.shutdown();
  	}
}