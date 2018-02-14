//UT-EID=


import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PMerge{

  private static ExecutorService exec;

  private static int[] A_ref;
  private static int[] B_ref;

  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
    if(numThreads > C.length) {
      numThreads = C.length;
    }
    exec = Executors.newFixedThreadPool(numThreads);
    A_ref = A;
    B_ref = B;

    int numThreadsA = numThreads / 2;
    int numThreadsB = numThreads - numThreadsA;

    int[] A_subArrays = new int[numThreadsA];
    int[] B_subArrays = new int[numThreadsB];

    if(numThreads == 1) {
      A_subArrays = new int[1];
      B_subArrays = new int[1];
      numThreadsA = 1;
      numThreadsB = 1;
    }

//    System.out.printf("A length [%d] --- A # Threads [%d]\n", A.length, numThreadsA);
//    System.out.printf("B length [%d] --- A # Threads [%d]\n\n", B.length, numThreadsB);

//    System.out.println("A_Length/numThreadsB: " + Math.floor(((double)A.length / numThreadsA)));
//    System.out.println("B_Length/numThreadsB: " + Math.floor(((double)B.length / numThreadsB)));

    for(int i = 0; i < A_subArrays.length; i++) {
      if(Math.floor(((double)A.length / numThreadsA)) == 0) {
        A_subArrays[i] = (int) ((i ) * Math.ceil(((double)A.length / numThreadsA)));
      } else {
        A_subArrays[i] = (int) ((i ) * Math.floor(((double)A.length / numThreadsA)));
      }
    }

    for(int i = 0; i < B_subArrays.length; i++) {
      if(Math.floor(((double)B.length / numThreadsB)) == 0) {
        B_subArrays[i] = (int) ((i ) * Math.ceil(((double)B.length / numThreadsB)));
      } else {
        B_subArrays[i] = (int) ((i ) * Math.floor(((double)B.length / numThreadsB)));
      }
    }

//    System.out.println("A Sub-Arrays: " + Arrays.toString(A_subArrays));
//    System.out.println("B Sub-Arrays: " + Arrays.toString(B_subArrays));

    ArrayList<Future<Void>> a_futures = new ArrayList<>();
    ArrayList<Future<Void>> b_futures = new ArrayList<>();

    for(int i = 0; i < numThreadsA; i++) {
      int begin;
      int end;
      Future<Void> f;

      if(i == numThreadsA - 1) {
        begin = A_subArrays[i];
        end = A.length;
      } else if(i == 0) {
        begin = 0;
        end = A_subArrays[1];
      } else {
        begin = A_subArrays[i];
        end = A_subArrays[i + 1];
      }
//      System.out.printf("[A] Submitting task #%d: begin=[%d], end=[%d]\n", i + 1, begin, end);
      f = exec.submit(() -> {
        return mergeSubArray(begin, end, A, B, C);
      });
      a_futures.add(f);
    }

    for(int i = 0; i < numThreadsB; i++) {
      int begin;
      int end;
      Future<Void> f;

      if(i == numThreadsB - 1) {
        begin = B_subArrays[i];
        end = B.length;
      } else if(i == 0) {
        begin = 0;
        end = B_subArrays[1];
      } else {
        begin = B_subArrays[i ];
        end = B_subArrays[i + 1];
      }
//      System.out.printf("[B] Submitting task #%d: begin=[%d], end=[%d]\n", i + 1, begin, end);
      f = exec.submit(() -> {
        return mergeSubArray(begin, end, B, A, C);
      });
      b_futures.add(f);
    }
    try {
      for(int i = 0; i < a_futures.size(); i++) {
        a_futures.get(i).get();
      }
      for(int j = 0; j < b_futures.size(); j++) {
        b_futures.get(j).get();
      }
    } catch (Exception e) {
//      System.out.println(e);
    }

//    System.out.println("Merged Array C: " + Arrays.toString(C));
  }

  private static Void mergeSubArray(int begin, int end, int[] arr, int[] otherArr, int[] mergedArr) {
    for(int i = begin; i < end; i++) {
      int c_index = findIndex(arr[i], i, otherArr);
//      System.out.printf("Assigning value [%d] for index [%d]\n", arr[i], c_index);
      mergedArr[c_index] = arr[i];
    }
    return null;
  }

  private static int findIndex(int value, int index, int[] otherArray) {
    int count = 0;
    for(int i = 0; i < otherArray.length; i++) {
      if(otherArray[i] < value) {
        count++;
      } else if(otherArray[i] == value){
        if(otherArray == B_ref) {
          count++;
        }
      } else {
        break;
      }
    }

    return count + index;
  }
}
