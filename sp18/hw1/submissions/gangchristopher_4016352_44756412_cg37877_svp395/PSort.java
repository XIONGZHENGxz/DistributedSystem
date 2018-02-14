//UT-EID=cg37877, svp395

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.*;

public class PSort {
  public static void parallelSort(int[] A, int begin, int end){
	  ForkJoinPool forkjoinpool = new ForkJoinPool();
	  PSort psort = new PSort();
	  PSort.quickSort quicksort = psort.new quickSort(A, begin, end);
	  forkjoinpool.invoke(quicksort);
  }
  
  public class quickSort extends RecursiveAction {
	  int[] A;
	  int begin, end;
	  
	  public quickSort(int[] A, int begin, int end) {
		  this.A = A;
		  this.begin = begin;
		  this.end = end;
	  }
	  
	  public int partition(int[] A, int begin, int end) {
		  int pivot = end - 1;
		  int front = begin;
		  while (front != pivot) {
			  if (A[pivot] > A[front]) {
				  front++;
			  } else if (A[pivot] < A[front]) {
				  // Swap front and pivot - 1
				  int temp = A[front];
				  A[front] = A[pivot - 1];
				  A[pivot - 1] = temp;
				  
				  // Swap pivot and pivot - 1
				  temp = A[pivot - 1];
				  A[pivot - 1] = A[pivot];
				  A[pivot] = temp;
				  pivot--;
			  }
		  }
		  return pivot;
	  }
	  
	  public void insertionSort(int[] A){
		  int temp;
	      for (int i = 1; i < A.length; i++) {
	    	  for(int j = i ; j > 0 ; j--){
	    		  if(A[j] < A[j-1]){
	    			  temp = A[j];
	    			  A[j] = A[j-1];
	                  A[j-1] = temp;
	              }
	          }
	      }
	  }
	  
	  @Override
	  protected void compute() {
		  if (A.length <= 16) {
			  // Insertion sort
			  insertionSort(A);
		  } else {
			  int pivot = partition(A, begin, end);
			  quickSort t1 = new quickSort(A, begin, pivot - 1);
			  quickSort t2 = new quickSort(A, pivot + 1, end);
			  t1.fork();
			  t2.compute();
			  t1.join();
		  }
	  }
	  
  }


}

