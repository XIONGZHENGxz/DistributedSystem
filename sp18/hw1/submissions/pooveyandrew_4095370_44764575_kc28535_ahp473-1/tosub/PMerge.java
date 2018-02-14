//UT-EID=


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  public static void main(String[] args) {
    int[] a = {1, 3, 5};
    int[] b = {1, 2 ,4,6,7};
    int[] c =  new int[a.length +b.length];
    parallelMerge(a,b,c,4);
    System.out.println(Arrays.toString(c));
  }
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    ExecutorService  threadPoolExecutor  = Executors.newFixedThreadPool(numThreads);
    /* calculate the number of threads needed for both arrays*/
    int numA = numThreads / 2;
    int numB = numThreads / 2;
    if(numThreads % 2 != 0) { //odd number of threads
      if(A.length > B.length) {
        numA += 1;
      } else numB += 1;
    }
    int start = 0;
    int end = A.length / numA;
    if(end == 0) {
        end = 1;
        numA = A.length;
    }
    //check if one thread per index when end  == 0
    for(int k = 0; k < numA; k++) {
      Merger m;
      if(k + 1 == numA) {
        m = new Merger(start, A.length, A, B,C, true);
      }
      else m = new Merger(start, end, A, B, C, true);
      start = end;
      end += A.length / numA;
      threadPoolExecutor.submit(m);
    }
    start = 0;
    end = B.length / numB;
    if(end == 0) {
        end = 1;
        numB = B.length;
    }
    for(int k = 0; k < numB; k++) {
      Merger m;
      if(k + 1 == numA) {
        m = new Merger(start, B.length, B, A,C, false);
      }
      else m = new Merger(start, end, B, A, C, false);
      start = end;
      end += B.length / numB;
      threadPoolExecutor.submit(m);
    }
    threadPoolExecutor.shutdown();
  }
}
