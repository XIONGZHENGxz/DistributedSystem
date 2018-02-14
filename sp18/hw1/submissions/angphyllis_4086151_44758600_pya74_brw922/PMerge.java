//UT-EID= pya74, brw922


import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Callable<Integer>{
  
  private int[] otherarr;
  private int[] finalarr;
  private int num;
  private int index;
  private static ExecutorService threadpool;
  
  public PMerge(int[] otherarr, int[] finalarr, int num, int index){
    /*this.A = A;
    this.B = B;
    this.C = C;*/
    //this.initialarr = initialarr;
    this.otherarr = otherarr;
    this.finalarr = finalarr;
    this.num = num;
    this.index = index;
  }
  
  @Override
  public Integer call() throws Exception {
    try{
      int otherIndex = binarySearch(otherarr, num);
      int finalIndex = otherIndex+index;
//      int dupIndex = findDuplicate(finalarr, num);
//      if(dupIndex != -1 && num != 0){
//        finalIndex = dupIndex + 1;
//      }
//      finalarr[finalIndex]  = num;
      
      //looks at finalarr index, if duplicate, store in next location 
      if(finalarr[finalIndex] == num && num != 0){
        finalarr[finalIndex+1] = num; 
      }
      else{
        finalarr[finalIndex] = num;
      }
      return null;
    }catch(Exception e){
      System.err.println (e);
      return 1;
    }
  }
  
//  private int findDuplicate(int[] arr, int num){
//    for(int i = arr.length - 1; i>=0; i--){
//      if(arr[i] == num){
//        return i;
//      }
//    }
//    return -1;
//  }
  
  /*
   * Precondition: arr has to be sorted
   * Looks through the arr for num 
   * If found, returns index in arr
   * If not found, returns index where it num should go 
   */
  private int binarySearch(int[] arr, int num){
    int low = 0; 
    int high = arr.length-1;
    
    while(low <= high){
      int middle = (low+high)/2;
      
      if(arr[middle] == num){
        return middle;
      }
      
      else if(arr[middle] < num){
        low = middle+1;
      }
      
      else if(arr[middle] > num){
        high = middle-1;
      }
    }
    return low;
  }
  
  

	public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    // TODO: Implement your parallel merge function
	  try{
  	  threadpool = Executors.newFixedThreadPool(numThreads);
  	  for(int i=0; i<A.length; i++){
  	    PMerge p1 = new PMerge(B, C, A[i], i);
  	    threadpool.submit(p1);
  	  }
  	  for(int i=0; i<B.length; i++){
  	    PMerge p2 = new PMerge(A, C, B[i], i);
        threadpool.submit(p2);
  	  }
  	  threadpool.shutdown(); 
  	  while(!threadpool.isTerminated()){}
	  }catch(Exception e) { System.err.println(e);}
  }
}


