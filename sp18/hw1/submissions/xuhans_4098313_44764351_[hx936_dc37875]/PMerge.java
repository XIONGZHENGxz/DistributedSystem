//UT-EID=


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    // TODO: Implement your parallel merge function
    ForkJoinPool pool = new ForkJoinPool(numThreads);
    MergeTask mainTask = new MergeTask(0, A.length, 0, B.length, 0, C.length, A, B, C);
    mainTask.fork();
    mainTask.join();
  }
  private static class MergeTask extends RecursiveAction
  {
    int aStart;
    int aEnd;
    int bStart;
    int bEnd;
    int cStart;
    int cEnd;
    int A[];
    int B[];
    int C[];
    public MergeTask(int as, int ae, int bs, int be, int cs, int ce, int[] one, int[] two, int[] three)
    {
      aStart = as;
      aEnd = ae;
      bStart = bs;
      bEnd = be;
      cStart = cs;
      cEnd = ce;
      A = one;
      B = two;
      C = three;
    }

      @Override
      protected void compute() {
        int sizeA = aEnd - aStart;
        int sizeB = bEnd - bStart;

        if(sizeA < sizeB)
        {
            int[] temp = A;
            A = B;
            B = temp;

            int tempVar = aStart;
            aStart = bStart;
            bStart = tempVar;

            tempVar = aEnd;
            aEnd = bEnd;
            bEnd = tempVar;

            tempVar = sizeA;
            sizeA = sizeB;
            sizeB = tempVar;
        }
        if(sizeA <= 0)
            return;

        int aNewEnd = (aStart + aEnd) / 2;
        int bNewEnd = binarySearch(A[aNewEnd]);
        int cNewEnd = cStart + (aNewEnd - aStart) + (bNewEnd - bStart);
        C[cNewEnd] = A[aNewEnd];

        MergeTask first = new MergeTask(aStart, aNewEnd, bStart, bNewEnd, cStart, cNewEnd, A, B, C);
        first.fork();
        MergeTask second = new MergeTask(aNewEnd + 1, aEnd, bNewEnd, bEnd, cNewEnd + 1, cEnd, A, B, C);
        second.fork();

        first.join();
        second.join();
      }

      private int binarySearch(int val) {
          int l = bStart, r = bEnd - 1;
          while (l <= r) {
              int m = l + (r - l) / 2;


              if (B[m] == val)
                  return m;


              if (B[m] < val)
                  l = m + 1;
              else
                  r = m - 1;
          }

          return l;
      }
  }
}
