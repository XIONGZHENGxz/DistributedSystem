//package hwk1;

//UT-EID= gbl286, chy253


import java.util.*;
import java.util.concurrent.*;

//Executive Service
//same n's 

public class PMerge implements Runnable{
  int lowerbound;
  int upperbound;
  int searchArray;
  boolean carry;
  static int[] returnArr;
  static int[] A;
  static int[] B;
  
  public PMerge(int[] arr, int lower, int high, int whichArr, int[] A, int[] B, boolean car) {
    this.lowerbound = lower;
    this.upperbound = high;
    this.searchArray = whichArr;
    this.carry = car;
    PMerge.returnArr = arr;
    PMerge.A = A;
    PMerge.B = B;
  }
  
  public void run() {
    //go through the specified array
    for(int index = this.lowerbound; !(this.carry == false && index > this.upperbound); index++) {
      //comparing to array A
      if(this.searchArray == 0) {
        int i=0;
        //go through array A until you get the number of elements less than B[index]
        i = binarySearch(B[index], A);
        PMerge.returnArr[i+index] = B[index]; 
      }
      //comparing to array B
      else {
        int i=0;
        i = binarySearch(A[index], B);
        PMerge.returnArr[i+index] = A[index]; 
        
        if(index == A.length - 1 && this.carry == true) {
          this.searchArray = 0;
          this.carry = false;
          index = -1;
        } 
      }
    }
  }
  
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
	ExecutorService threadPool = Executors.newCachedThreadPool();
	int elements = A.length+B.length;
    int threadNum = 1;
    int extra = 0;
    if (numThreads < elements) {
      threadNum = elements/numThreads;
      extra = elements%numThreads;
    }
    //PMerge[] threads = new PMerge[numThreads];
    for(int i = 0; i < numThreads && i < elements; i++) {
      //if it's the last element, it needs to consider the left overs
      if(i == numThreads - 1 | i == elements - 1) {
        if((i*threadNum) < A.length) {
          int upperboundInd = (((i+1)*threadNum)-1)+extra;
          if(upperboundInd >= A.length) {
            upperboundInd = upperboundInd - (A.length);
            threadPool.submit(new PMerge(C, (i*threadNum), upperboundInd, 1, A, B, true));
          }
          else {
            threadPool.submit(new PMerge(C, (i*threadNum), upperboundInd, 1, A, B, false));
          }
        }
        else {
          
          threadPool.submit(new PMerge(C, (i*threadNum)-A.length, (((i+1)*threadNum)-1-A.length)+extra, 0, A, B, false));
        }
      }
      else {
        if((i*threadNum) < A.length) {
          int upperboundInd = ((i+1)*threadNum)-1;
          if(upperboundInd >= A.length) {
            upperboundInd = (upperboundInd -A.length);
            threadPool.submit(new PMerge(C, (i*threadNum), upperboundInd, 1, A, B, true));
          } 
          else {
            threadPool.submit(new PMerge(C, (i*threadNum), ((i+1)*threadNum)-1, 1, A, B, false));
          }
        }
        else {
          threadPool.submit(new PMerge(C, (i*threadNum)-A.length, (((i+1)*threadNum)-1-A.length), 0, A, B, false));
        }
      }
      if(i == numThreads-1 | i == elements-1) {
    	  threadPool.shutdown();

    	  try {
    	      while(!threadPool.isTerminated()) {
    	    	  Thread.sleep(100);
    	      } 
    	  } catch (InterruptedException e) {
    		  threadPool.shutdownNow();
    	  }
      }
    }
    
  }
  
  
  public int binarySearch(int num, int[] arr) {
    int lo = 0;
    int hi = arr.length -1;
    int mid = (lo + hi)/2;
    while(lo <= hi) {
      mid = (lo + hi)/2;
      if(arr[mid] > num) {
        hi = mid-1;
      }
      if(arr[mid] < num) {
        lo = mid+1;
      }
      if(arr[mid] == num) {
    	while(mid < arr.length && arr[mid] == num) {
    		if(arr == PMerge.B) {
    			while(arr[mid] == num) {
    				mid--;
    				if(mid < 0) {
    					return 0;
    				}
    			}
    			return mid+1;
    		}
    		else {
        		mid++;
    		}
    	}
    	if(arr == PMerge.A) {
    		return mid;
    	}
        return mid-1;
      }
    }
    if(hi == -1) {
      return 0;
    }
    if(arr[mid] < num) {
      return mid+1;
    }
    else {
      return mid;
    }
  }
}
