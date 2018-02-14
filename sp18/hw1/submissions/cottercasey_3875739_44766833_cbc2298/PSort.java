//UT-EID=cbc2298


import java.util.*;
import java.util.concurrent.*;

public class PSort implements Runnable {
	
	public static ExecutorService es = Executors.newCachedThreadPool();
    private int[] A;
    private int start, finish;
    
    PSort(int a[], int begin, int end) {
		this.A = a;
		this.start = begin;
		this.finish = end;
	}
	
  /**
   * Parallel QuickSort 
   * @param A
   * @param begin
   * @param end
   */ 
  public static void parallelSort(int[] A, int begin, int end){
	  
	//if any are true, the inputs are invalid  
    if(begin >= end || A.length <= 0 || end - begin < 2)
    	return;
    
    //find the pivot value the portion of A you are sorting
    int pivot = A[begin + ((end-begin)/2)];
    
    if(end - begin == 2){
    	if(A[begin] > A[begin+1]){
    		flip(A,begin,begin+1);
    	}
    	return;
    }
    
    //if array size is less than or equal to 16, insertion sort
    if(end - begin <= 16){
    	int index = 0;
    	while(index < (end-begin)-1){
    		for(int i = index; i >= 0; i--){
    			if(A[i+1] < A[i]){
        			flip(A,i,i+1);
        		}
    		}
    		index++;
    	}
    	return;
    }
    //if not, recursive parallelSort
    else{
    	int temps = begin;
        int tempe = end-1;
        
        //find the partition
        while(temps <= tempe){
        	while(A[temps] < pivot){
        		temps++;
        	}
        	while(A[tempe] > pivot){
        		tempe--;

        	}
        	if(temps <= tempe){
        		flip(A,tempe,temps);
        		temps++;
        		tempe--;
        	}
        }
        
        //create PSort objects using partition and create a thread for each
        int partition = temps;
        PSort sortA = new PSort(A, begin, partition);
        PSort sortB = new PSort(A, partition, end);
        Thread threadA = new Thread(sortA);
        Thread threadB = new Thread(sortB);
        
        //start the threads
        threadA.start();
        threadB.start();
        
        try {
        	//join the threads using ForkJoinPool
        	threadA.join();
        	threadB.join();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
    }  
  }
  
  //flip A[first} and A[second]
  public static void flip(int A[], int first, int second) {
  	int temp = A[second];
  	A[second] = A[first];
  	A[first] = temp;
  }
  
  @Override
  public void run() {
	  parallelSort(A, start, finish);
  }
}
