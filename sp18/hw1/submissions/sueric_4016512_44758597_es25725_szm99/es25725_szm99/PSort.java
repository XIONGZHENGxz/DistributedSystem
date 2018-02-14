//UT-EID= szm99 es25725
import java.util.concurrent.*;

public class PSort extends RecursiveAction {

  private int[] psArray;
  private int psBegin;
  private int psEnd;

  public PSort(int[] A, int begin, int end) {
    psArray = A;
    psBegin = begin;
    psEnd = end;
  }

  private void insertSort(int[] A, int begin, int end) {
    if (end - begin <= 1) {
      return;
    }
    int i = begin;
    while (i < end) {
      int j = i + 1;
      while (j > begin && j < end && A[j] < A[j - 1]) {
        int t = A[j];
        A[j] = A[j - 1];
        A[j - 1] = t;
        j -= 1;
      }
      i += 1;
    }
  }

  @Override
  protected void compute() {
    if (psEnd - psBegin <= 60) {
      insertSort(psArray, psBegin, psEnd); // TODO
    } else {
      // Choose pivot
      int pivot = psArray[psEnd - 1];
      int i = psBegin; int j = psEnd - 2;
      boolean iPin, jPin;
      while (i < j) {
        if (psArray[i] <= pivot) {
          i++;
          iPin = false;
        } else {
          iPin = true;
        }
        if (psArray[j] > pivot) {
          j--;
          jPin = false;
        } else {
          jPin = true;
        }
        if (iPin && jPin) {
          int temp = psArray[j];
          psArray[j] = psArray[i];
          psArray[i] = temp;
          i++;
          j--;
        }
      }
      int indexToSwap = i;
      // correct the swap index
      while (psArray[indexToSwap] < pivot) {
        indexToSwap++;
      }
      int temp = psArray[psEnd - 1];
      psArray[psEnd - 1] = psArray[indexToSwap];
      psArray[indexToSwap] = temp;

      invokeAll(new PSort(psArray, psBegin, indexToSwap), new PSort(psArray, indexToSwap, psEnd));
    }
  }

  public static void parallelSort(int[] A, int begin, int end){
    PSort ps = new PSort(A, begin, end);
    ForkJoinPool pool = new ForkJoinPool();
    pool.invoke(ps);
  }
}


