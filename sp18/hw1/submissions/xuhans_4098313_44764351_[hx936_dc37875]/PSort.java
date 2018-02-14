//UT-EID=


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function
    ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    PSortTask mainTask = new PSortTask(A, begin, end - 1);
    mainTask.fork();
    mainTask.join();
  }

  private static class PSortTask extends RecursiveAction{
    int taskSize = 16;
    int[] arrayToSort;
    int start;
    int end;
    public PSortTask(int[] a, int s, int e)
    {
      arrayToSort = a;
      start = s;
      end = e;
    }
    @Override
    protected void compute() {
      int size = end - start;
      if (size <= taskSize) {
        //sort iteratively
        for (int i = start; i <= end; i++) {
          int valueToSort = arrayToSort[i];
          for (int j = i - 1; j >= start; j--) {
            if (valueToSort > arrayToSort[j])
              break;
            int temp = arrayToSort[j];
            arrayToSort[j] = valueToSort;
            arrayToSort[j + 1] = temp;
          }
        }
      }else if(start < end) {
        int p = partition();
        PSortTask left = new PSortTask(arrayToSort, start , p - 1);
        left.fork();
        PSortTask right = new PSortTask(arrayToSort, p + 1 , end);
        right.fork();
        left.join();
        right.join();
      }
    }
    private int partition(){
      int pivot = arrayToSort[end];
      int i = start - 1;
      for(int j = start; j <= end - 1; j++)
      {
        if(arrayToSort[j] <= pivot)
        {
          i++;
          int temp = arrayToSort[j];
          arrayToSort[j] = arrayToSort[i];
          arrayToSort[i] = temp;
        }
      }
      int temp = arrayToSort[i + 1];
      arrayToSort[i + 1] = pivot;
      arrayToSort[end] = temp;
      return i + 1;
    }

  }
}
