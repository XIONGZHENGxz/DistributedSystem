//UT-EID= gg25488, dk23765


import java.util.concurrent.*;


public class PMerge{
	private static class Merge extends RecursiveTask<Integer>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int[] A;
		private int[] B;
		private int[] C;
		private int A_beg;
		private int A_end;
		private int B_beg;
		private int B_end;
		private int C_beg;
		private int C_end;

		public Merge(int[] A_, int[] B_, int[] C_, int begin_A, int end_A, int begin_B, int end_B, int begin_C, int end_C){
			A = A_;
			B = B_;
			C = C_;
			A_beg = begin_A;
			A_end = end_A;
			B_beg = begin_B;
			B_end = end_B;
			C_beg = begin_C;
			C_end = end_C;
		}

		@Override
		protected Integer compute(){
			int A_len = A_end - A_beg;								// calculate subarray lengths
			int B_len = B_end - B_beg;
			if(A_len > B_len){
				if(B_len == 0){										// B empty, A not
				  int j = C_beg;
	              for(int i = A_beg; i < A_end; i++){				// Fill C with A, then return	
	                    C[j++] = A[i];
	              }
	              return 1;
				}
				int mid_A = (A_beg + A_end) >> 1;					// Select middle element from longer subarray
				int mid_B = B_end;
				for(int i = B_beg; i < B_end; i++){					// find element index in smaller subarray that is >=
					if(B[i] >= A[mid_A]){							// to middle element from other array
						mid_B = i;									// split both arrays at these indecies to create 4 new subarrays
						break;
					}
				}
				int mid_C = mid_A + mid_B;							// calculate point to split C at				
				Merge left = new Merge(A, B, C, A_beg, mid_A, B_beg, mid_B, C_beg, mid_C);
				left.fork();
				Merge right = new Merge(A, B, C, mid_A, A_end, mid_B, B_end, mid_C, C_end);
				right.compute();
				left.join();
			}
			else{
				if(B_len == 0){										// B and A empty, exit
					return 1;
				}
				if(A_len == 0) {									// A empty, B not
				  int j = C_beg;
	              for(int i = B_beg; i < B_end; i++){				// Fill C with B, then return
	                    C[j++] = B[i];
	              }
	              return 1;
				}
				if(A_len == 1 && B_len == 1)
                {
                    if(A[A_beg] < B[B_beg]) {
                        C[C_beg] = A[A_beg];
                        C[C_beg + 1] = B[B_beg];
                    }
                    else {
                        C[C_beg] = B[B_beg];
                        C[C_beg + 1] = A[A_beg];
                    }
                    return 1;
                }
	          	int mid_B = (B_beg + B_end) >> 1;
				int mid_A = A_end;
				for(int i = A_beg; i < A_end; i++){
					if(A[i] >= B[mid_B]){
						mid_A = i;
						break;
					}
				}
				int mid_C = mid_A + mid_B;
				Merge left = new Merge(A, B, C, A_beg, mid_A, B_beg, mid_B, C_beg, mid_C);
				left.fork();
				Merge right = new Merge(A, B, C, mid_A, A_end, mid_B, B_end, mid_C, C_end);
				right.compute();
				left.join();
			}
			return 1;
		}
	}

  	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
  		ForkJoinPool pool = new ForkJoinPool(numThreads);						// use numThreads threads
  		pool.invoke(new Merge(A, B, C, 0, A.length, 0, B.length, 0, C.length));
  		pool.shutdown();
  	}
}

