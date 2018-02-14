/* Brendan Ngo bdn378
 * Neil Charles nc23228
 * */
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class PSort {
	
	int[] arr;
	
	public static void parallelSort(int[] A, int begin, int end) {
		// your implementation goes here.
		QuickSort qs = new QuickSort(A, begin, end-1);
		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		pool.invoke(qs);
		pool.shutdown();
		while(!pool.isTerminated());
		
	}

}


class QuickSort extends RecursiveAction {
	int begin, end;
	int[] arr;
	QuickSort(int[] arr, int begin, int end) {
		this.arr = arr;
		this.begin = begin;
		this.end = end;
	}
	@Override
	protected void compute() {
		// TODO Auto-generated method stub
		if ((end - begin) <= 16) {
			insertionSort(arr, begin, end);
		}
		else {
			int p = partition(arr, begin, end);
			
			QuickSort qs1 = new QuickSort(arr, begin, p-1);
			qs1.fork();
			QuickSort qs2 = new QuickSort(arr, p+1, end);
			qs2.compute();

//			qs1.join();
//			qs2.join();
			
		}
	}
	
	private static int partition(int[] arr, int begin, int end) { 
		int pivot = arr[end];
		int i = begin-1;
		for(int j = begin; j <= end-1; j++) {
			if(arr[j] <= pivot) {
				i++;
				int temp = arr[j];
				arr[j] = arr[i];
				arr[i] = temp;
			}
		}
		
		int temp = arr[i+1];
		arr[i+1] = arr[end];
		arr[end] = temp;
		return i+1;
	}
	
	private static void insertionSort(int[] arr, int begin, int end) {
		for(int i = begin+1; i <= end; i++) {
			int j = i-1;
			int value = arr[i];
			while(j >= 0 && arr[j] > value){
				arr[j+1] = arr[j];
				j--;
			}
			arr[j+1] = value;
		}
	}
	
}
