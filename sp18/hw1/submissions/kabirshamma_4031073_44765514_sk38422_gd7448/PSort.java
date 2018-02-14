//UT-EID=
//Shamma Kabir = sk38422
//George Doykan = gd7448


import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class PSort {


  public static void parallelSort(int[] arr, int low, int high) {

    // TODO: Implement your parallel sort function
    int processors = Runtime.getRuntime().availableProcessors();
    sort result = new sort(arr, low, high-1);

    ForkJoinPool pool = new ForkJoinPool(processors);

    pool.invoke(result);

  }

  public static void main(String[] args) {
    int[] x = {9, 2, 4, 7, 3, 7, 10};
    //System.out.println(Arrays.toString(x));

    int low = 0;
    int high = x.length;


    parallelSort(x, low, high);
    //System.out.println(Arrays.toString(x));
  }
}



class sort extends RecursiveAction {
  int a[];
  int low;
  int high;

  public sort(int[] a, int low, int high) {
    this.a = a;
    this.low = low;
    this.high = high;
  }

  @Override
  public void compute() {
	  if (a.length <= 16) {
		  insertionSort(a);
	  }
	  else if (low < high) {
    	int pi = get_indexes(a, low, high);
        sort left = new sort(a, low, pi - 1);
        sort right = new sort(a, pi + 1, high);
        left.fork();
        right.compute();
        left.join();
    } 
  }
  public static int get_indexes(int a[], int low, int high) {
    int p = a[high];
    int i = low - 1; 
    for (int j = low; j < high; j++) {
   
      if (a[j] <= p) {
        i++;

        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
      }
    }

    int temp = a[i + 1];
    a[i + 1] = a[high];
    a[high] = temp;

    return i + 1;
  }
  
  void insertionSort(int a[]) {
    int n = a.length;
    for (int i=1; i<n; ++i)
    {
      int temp = a[i];
      int j = i-1;

      while (j>=0 && a[j] > temp)
      {
        a[j+1] = a[j];
        j = j-1;
      }
      a[j+1] = temp;
    }
  }
}



