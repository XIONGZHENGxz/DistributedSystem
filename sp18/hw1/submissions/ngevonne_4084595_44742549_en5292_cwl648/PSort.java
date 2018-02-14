//UT-EID= en5292 cwl648


import java.util.*;
import java.util.concurrent.*;

public class PSort extends RecursiveAction{

  int[] arr;
  int left;
  int right;

  public PSort(int[] arr, int left, int right) {
    this.arr = arr;
    this.left = left;
    this.right = right;
  }

  // method to partition array according to pivot
  public int partition() {
    int i = left - 1;
    int pivot = arr[right];
    for (int j = left; j < right; j++) {
      if (arr[j] < pivot) {
        i++;
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
      }
    }
    int temp = arr[right];
    arr[right] = arr[i+1];
    arr[i+1] = temp;
    return i+1;
  }

  // method to do sequential insertion sort if item is 16 or less
  public void insertionSort() {
    for (int i = left; i <= right; i++) {
      int temp = arr[i];
      int j = i-1;
      while (j >= left && arr[j] > temp) {
        arr[j+1] = arr[j];
        j = j-1;
      }
      arr[j+1] = temp;
    }
  }

  protected void compute() {
    if (left < right) {
      if (right - left + 1 <= 16) {
        insertionSort();
      } else {
          int pivotIndex = partition();
          PSort t1 = new PSort(arr, left, pivotIndex - 1);
          PSort t2 = new PSort(arr, pivotIndex + 1, right);
          t1.fork();
          t2.compute();
          t1.join();
      }
    }
  }

  public static void parallelSort(int[] A, int begin, int end){
    ForkJoinPool pool = new ForkJoinPool();
    PSort ps = new PSort(A, begin, end-1);
    pool.invoke(ps);
    pool.shutdown();
  }

  // public static void main(String[] args) {
  //   int[] data1 = new int[10000];
  //   Random rand = new Random();
  //   for (int i = 0; i < data1.length; i++) {
  //     int randomNum = rand.nextInt((10000) + 1);
  //     data1[i] = randomNum;
  //   }
  //   parallelSort(data1, 0, 10000);
  //   System.out.println("***** RUNNING TEST 1 ******");
  //   for (int i = 1; i < data1.length; i++) {
  //     if (data1[i-1] > data1[i]) {
  //       System.out.println("FAILURE!!!!!!!!!!!!");
  //     }
  //   }

  //   int[] data2 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
  //   parallelSort(data2, 0, 10);
  //   System.out.println("***** RUNNING TEST 2 ******");
  //   for (int i = 1; i < data2.length; i++) {
  //     if (data2[i-1] > data2[i]) {
  //       System.out.println("FAILURE!!!!!!!!!!!!");
  //     }
  //   }

  //   int[] data3 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
  //   parallelSort(data3, 2, 8);
  //   System.out.println("***** RUNNING TEST 3 ******");
  //   int[] sol = {10, 9, 3, 4, 5, 6, 7, 8, 2, 1};
  //   for (int i = 0; i < data3.length; i++) {
  //     if (data3[i] != sol[i]) {
  //       System.out.println("FAILURE!!!!!!!!!!");
  //     }
  //   }
  // }
}
