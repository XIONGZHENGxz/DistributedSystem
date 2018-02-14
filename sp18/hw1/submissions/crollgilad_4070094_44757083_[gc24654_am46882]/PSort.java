//UT-EID= GC24654
//Names= Gilad Croll
//Andoni Mendoza
//UTIDs:
//gc24654
//AM46882


import java.util.*;
import java.util.concurrent.*;

//Java program for implementation of QuickSort
class PSort extends RecursiveAction{
	int[] A;
	int begin;
	int end;

	public PSort(int[] A, int begin, int end){
		this.A = A;
		this.begin = begin;
		this.end = end;
	}

	public static void parallelSort(int[] A, int begin, int end){
		PSort f = new PSort(A, begin, end-1);
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(f);
	}

	@Override
	protected void compute(){
		if (begin < end){
			if (end-begin <= 16)
				insertionSort(A, begin, end);
			else{
				int pi = partition(A, begin, end);
				invokeAll(new PSort(A, begin, pi-1), new PSort(A, pi+1, end));
			}
		}
	}

	private static int partition(int arr[], int begin, int end)
	{
		int pivot = arr[end]; 
		int i = (begin-1); // index of smaller element
		for (int j=begin; j<end; j++){
			if (arr[j] <= pivot){
				i++;
				int temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
			}
		}

		int tmp = arr[i+1];
		arr[i+1] = arr[end];
		arr[end] = tmp;

		return i+1;
	}

	/*Function to sort array using insertion sort*/
	static void insertionSort(int arr[], int start, int end){
		for (int i=start+1; i<end+1; ++i){
			int key = arr[i];
			int j = i-1;

			while (j>=0 && arr[j] > key){
				arr[j+1] = arr[j];
				j = j-1;
			}
			arr[j+1] = key;
		}
	}
}