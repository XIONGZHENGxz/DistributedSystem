
//UT-EID= cfd363, csf596

import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<ArrayList<Integer>>{
	
	 final ArrayList<Integer> array; //sub array for each PSort instance
	
	/* 
	 * PSort constructor that initializes sub-array
	 * for this recursive sub-task.
	 */
	PSort(int[] A, int b, int e){
		this.array = new ArrayList<Integer>();
		for (int i = b; i < e; i++) { //convert to ArrayList 
			this.array.add(A[i]);
		}
	}
	
	/* 
	 * PSort constructor that initializes sub-array
	 * for this recursive sub-task.
	 */
	PSort(ArrayList<Integer> A, int b, int e){
		this.array = new ArrayList<Integer>();
		for (int i = b; i < e; i++) { //copy array
			array.add(A.get(i));
		}
	}
	
	/*
	 * Sorts an integer array in parallel using quick sort to divide the 
	 * problem into smaller sub-problems. Once the sub-arrays have size <= 16,
	 * they are sorted with insert sort.
	 */
	public static void parallelSort(int[] A, int begin, int end) {
	    int processors = Runtime.getRuntime().availableProcessors();
	    PSort ps = new PSort(A, begin, end); //Initialize root RecursiveTask
	    ForkJoinPool pool = new ForkJoinPool(processors);

	    ArrayList<Integer> result = pool.invoke(ps); //Run recursive tasks

	    for (int i = begin; i < end; i++) { //Extract swapped result segment to original array
	    	A[i] = result.get(i-begin);
	    }
	}

	/*
	 * A simple insertion sort for when the arrays get <= 16 length
	 */
	protected ArrayList<Integer> insertSort(){
		if (array.size() <= 1) {
			return array;
		}
		for (int i = 1; i < array.size(); i++){
			for (int j = i; j > 0; j--){
				if (array.get(j) < array.get(j - 1)){ //if a previous element is smaller
					//swap
					int temp = array.get(j - 1);
					array.set(j - 1, array.get(j));
					array.set(j, temp);
				}
			}
		}
		return array;
	}
	
	protected ArrayList<Integer> compute() {
		if (array.size() <= 16) { //sequential insert sort for small arrays			
			return insertSort(); //this is always being called?
		}
		
		/* PARTITION */
		int pivotValue = array.get(array.size()-1); //pick last element as pivot and get its value
		int pointer = 0;
		for (int i = 0; i < array.size()-1; i++){
			if (array.get(i) <= pivotValue) {
				//swap
				int temp = array.get(i);
				array.set(i, array.get(pointer));
				array.set(pointer, temp);
				pointer++;
			}
		}
		//swap pivot and pointer index
		array.set(array.size()-1, array.get(pointer));
		array.set(pointer, pivotValue);
		
		/* RECURSIVELY SOLVE TWO SUB-PROBLEMS */
		PSort highTask = new PSort(array, pointer+1, array.size());
		PSort lowTask = new PSort(array, 0, pointer);
		lowTask.fork(); //split off and do work
		ArrayList<Integer> highResult = highTask.compute();
		ArrayList<Integer> lowResult = lowTask.join(); //resync
		
		/* MERGE FINAL SOLUTION */
		lowResult.add(pivotValue);
		lowResult.addAll(highResult);
		return lowResult;
	}
}
