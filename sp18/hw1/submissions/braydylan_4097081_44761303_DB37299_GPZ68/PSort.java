//UT-EID= gpz68 and db37299


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<Void>{
	
	int[] array;
	int begin;
	int end;
	
	PSort(int[] a, int b, int e) {
		this.array = a;
		this.begin = b;
		this.end = e;
	}
	
	protected Void compute() {
		if ((end-begin) <= 16) {
			insertSort(this.array, begin, end);
		}
		
		else {
			int pivot = quickSort(this.array, begin, end);
			PSort p1 = new PSort(array, begin, pivot);
			p1.fork();
			PSort p2 = new PSort(array, pivot + 1, end);
			p2.compute();
			p1.join();
		}
		
		
		return null;
	}
	
	private static void insertSort(int[] array, int begin, int end) {
		for (int i = begin + 1; i < end; i++) {
			int j = i;
			while (j > begin && array[j] < array[j-1]) {
				int temp = array[j-1];
				array[j-1] = array[j];
				array[j] = temp;
				j--;
			}
		}
	}
	
	private static int quickSort(int[] array, int begin, int end) {
		int pivot = end - 1;
		int wall = begin;
		int current = begin;
		
		while (current < end) {
			if (array[current] < array[pivot]) {
				int temp = array[wall];
				array[wall] = array[current];
				array[current] = temp;
				wall++;
			}
			current++;
		}
		
		int temp = array[wall];
		array[wall] = array[pivot];
		array[pivot] = temp;
		
		return wall;
		
	}
	
	public static void parallelSort(int[] A, int begin, int end){
		int processors = Runtime.getRuntime().availableProcessors();
		PSort input = new PSort(A, begin, end);
		ForkJoinPool pool = new ForkJoinPool(processors);
		pool.invoke(input);
	}
}
