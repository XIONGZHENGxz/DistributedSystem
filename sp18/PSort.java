//UT-EID=


import java.util.*;
import java.lang.InterruptedException;
import java.util.concurrent.*;

public class PSort{

		static class sortingThread implements Runnable {
				int[] A;
				int b, e;
				ExecutorService es;
				public sortingThread(int[] A, int begin, int end, ExecutorService es) {
						this.A = A;
						b = begin;
						e = end;
						this.es = es;
				}

				public void run() {
						if(e - b + 1 <= 16) {
								insertionSort(A, b, e);
								return;
						}

						int pivot = partition(A, b, e);
						Future f1 = es.submit(new sortingThread(A, b, pivot, es));
						Future f2 = es.submit(new sortingThread(A, pivot, e, es));
						try {
								f1.get();
								f2.get();
						} catch (Exception e) {
								e.printStackTrace();
						}


				}
		}

		public static void parallelSort(int[] A, int begin, int end){
				// TODO: Implement your parallel sort function 
				ExecutorService es = Executors.newFixedThreadPool(1000);
				sortingThread t = new sortingThread(A, begin, end, es);
				Future f = es.submit(t);
				try {
						f.get();
				} catch(Exception e) {
						e.printStackTrace();
				}
				es.shutdown();
		}

		public static int partition(int[] A, int begin, int end) {
				int pivot = A[end - 1];
				int i = begin - 1;
				for(int j = begin; j < end - 1; j ++) {
						if(A[j] < pivot) {
								swap(A, ++i, j);
						}
				}
				if(A[i + 1] > pivot) swap(A, i + 1, end - 1);
				return i + 1;
		}

		public static void insertionSort(int[] A, int start, int end) {
				int i = start, j = start;
				while(i < end) {
						j = i;
						while(j > start && A[j - 1] > A[j]) {
								swap(A, j - 1, j--);
						}
						i++;
				}
		}


		public static void swap(int[] A, int i, int j) {
				int t = A[i];
				A[i] = A[j];
				A[j] = t;
		}

}
