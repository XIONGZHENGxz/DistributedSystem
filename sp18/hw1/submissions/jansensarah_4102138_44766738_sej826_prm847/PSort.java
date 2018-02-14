package homework1;
//UT-EID=


import java.util.*;
import java.util.concurrent.*;

public class PSort{
	
  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function
	  int processors = Runtime.getRuntime().availableProcessors();
	  ParallelSortClass p = new PSort().new ParallelSortClass(A, begin, end);
	  ForkJoinPool pool = new ForkJoinPool(processors);
	  int[] result = pool.invoke(p);
  }
  
  class ParallelSortClass extends RecursiveTask<int[]>{
	  
	  private int[] A;
	  private int begin, end;
	  ParallelSortClass(int[] A, int begin, int end){
		  this.A = A;
		  this.begin = begin;
		  this.end = end;
	  }
	  @Override
	  protected int[] compute() {
		  if(end - begin <= 16){
			  insertionSort(A, begin, end);
		  }
		  
		  else{
			  int middle = (begin + end)/2;
			  ParallelSortClass p1 = new ParallelSortClass(A, begin, middle);
			  p1.fork();
			  ParallelSortClass p2 = new ParallelSortClass(A, middle, end);
			  p2.compute();
			  p1.join();
			  merge(A, begin, middle, end);
		  }
		  return A;
	  }
  
	  private void insertionSort(int[] A, int begin, int end){
		  
		  int i = begin;
		  
		  while(i < end){
			
			  int j = i;
			  
			  while(j > begin && A[j-1] > A[j]){
				
				  int temp = A[j-1];
				  A[j-1] = A[j];
				  A[j] = temp;
				  
				  j--;
			  }
			  
			  i++;
			  
		  }
		  
	  }
	  
	  private void merge(int [] A, int begin, int middle, int end){
		  
		// TODO: Implement merge
		  int [] ACopy = (int [])A.clone();
		  
		  int firstIndex = begin;
		  int secondIndex = middle;
		  
		  for(int i = begin; i < end; i++){
			  
			  if(secondIndex >= end||(firstIndex < middle && ACopy[firstIndex] <= ACopy[secondIndex])){
				  A[i] = ACopy[firstIndex];
				  firstIndex++;
			  }
			  
			  else{
				A[i] = ACopy[secondIndex];
				secondIndex++;
			  }
			  
		  }
		  
	  }

	
  }
  
}
