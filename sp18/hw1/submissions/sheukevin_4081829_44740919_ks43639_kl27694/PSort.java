//UT-EID= ks43639


import java.util.*;
import java.util.concurrent.*;

public class PSort{
	public static void parallelSort(int[] A, int begin, int end){
		ForkJoinPool commonPool = ForkJoinPool.commonPool();
		
		commonPool.invoke(new PSortRecursiveAction(A, begin, end - 1));

	}
}