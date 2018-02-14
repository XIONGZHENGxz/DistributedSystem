//UT-EID= adk882, mi4467

import java.util.*;
import java.util.concurrent.*;

public class PSort{

  public static void parallelSort(int[] A, int begin, int end){
	  RecursiveAction task = new ParallelSort(A, begin, end);
	  ForkJoinPool pool = new ForkJoinPool();
	  pool.invoke(task);
  }
  
  private static class ParallelSort extends RecursiveAction {
	private int[] A;
	int begin;
	int end;
	
	public ParallelSort(int[] a, int b, int e) {
		A = a;
		begin = b;
		end =e;
	}
	@Override
	protected void compute() {
	    if((end - begin) <16) {
	    	int temp;
	    	int j;
	    	for(int i = begin; i<end; i++) {			//normal insertion sort for arrays smaller than sixteen elements
	    		temp = A[i];
	    		for(j = i-1; j>=0 && A[j]>temp; j--) {
	    			A[j+1] = A[j];
	    		}
	    		A[j+1] = temp;
	    	}
	    }
	    else {
	    	//System.out.println("Number of active threads from the given thread: " + Thread.activeCount());
	    	int pivot = partition(A, begin, end);
	    	ParallelSort one = new ParallelSort(A, begin, pivot-1);
	    	ParallelSort two = new ParallelSort(A, pivot+1, end);
	    	invokeAll(one, two);
	    }	
	}
	
	private int partition(int[] A, int begin, int end) {
		int pivot = A[end]; 
        int i = begin-1; 
        int temp;
        for (int j=begin; j<end; j++) {
            if (A[j] <= pivot) {
            	i++;
                temp = A[i];
                A[i] = A[j];
                A[j] = temp;
            }
        }
        temp = A[i+1];
        A[i+1] = A[end];
        A[end] = temp;
        return i+1;
	}	  
  }
}
