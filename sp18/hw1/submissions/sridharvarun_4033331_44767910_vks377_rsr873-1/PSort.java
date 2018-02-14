//UT-EID= vks377, rsr873


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<int[]>{
	int[] array;
	int begin;
	int end;
	public PSort(int[] A, int begin, int end) {
		this.array = A;
		this.begin = begin;
		this.end = end;
	}
  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function 
	    int processors = Runtime.getRuntime().availableProcessors();
	    System.out.println("Number of processors: " + processors);
	    PSort p = new PSort(A, begin, end - 1);
	    int[] array;
	    if(A.length <= 16) 
	    	array = p.insertionSort();
	    else {
	    	ForkJoinPool pool = new ForkJoinPool(processors);
	    	array = pool.invoke(p);
	    }
	    System.out.println("Result: " + Arrays.toString(array));
  }
@Override
protected int[] compute() {
	// TODO Auto-generated method stub
	if(begin >= end)
		return array;
	
	int pivot = array[end];
	int wall = begin - 1;
	for(int i = begin; i <= end; i++) {
		if(array[i] < pivot) {
			wall++;
			swap(i, wall);
		}
	}
	wall++;
	swap(wall, end);
	
	PSort p1 = new PSort(array, begin, wall - 1);
	PSort p2 = new PSort(array, wall + 1, end);
	
	p1.fork();
	p2.compute();
	p1.join();
	return array;
}

private void swap(int i, int j) {
	int temp = array[i];
	array[i] = array[j];
	array[j] = temp;
}

private int[] insertionSort() {
	for(int i = 0; i < array.length; i++) {
		int currentIndex = i;
		int curr = array[i];
		for(int j = i - 1; j >= 0; j--) {
			if(array[j] > curr) {
				swap(currentIndex, j);
				currentIndex--;
			}
			else
				break;
		}
	}
	return array;
}
}
