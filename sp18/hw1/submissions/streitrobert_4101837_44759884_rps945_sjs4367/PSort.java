//UT-EID= sjs4367
//UT-EID= rps945


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
    ForkJoinPool threadPool = new ForkJoinPool();
    Qsort.arr = A;
    if(begin <= end)
      threadPool.invoke(new Qsort(begin,end));
    else
      return;
  }

  static class Qsort extends RecursiveTask<Integer>
  {
    static int[] arr;
    int begin, end;

    public Qsort(int b, int e)
    {
      begin = b;
      end = e;
    }

    protected Integer compute()
    {
      if(end - begin <= 16)
      {
        insertionSort(begin,end);
        return 0;
      }
      else
      {
        ////Partioning
        swap((end-begin)/2 + begin,end-1); //Helps with nearly sorted arrays
        int pivot = end-1;
        int i = begin-1;
        for(int j = i+1; j<pivot; j++)
        {
          if(arr[j] <= arr[pivot])
          {
            i += 1;
            swap(i,j);
          }
        }
        //System.out.println(Arrays.toString(arr));
        swap(pivot,i+1);
        pivot = i+1;

        ////RecursiveTask
        Qsort left = new Qsort(begin,pivot);
        left.fork();
        Qsort right = new Qsort(pivot+1,end);
        return right.compute() + left.join();

      }
    }

    protected void insertionSort(int begin, int end)
    {
      for(int i = begin+1; i<end; i++)
      {
        int j = i;
        while(j>begin)
        {
          if(arr[j]<arr[j-1])
          {
            swap(j,j-1);
          }
          j--;
        }
      }
    }

    void swap(int i, int j)
    {
      int temp = arr[i];
      arr[i] = arr[j];
      arr[j] = temp;
    }
  }
}
