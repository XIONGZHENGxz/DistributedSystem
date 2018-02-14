//UT-EID=es25725, szm99

import java.util.*;
import java.util.concurrent.*;

public class PMerge implements Runnable {

  private int[] current_array;
  private int[] other_array;
  private int[] C;
  private int index;
  private int offset;

  public PMerge(int[] a, int[] b, int[] c, int i, boolean f) {
    current_array = a;
    other_array = b;
    C = c;
    index = i;
    if (f) {
      offset = 1;
    } else {
      offset = 0;
    }
  }

  public void run() {
    if (other_array.length == 0) {
      C[index] = current_array[index];
    }
    int i = 0;
    while (i < other_array.length) {
      if (current_array[index] < other_array[i]) {
        C[index + i] = current_array[index];
        return;
      } else if (current_array[index] == other_array[i]) {
        C[index + i + offset] = current_array[index];
      }
      i++;
    }
    C[index + i] = current_array[index];
    return;
  }

  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
    ExecutorService e = Executors.newFixedThreadPool(numThreads);
    for (int i = 0; i < A.length; i++) {
      PMerge pm = new PMerge(A, B, C, i, true);
      e.submit(pm);
    }
    for (int i = 0; i < B.length; i++) {
      PMerge pm = new PMerge(B, A, C, i, false);
      e.submit(pm);
    }
    e.shutdown();
    try {
      e.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      System.out.println("Something went wrong :(");
    }
  }
}
