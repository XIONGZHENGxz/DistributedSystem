//UT-EID=bdz223


import java.util.*;
import java.util.concurrent.*;

public class PSort {
  public static void parallelSort(int[] A, int begin, int end) {
    // TODO: Implement your parallel sort function 
		if(begin < 0 || end > A.length || end <= begin) {
			return;
		}
		
		int processors = Runtime.getRuntime().availableProcessors();
		int[] list = new int[end - begin];
		for(int i = begin; i < end; i++) {
			list[i - begin] = A[i];
		}

		QuickSort q = new QuickSort(list);
		ForkJoinPool pool = new ForkJoinPool(processors);
		int[] result = pool.invoke(q);
		for(int i = begin; i < end; i++) {
			A[i] = result[i - begin];
		}
	}
}

class QuickSort extends RecursiveTask<int[]> {
	final int[] list;

	QuickSort(int[] list) {
		this.list = list;
	}

	protected int[] compute() {
		int listSize = list.length;
		if(listSize <= 16) {
			insertSort(list);
			return list;
		}

		int[] l1 = new int[listSize / 2];
		for(int i = 0; i < l1.length; i++) {
			l1[i] = list[i];
		}
		QuickSort q1 = new QuickSort(l1);
		q1.fork();

		int[] l2 = new int[(listSize / 2) + (listSize % 2)];
		for(int i = 0; i < l2.length; i++) {
			l2[i] = list[i + listSize / 2];
		}
		QuickSort q2 = new QuickSort(l2);

		return merge(q2.compute(), q1.join());
	}

	private void insertSort(int[] list) {
		if(list.length <= 1) {
			return;
		}
		
		for(int i = 1; i < list.length; i++) {
			int x = list[i];
			int j = i - 1;

			while(j >= 0 && list[j] > x) {
				list[j + 1] = list[j];
				j--;
			}
			list[j + 1] = x;
		}
		return;
	}

	private int[] merge(int a[], int b[]) {
		int mergedSize = a.length + b.length;
		int [] merged = new int[mergedSize];
		int aIndex = 0;
		int bIndex = 0;
		for(int i = 0; i < mergedSize; i++) {
			if(aIndex >= a.length) {
				merged[i] = b[bIndex];
				bIndex++;
			}
			else if(bIndex >= b.length) {
				merged[i] = a[aIndex];
				aIndex++;
			}
			else if(a[aIndex] <= b[bIndex]) {
				merged[i] = a[aIndex];
				aIndex++;
			}
			else {
				merged[i] = b[bIndex];
				bIndex++;
			}
		}

		return merged;
	}
}
