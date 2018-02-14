//UT-EID= sjs4367
//UT-EID= rps945


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  static ExecutorService threadPool;
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    threadPool = Executors.newFixedThreadPool(numThreads);

    ArrayList< Future<Integer> > th = new ArrayList< Future<Integer> >();
    Merge.A = A;
    Merge.B = B;
    Merge.C = C;
    for(int i = 0; i<A.length; i++)
    {
      th.add(threadPool.submit(new Merge(false,i,A[i])));
    }

    for(int i = 0; i<B.length; i++)
    {
      th.add(threadPool.submit(new Merge(true,i,B[i])));
    }
    for(Future<Integer> f : th)
    {
    	if(th != null)
      {
        try
        {
          f.get();
        }
        catch (Exception e) 
        { 
      	  //e.printStackTrace(); 
        }
      }
    }
    threadPool.shutdown();
  }

  static class Merge implements Callable<Integer>
  {
    static int[] A;
    static int[] B;
    static int[] C;
    boolean sw;
    int in;
    int num;

    public Merge(boolean s, int index, int n)
    {
      sw = s;
      in = index;
      num = n;
    }

    public Integer call()
    {
    	int neg;
      try
      {
        if(sw)
        {
          neg = Arrays.binarySearch(A,num);
          if(neg < 0)
          {
            in += -1 * neg - 1;
          }
          else
          {
            in += neg;
          }
        }
        else
        {
          neg = Arrays.binarySearch(B,num);
          if(neg < 0)
          {
            in += -1 * neg - 1;
          }
          else
          {
            in += neg + 1;
          }
        }
        C[in] = num;
        return 0;
      }
      catch(Exception e)
      {
        //e.printStackTrace();
        return 1;
      }
    }
  }
}
