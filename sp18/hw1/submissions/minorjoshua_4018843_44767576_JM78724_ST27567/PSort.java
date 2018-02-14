import java.util.concurrent.*;
import java.util.Arrays;

public class PSort{

  public static void parallelSort(int[] A, int begin, int end){
    ForkJoinPool pool = new ForkJoinPool();
    RecursiveSort r = new RecursiveSort(A, begin, end - 1);
    pool.invoke(r);
  }

}

class RecursiveSort extends RecursiveTask<Void> {

  int[] A;
  int begin;
  int end;
  int length;
  int pivot_value;

  RecursiveSort(int[] A, int begin, int end) {
    this.A = A;
    this.begin = begin;
    this.end = end;
    this.length = end - begin + 1;
    this.pivot_value = this.A[(begin + end) / 2];
  }
  
  void insertionSort() {
      for(int i = begin + 1; i < end + 1; i++) {
    	  insert(i - 1, A[i]);
      }
  }
  
  // assumes A[rightIndex] is end of sorted portion
  void insert(int rightIndex, int value) {
	  int index = 0;
	  // find index to insert into
	  while(value > A[index]) {
		  index++;
	  }
	  
	  // shift all elements in sorted array over 1 to make room
	  // NOTE: Overwrites index where "value" was stored
	  for(int i = rightIndex + 1; i > index; i--) {
		  A[i] = A[i - 1];
	  }
	  
	  // insert desired value into cleared space
	  A[index] = value;
  }

  protected Void compute() {	
    if(length > 1) {
      if (length <= 16) {
    	insertionSort();
      } 
      else {
	      int index = partition();
	      RecursiveSort r1 = new RecursiveSort(A, begin, index - 1);
	      r1.fork();
	      RecursiveSort r2 = new RecursiveSort(A, index, end);
	      r2.compute();
	      r1.join();
      }
    }
    return null;
  }

  int partition() {
    int left = begin;
    int right = end;
    while(left <= right) {
    	//find value which doesn't belong on left
    	while (A[left] < pivot_value) {
    		left++;
    	}
    	//find value which doesn't belong on right
    	while (A[right] > pivot_value) {
    		right--;
  	    }
    	if (left <= right) {
    		swap(left, right);
    		left++;
    		right--;
    	}
    }
    return left;
  }
  
  void swap(int index1, int index2) {
	  int temp = A[index1];
	  A[index1] = A[index2];
	  A[index2] = temp;
  }
  
  public static void printArray(int[] A, int begin, int end) {
    for (int i = begin; i <= end; i++) {
      if (i != end) {
        System.out.print(A[i] + " ");
      } else {
        System.out.print(A[i]);
      }
    }
    System.out.println();
  }
}