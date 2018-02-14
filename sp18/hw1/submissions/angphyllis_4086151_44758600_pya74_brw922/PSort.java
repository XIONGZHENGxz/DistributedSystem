//UT-EID=pya74, brw922


import java.util.*;
import java.util.concurrent.*;


public class PSort extends RecursiveTask<Integer>{
  final int array[];
  final int start;
  final int end;
  
  PSort(int[] array, int start, int end) {
    this.array = array;
    this.start = start;
    this.end = end;
    
    if (end-start <= 16) {
      insertionSort(array);
    } else {
      quickSort(array, start, end-1);
    }
  }
  
  public void insertionSort(int[] array) {
     for (int i = 1; i < array.length; i++) {
       int current = array[i];
       int j = i-1;
       
       while (j >= 0 && array[j] > current) {
         array[j+1] = array[j];
         j = j-1;
       }
       
       array[j+1] = current;
     }
  }
  
  public void quickSort(int[] array, int start, int end) {
    if (start < end) {
      int mid = partition(array, start, end);
      
      quickSort(array, start, mid-1);
      quickSort(array, mid+1, end);
    }
  }
  
  public int partition(int[] array, int start, int end) {
    int pivot = array[end];
    int i = (start-1);
    
    for (int j = start; j < end; j++) {
      if (array[j] <= pivot) {
        i++;
        
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
      }   
    }
    
    int temp = array[i+1];
    array[i+1] = array[end];
    array[end] = temp;
    
    return i+1;
  }
  
  public Integer compute() {
    if (end >= start) {
      return 1;
    }
    
    int mid = array.length/2;
    PSort p1 = new PSort(array, start, mid-1);
    p1.fork();
    PSort p2 = new PSort(array, mid+1, end);
    
    return p2.compute() + p1.join();
  }

  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function 
    int processors = Runtime.getRuntime().availableProcessors();
    System.out.println("Number of processors: " + processors);
    PSort p = new PSort(A, 0, A.length);
    ForkJoinPool pool = new ForkJoinPool(processors);
    pool.invoke(p);
  }
}
