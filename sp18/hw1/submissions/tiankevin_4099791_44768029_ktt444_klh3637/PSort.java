//UT-EID= ktt444, klh3637


import java.util.*;
import java.util.concurrent.*;

public class PSort{
	
	private final static ForkJoinPool pool = new ForkJoinPool(4);
	
	private static class PSortTask extends RecursiveAction{
		private final int[] A;
		private final int begin;
		private final int end;
		private static final int THRESHOLD = 16;
		
		protected PSortTask(int[] A, int begin, int end) {
			this.A = A;
			this.begin = begin;
			this.end = end;
		}
		
		@Override
		protected void compute() {
			if(end - begin <= THRESHOLD) {
				insertionSort(A, begin, end);
			}else {
				int middle = begin + ((end - begin)/2);
				invokeAll(new PSortTask(A, begin, middle), new PSortTask(A, middle, end));
				combine(middle);
			}
		}
		
		private void combine(int middle) {
			if(A[middle - 1] < A[middle]) return;
			
			int[] A2 = new int[end-begin];
			System.arraycopy(A, begin, A2, 0, A2.length);
			int begin2 = 0;
			int end2 = end - begin;
			int middle2 = middle - begin;
			
			for(int i = begin, p = begin2, q = middle2; i < end; i++) {
				if(q >= end2 || (p < middle2 && A2[p] < A2[q])) {
					A[i] = A2[p++];
				}else {
					A[i] = A2[q++];
				}
			}
		}
		
	}
	
	/* Function to sort an array using insertion sort*/
	static void insertionSort(int arr[], int begin, int end)
	{
	   int i, key, j;
	   for (i = begin; i < end; i++)
	   {
	       key = arr[i];
	       j = i-1;

	       while (j >= begin && arr[j] > key)
	       {
	           arr[j+1] = arr[j];
	           j = j-1;
	       }
	       arr[j+1] = key;
	   }
	}
	
	public static void parallelSort(int[] A, int begin, int end){
		ForkJoinTask<Void> job = pool.submit(new PSortTask(A, begin, end));
		job.join();
	}
}
