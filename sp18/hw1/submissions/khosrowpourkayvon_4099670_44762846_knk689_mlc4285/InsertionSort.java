/*
 * Names:    Kayvon Khosrowpour, Megan Cooper
 * Class:    EE 360P
 * UT EIDs:   knk689, mlc4285
 * Homework: 1
 */

import java.util.Arrays;

public class InsertionSort {
	
	public static void insertionSort(int[] array) {
		
		if (array == null || array.length <= 1) return;
		
		for (int i = 1; i < array.length; i++) {
			int j = i-1;
			int k = i;
			
			while (j >= 0 && array[k] < array[j]) {
				int temp = array[k];
				array[k] = array[j];
				array[j] = temp;
				j--;k--;
			}
			
		}

	}
	
	// array to be sorted
	// lo = lower index of subarray
	// hi = higher index+1 of subarray
	public static void insertionSortSubarray(int[] array, int lo, int hi) {
		if (array == null || array.length <= 1 || lo >= hi || hi-lo == 1) return;
		
		for (int i = lo+1; i < hi; i++) {
			int j = i-1;
			int k = i;
			
			while (j >= lo && array[k] < array[j]) {
				int temp = array[k];
				array[k] = array[j];
				array[j] = temp;
				j--;k--;
			}
			
		}
	}

}




























