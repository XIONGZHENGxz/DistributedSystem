//UT-EID = msj696


import java.util.*;
import java.util.concurrent.*;

public class PSort
{
	public static class concurrentSort extends RecursiveAction
	{
		private int[] A;
		private int begin, end;
		
		public concurrentSort(int[] A, int begin, int end)
		{
			this.A = A;
			this.begin = begin;
			this.end = end;
		}
		
		private void insertionSort(int[] A, int begin, int end)
		{
	        int i, j, temp;
	        for (i = begin+1; i< end; i++) 
	        {
	            j = i;
	            temp = A[i];    
	            while (j > 0 && temp < A[j-1])
	            {
	                A[j] = A[j-1];
	                j = j-1;
	            }
	            A[j] = temp;            
	        }        
		}
		
		private void swap(int[] A, int one, int two)
		{
			int swap = A[one];
	        A[one] = A[two];
	        A[two] = swap;
		}
		
		private int partition(int[] A, int begin, int end)
		{
			int pivot = A[end-1];
			int i = begin, j;

			for(j=begin; j<end-1; j++)
			{
				if(A[j] < pivot)
				{
					swap(A, i, j);
					i++;
				}
			}
			swap(A, i, end-1);
			return i;
		}

		@Override
		protected void compute()
		{
			if(begin == end)
				return;
			
			if((end-begin) <= 16)
			{
				insertionSort(A, begin, end);
				return;
			}
			else
			{
				int pivot = partition(A, begin, end);
				concurrentSort sub1 = new concurrentSort(A, begin, pivot);
				concurrentSort sub2 = new concurrentSort(A, pivot+1, end);
				sub1.fork();
				sub2.compute();
				sub1.join();
			}
		}
		
	}
	
	public static void parallelSort(int[] A, int begin, int end)
	{
		ForkJoinPool pool = new ForkJoinPool();
		concurrentSort sort = new concurrentSort(A, begin, end);
		pool.invoke(sort);
	}
}

