//UT-EID= en5292 cwl648


import java.util.*;
import java.util.concurrent.*;


public class PMerge extends RecursiveAction{

  int[] arr1, arr2, arr3;
  int start1, start2, start3, end1, end2, end3;

  public PMerge(int[] arr1, int start1, int end1,
                int[] arr2, int start2, int end2,
                int[] arr3, int start3, int end3) {
    this.arr1 = arr1;
    this.arr2 = arr2;
    this.arr3 = arr3;
    this.start1 = start1;
    this.start2 = start2;
    this.start3 = start3;
    this.end1 = end1;
    this.end2 = end2;
    this.end3 = end3;
  }

  public void compute() {
    int len1 = end1 - start1;
    int len2 = end2 - start2;
    int mid = 0;
    int pivot = 0;
    int idx = 0;
    PMerge t1;
    PMerge t2;

    assert(len1 >= 0);
    assert(len2 >= 0);

    if (len1 == 0 && len2 == 0) {
      return;
    }

    if (len2 < len1) {
      mid = (start1 + end1) / 2;
      pivot = Arrays.binarySearch(arr2, arr1[mid]);
      if (pivot >= 0) {
        // case if there is a duplicate in the other array
        idx = start3 + (mid - start1) + (pivot - start2);
        arr3[idx] = arr1[mid];
        arr3[idx + 1] = arr2[pivot];
        t1 = new PMerge(arr1, start1, mid,
                arr2, start2, pivot,
                arr3, start3, idx);
        t2 = new PMerge(arr1, mid + 1, end1,
                arr2, pivot + 1, end2,
                arr3, idx + 2, end3);

      }
      else {
        // case if no duplicates occur
        pivot = (pivot + 1) * -1;
        idx = start3 + (mid - start1) + (pivot - start2);
        arr3[idx] = arr1[mid];
        t1 = new PMerge(arr1, start1, mid,
                arr2, start2, pivot,
                arr3, start3, idx);
        t2 = new PMerge(arr1, mid + 1, end1,
                arr2, pivot, end2,
                arr3, idx + 1, end3);
      }
    } else {
      mid = (start2 + end2) / 2;
      pivot = Arrays.binarySearch(arr1, arr2[mid]);
      if (pivot >= 0) {
        // duplicate case
        idx = start3 + (mid - start2) + (pivot - start1);
        arr3[idx] = arr2[mid];
        arr3[idx + 1] = arr1[pivot];
        t1 = new PMerge(arr1, start1, pivot,
                arr2, start2, mid,
                arr3, start3, idx);
        t2 = new PMerge(arr1, pivot + 1, end1,
                arr2, mid + 1, end2,
                arr3, idx + 2, end3);
      }
      else {
        // non duplicate case
        pivot = (pivot + 1) * -1;
        idx = start3 + (mid - start2) + (pivot - start1);
        arr3[idx] = arr2[mid];
        t1 = new PMerge(arr1, start1, pivot,
                arr2, start2, mid,
                arr3, start3, idx);
        t2 = new PMerge(arr1, pivot, end1,
                arr2, mid + 1, end2,
                arr3, idx + 1, end3);
      }
    }

    t1.fork();
    t2.compute();
    t1.join();
  }

  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
    try {
      // call join pool with the specified number of threads
      ForkJoinPool pool = new ForkJoinPool(numThreads);
      PMerge pm = new PMerge(A, 0, A.length,
                             B, 0, B.length,
                             C, 0, C.length);
      pool.invoke(pm);
      pool.shutdown();
    } catch (Exception e) {
      System.err.println (e);
    }
  }

  // public static void main(String[] args) {
  //   int[] test1 = new int[10000000];
  //   int[] test2 = new int[10000000];

  //   for (int i = 0; i < test1.length; i++) {
  //     test1[i] = i*2;
  //     test2[i] = i*2 + 1;
  //   }

  //   int[] result = new int[test1.length + test2.length];

  //   parallelMerge(test1, test2, result, 4);
  //   System.out.println("***** RUNNING TEST 1 ******");
  //   for (int i = 0; i < result.length; i++) {
  //     if (result[i] != i) {
  //       System.out.println("FAILURE!!!!!!!!!!");
  //     }
  //   }

  //   int[] test3 = {1, 3, 4, 6, 7, 8};
  //   int[] test4 = {2, 5, 9, 10};
  //   int[] result2 = new int[test3.length + test4.length];
  //   int[] solution = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

  //   parallelMerge(test3, test4, result2, 3);
  //   System.out.println("***** RUNNING TEST 2 ******");
  //   for (int i = 0; i < result2.length; i++) {
  //     if (result2[i] != solution[i]) {
  //       System.out.println("FAILURE!!!!!!!!!!");
  //     }
  //   }

  //   int[] test5 = {2, 4, 6, 8, 10};
  //   int[] test6 = {};
  //   int[] result3 = new int[test5.length + test6.length];
  //   parallelMerge(test5, test6, result3, 5);
  //   System.out.println("***** RUNNING TEST 3 ******");
  //   for (int i = 0; i < result3.length; i++) {
  //     if (result3[i] != test5[i]) {
  //       System.out.println("FAILURE!!!!!!!!!!");
  //     }
  //   }

  //   int[] test7 = {1, 2, 3, 4};
  //   int[] test8 = {1, 2, 5, 6};
  //   int[] result4 = new int[test7.length + test8.length];
  //   int[] solution2 = {1, 1, 2, 2, 3, 4, 5, 6};
  //   parallelMerge(test7, test8, result4, 1);
  //   System.out.println("***** RUNNING TEST 4 ******");
  //   for (int i = 0; i < result4.length; i++) {
  //     if (result4[i] != solution2[i]) {
  //       System.out.println("FAILURE!!!!!!!!!!");
  //     }
  //   }
  // }
}
