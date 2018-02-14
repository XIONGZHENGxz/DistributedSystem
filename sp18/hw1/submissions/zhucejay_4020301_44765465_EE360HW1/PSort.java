//UT-EID=cz4723


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{
	
	private int[] A;
	private int begin, end;
	
	public PSort(int[] A, int begin, int end){
		this.A = A;
		this.begin = begin;
		this.end = end;
	}
	
	protected void compute(){
		if(A.length < 17){
			insertSort(A, begin, end);
		}
		else if(begin < end){
			int pivot = begin + ((end - begin)/2);
			pivot = partition(pivot);
			invokeAll(new PSort(A, begin, pivot-1), new PSort(A, pivot+1, end));
		}
	}
	
	private int partition(int pivot){
		int pivotValue = A[pivot];
		swap(pivot, end);
		int store = begin;
		for(int i = begin; i < end; i++){		//if any value is less, it needs to be left of store
			if((A[i] - pivotValue) < 0){		//store incremented for each value less
				swap(i, store);
				store++;
			}
		}
		swap(store, end);
		return store;
	}
	
	private void swap(int i, int j){
		if(i != j){
			int temp = A[i];
			A[i] = A[j];
			A[j] = temp;
		}
	}
	
	private void insertSort(int[] A, int begin, int end){
		for(int i = begin + 1; i < end + 1; i++){
			int compareValue = A[i];
			int j = i-1;
			while(j >= 0 && A[j] > compareValue){
				A[j+1] = A[j];
				j--;
			}
			A[j+1] = compareValue;
		}
	}
	
	public static void parallelSort(int[] A, int begin, int end){
		int processors = Runtime.getRuntime().availableProcessors();
		PSort p1 = new PSort(A, begin, end);
		ForkJoinPool pool = new ForkJoinPool(processors);
		pool.invoke(p1);
	}
	
	/*
	public static void main(String[] args){
		final int SIZE = 10000;
		int[] test = new int[SIZE];
		for(int i = 0; i < SIZE; i++){
			test[i] = (int)(Math.random()*1000);	//integer between 0 and 1000
		}
		int begin = 0;
		int end = SIZE - 1;
		parallelSort(test, begin, end);
		int prev = 0;
		boolean TestPass = true;
		for(int i = begin; i < end + 1; i++){
			if(test[i] < prev){
				TestPass = false;
			}
			prev = test[i];
		}
		System.out.println("TestPass is "+TestPass);
	}
	*/
	
}
