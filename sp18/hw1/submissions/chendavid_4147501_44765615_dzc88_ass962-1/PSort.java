package ee360p_hw1;

//UT-EID=dzc88 and ass962

import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveTask<Void>{
  final int[] A;
  final int begin;
  final int end;
  PSort(int[] A, int begin, int end){
      this.A = A;
      this.begin = begin;
      this.end = end;
  }
  int partition(){
      int pivot = A[end];
      int i = (begin-1);
      for(int j=begin; j<end; j++){

          if(A[j] <= pivot) {
              i++;

              //swap
              int temp = A[i];
              A[i] = A[j];
              A[j] = temp;
          }
      }
      int temp = A[i+1];
      A[i+1] = A[end];
      A[end] = temp;

      return i+1;
  }
  protected Void compute(){
      if(end <= 15){
          int length = end+1;
          for(int i = 1; i < length; i++){
              int key = A[i];
              int j = i - 1;

              while(j >= 0 && A[j] > key){
                  A[j+1] = A[j];
                  j = j-1;
              }
              A[j+1] = key;
          }
      }else if(begin < end){
          int sortedIndex = partition();

          PSort p1 = new PSort(A, begin, sortedIndex-1);
          PSort p2 = new PSort(A, sortedIndex, end);

          p1.fork();
          p2.compute();
          p1.join();
      }
      return null;
  }
  public static void parallelSort(int[] A, int begin, int end){
      // TODO: Implement your parallel sort function
      int processors = Runtime.getRuntime().availableProcessors();
      PSort p = new PSort(A, begin, end-1);
      ForkJoinPool pool = new ForkJoinPool(processors);

      pool.invoke(p);
      System.out.println(Arrays.toString(A));

  }
}