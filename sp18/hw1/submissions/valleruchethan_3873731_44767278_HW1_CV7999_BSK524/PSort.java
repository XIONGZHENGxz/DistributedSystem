//UT-EID= bsk524
//        cv7999


import java.util.*;
import java.util.concurrent.*;

public class PSort {
  public static void parallelSort(int[] A, int begin, int end){
	  quickSort QS = new quickSort(A, begin, (end - 1));
	  ForkJoinPool threadPool = new ForkJoinPool();
	  threadPool.invoke(QS);
  }
}



