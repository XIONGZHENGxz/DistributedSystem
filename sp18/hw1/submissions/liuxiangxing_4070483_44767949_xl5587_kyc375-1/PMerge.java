/*
 * EE 360P Homework 1
 * Professor Grag
 * Name: Xiangxing Liu
 * EID:  xl5587
 * Name: Kravis Cho
 * EID:  kyc375
 * Date: 02/05/2018
 */


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  private final static boolean DEBUG = false;
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    // TODO: Implement your parallel merge function

    if((A.length == 0) || (B.length == 0) || (C.length == 0)) {
      throw new IllegalArgumentException("Can't be empty\n");
    }

    if(numThreads <= 0)
      throw new IllegalArgumentException("Wrong numThreads\n");

    ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    List<Future<Map<Integer,Integer>>> threadList = new ArrayList<>();

    int acount = 0, bcount = 0;
    int total = A.length + B.length;
    if(total < numThreads){
        numThreads = total;
    }

    for(int i = 0; i < numThreads; i++){
      if(acount < A.length){
        Callable<Map<Integer,Integer>> callable = new PMergeTask(A[acount], B, acount);
        Future<Map<Integer,Integer>> future = pool.submit(callable);
        acount ++;
        threadList.add(future);
      }

      else {
        if(bcount < B.length){
          Callable<Map<Integer,Integer>> callable = new PMergeTask(B[bcount], A, bcount);
          Future<Map<Integer,Integer>> future = pool.submit(callable);
          bcount ++;
          threadList.add(future);
        }
      }
    }

    Map<Integer,Integer> result = null;

    for(int i = 0; i< numThreads; i++){
      try {
        result = threadList.get(i).get();

        if (result == null){
          break;
        }

        if(DEBUG){System.out.println(result.toString());}

        Set<Integer> temp = result.keySet();
        int tempIndex = -1;

        for(int temp1:temp)
          tempIndex = temp1;

        C[tempIndex] = result.get(tempIndex);

      } catch (InterruptedException | ExecutionException e) {
        System.out.println("ERROR");
        e.printStackTrace();
      }
    }

    PMergeTask.clearSet();

    pool.shutdown();
  }
}
