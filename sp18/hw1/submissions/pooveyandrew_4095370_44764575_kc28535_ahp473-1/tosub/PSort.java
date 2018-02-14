
//UT-EID=kc28535, ahp473

import java.util.*;
import java.util.concurrent.*;

public class PSort {
	public static void parallelSort(int[] A, int begin, int end) {
		// TODO: Implement your parallel sort function
	    int processors = Runtime.getRuntime().availableProcessors();
	    QSort q = new QSort(A, begin, end);
	    ForkJoinPool pool = new ForkJoinPool(processors);
	    pool.invoke(q);
	}
}
