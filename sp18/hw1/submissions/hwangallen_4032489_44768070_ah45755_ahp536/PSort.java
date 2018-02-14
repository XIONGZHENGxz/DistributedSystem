import java.util.*;
import java.util.concurrent.*;

public class PSort implements Callable<Void>{
	
	  public static ExecutorService threadPool = Executors.newCachedThreadPool();
	  int x, begin, end;
	  int[] A;
	  public PSort(int[] A, int begin, int end)
	  {
		this.A = A;
		this.begin = begin;
		this.end = end;
	  }

	  public static void parallelSort(int[] A, int begin, int end){
	    // TODO: Implement your parallel sort function 
		 threadPool = Executors.newCachedThreadPool();
		 PSort start = new PSort(A, begin, end);
		 Future<Void> sub = threadPool.submit(start);
		 try {
			 sub.get();
		 }
		 catch(Exception badthings)
		 {
			 System.err.println(badthings);
		 }
		 threadPool.shutdown();
	  }
	  public static int splitHelper(int[] A, int begin, int end)
	  {
		  int halfPoint = A[(begin+end)/2];
		  int beginSplit = begin;
		  int endSplit = end -1;
		  while(beginSplit <= endSplit)
		  {
			  while(A[beginSplit] < halfPoint) beginSplit++;
			  while(A[endSplit] > halfPoint) endSplit--;
			  if (beginSplit <= endSplit)
			  {
				  int swap = A[beginSplit];
				  A[beginSplit] = A[endSplit];
				  A[endSplit] = swap;
				  beginSplit ++;
				  endSplit --;
			  }
		  }
		  return beginSplit;
	  }

	@Override
	public Void call() throws Exception {
		try
		{
			int splitIndex = splitHelper(A, begin, end);
			if(A==null)
				return null;
			if(end - begin <= 16)
			{
				int beginSort = begin;
				int endSort = end -1;
				int value = 0;
				for(; beginSort <= endSort; beginSort++) {
					if((beginSort+1) == A.length) return null;
					value = A[beginSort+1];
					int index = beginSort;
					while(index >= begin -1 && A[index] > value) {
						A[index+1] = A[index];
					index = index -1;
					if (index == -1) break;
					}
				A[index+1] = value;
			}	
				  return null;
			}
			Future<Void> left = null;
			Future<Void> right = null;
			if(begin < splitIndex -1)
			{
				PSort leftSort = new PSort(A, begin, splitIndex);
				left = threadPool.submit(leftSort);
			}
			if(splitIndex < end)
			{
				PSort rightSort = new PSort(A, splitIndex, end);
				right = threadPool.submit(rightSort);				
			}
			try {
				left.get();
			}
			catch(Exception e) {
				int dummy = 1;
			}
			try {
				right.get();
			}
			catch(Exception e)
			{
				int dummy = 1;
			}
			return null;
			} catch(Exception e) {
				return null;
			}
	}
}