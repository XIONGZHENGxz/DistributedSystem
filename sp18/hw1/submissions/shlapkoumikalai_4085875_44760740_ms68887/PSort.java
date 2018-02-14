//UT-EID=ms68887


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
	  int numProcessors = Runtime.getRuntime().availableProcessors();
	  QuickSort q1 = new QuickSort(A, begin, end - 1);
	  ForkJoinPool pool = new ForkJoinPool(numProcessors);
	  pool.invoke(q1);
  }
}

//don't need to return anything, so we can use RecursiveAction instead
class QuickSort extends RecursiveAction
{
	private final int[] n;
	private final int begin;
	private final int end;
	
	QuickSort(int[] n, int begin, int end) {
		this.n = n;
		this.begin = begin;
		this.end = end;
	}
	
	@Override
	protected void compute() {
		if(end - begin <= 16)
		{
			insertSort(n, begin, end + 1);
		}
		else
		{
			int middle = partition(n, begin, end);
			QuickSort leftEnd = new QuickSort(n, begin, middle - 1);
			QuickSort rightEnd = new QuickSort(n, middle + 1, end);
			leftEnd.fork();
			rightEnd.compute();
			leftEnd.join();
		}
	}
	
    private int partition(int arr[], int low, int high)
    {
        int pivot = arr[high]; 
        int i = (low - 1);
        
        for (int j = low; j < high; j++)
        {
            if (arr[j] <= pivot)
            {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
 
        return i + 1;
    }

	
	private static int[] insertSort(int[] input, int begin, int end)
	{
        int temp;
        for (int i = begin + 1; i < end; i++) {
            for(int j = i ; j > begin ; j--){
                if(input[j] < input[j-1]){
                    temp = input[j];
                    input[j] = input[j-1];
                    input[j-1] = temp;
                }
            }
        }
        return input;
	}
}
