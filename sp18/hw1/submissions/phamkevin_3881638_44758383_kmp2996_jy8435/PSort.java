package hw1;
//UT-EID = Kevin Pham (kmp2996)
//UT-EID = Jia-luen Yang (jy8435)

import java.util.concurrent.*;

public class PSort extends RecursiveAction {
	
  private int[] a;
  private int b;
  private int e;
  
  public PSort(int[] A, int begin, int end) {
	  this.a = A;
	  this.b = begin;
	  this.e = end;
  }
  
  protected void compute() {

      if (b < e)
      {

          int pi = partition(a, b, e);

          PSort ps1 = new PSort(a, b, pi - 1);
          ps1.fork();
    	      PSort ps2 = new PSort(a, pi + 1, e);
    	      ps2.compute();
    	      ps1.join();
      }
  }
  
  int partition(int arr[], int low, int high)
  {
      int pivot = arr[high]; 
      int i = (low-1); 
      for (int j=low; j<high; j++)
      {
 
          if (arr[j] <= pivot)
          {
              i++;
              int temp = arr[i];
              arr[i] = arr[j];
              arr[j] = temp;
          }
      }

      int temp = arr[i+1];
      arr[i+1] = arr[high];
      arr[high] = temp;

      return i+1;
  }
  
  public static void parallelSort(int[] A, int begin, int end) {
      
      //int processors = Runtime.getRuntime(). availableProcessors (); 
      PSort ps = new PSort (A, begin, end - 1); 
      ForkJoinPool pool = new ForkJoinPool();
      pool.invoke(ps);
  }
  

}