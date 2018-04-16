//UT-EID=wpp228


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction
{
	int[] A;
	int begin;
	int end;

	public PSort(int[] A, int begin, int end)
	{
		this.A = A;		
		this.begin = begin;
		this.end = end;
	}

	protected void compute()
	{
		if (begin - end <= 16)
		{	
			insertionSort(A, begin, end);
			return;
		}

		PSort p1 = new PSort(A, begin, end/2);	
		p1.fork();	
		PSort p2 = new PSort(A, end/2, end);
		p2.compute();
		p1.join();
		return;
	}

	public static void parallelSort(int[] A, int begin, int end)
	{
    		// TODO: Implement your parallel sort function 
		
		int processors = Runtime.getRuntime().availableProcessors();
		
		PSort p = new PSort(A, begin, end);
		ForkJoinPool pool = new ForkJoinPool(processors);

		pool.invoke(p);
	}

	public static void insertionSort(int[] A, int begin, int end)
	{
		/* n = index of the largest sorted element, n + 1 = index of the next unsorted element
		 * Compares next unsorted element against largest element of the sorted list
		 * If the [n + 1] > [n], then [n + 1] stays where it is, and it becomes the new value of n
		 * Otherwise, [n + 1] is pulled out of the array, and inserted into its proper place in the sorted array
		 */

		int pulledOutVal = 0;

		if((end - begin) <= 1)
		{
			return;
		}	

		//in inclusive, end exclusive
		for (int i = begin + 1; i < end; i++)
		{
			pulledOutVal = A[i];

			//Shift the unsorted element into a sorted position in the array
			for (int j = i; j > 0 && (A[j] < A[j - 1]); j--)
			{
				A[j] = A[j - 1];
				A[j - 1] = pulledOutVal;
			}
		}
			
	}


}
