//UT-EID=kmb3534, sty223

import java.util.*;
import java.util.concurrent.*;

public class PMerge {
  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads)
  {
    ArrayList<Integer> pivots = new ArrayList<Integer> ();
    pivots.add (0);

    int stride = A.length / numThreads;
    if (stride == 0)
    {
      new Merger(A, B, C, 0, A.length, 0, B.length).run ();
      return;
    }

    int start = 0;
    for (int i=stride-1; i < A.length; i += stride)
    {
      pivots.add (bisect (B, A[i], start, B.length));
      start = pivots.get (pivots.size () - 1);
      if (start == 0 || start == B.length)
        break;
    }

    ArrayList<Thread> workers = new ArrayList<Thread> ();
    for (int i = 1; i < pivots.size (); i++)
    {
      Thread t = new Thread (new Merger (A, B, C, (i-1) * stride,
                                         i * stride, pivots.get (i-1),
                                         pivots.get (i)));
      workers.add (t);
      t.start ();
    }

    new Merger (A, B, C, (pivots.size () - 1) * stride, A.length,
                pivots.get (pivots.size () - 1), B.length).run ();

    try
    {
      for (Thread t : workers)
        t.join ();
    }
    catch (InterruptedException e)
    {}
  }

  public static int bisect (int[] array, int x, int start, int stop)
  {
    while (start < stop)
    {
        int mid = (start + stop) / 2;
        if (array[mid] < x)
          start = mid + 1;
        else
          stop = mid;
    }

    return start;
  }
}

class Merger implements Runnable
{
  int[] A;
  int[] B;
  int[] C;
  int A_start;
  int A_stop;
  int B_start;
  int B_stop;

  public Merger (int [] A, int [] B, int[] C, int A_start, int A_stop,
                 int B_start, int B_stop)
  {
    this.A = A;
    this.B = B;
    this.C = C;
    this.A_start = A_start;
    this.A_stop = A_stop;
    this.B_start = B_start;
    this.B_stop = B_stop;
  }

  public void run ()
  {
    int i = A_start, j = B_start;

    while (i<A_stop && j<B_stop)
    {
      if (A[i] < B[j])
        C[i+j] = A[i++];
      else
        C[i+j] = B[j++];
    }

    while (i<A_stop)
      C[i+j] = A[i++];

    while (j<B_stop)
      C[i+j] = B[j++];
  }
}
