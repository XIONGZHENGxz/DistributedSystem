/*
 * @file QuickSort.java
 * ----------
 * Parallel Version of Quick Sort using RecursiveTask.
 * ----------
 * @author Ze Lyu, Patrick Zhu
 * Feb 5, 2018
 */

import java.util.*;
import java.util.concurrent.RecursiveTask;

class QuickSort extends RecursiveTask<int[]>{
    
    // MARK: - Properties
    
    // array to be sorted.
    private int[] array;
    // begin index for portion of array to be sorted
    private int beginIndex;
    // end index for portion of array to be sorted
    private int endIndex;
    
    
    // MARK: - Constructors
    
    /**
     * Just initialize properties.
     * @param array: array to be sorted.
     * @param beginIndex: begin index for portion of array to be sorted.
     * @param endIndex: end index for portion of array to be sorted.
     */
    QuickSort(int[] array, int beginIndex, int endIndex){
        this.array = array;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }
    
    
    
    // MARK: - Override Methods
    
    @Override
    protected int[] compute() {
        // if length of A is less or equal to 16, use sequential insert sort
        if (endIndex - beginIndex <= 16) {
            insertionSort();
        } else {
            List<QuickSort> subtasks = createSubtasks();
            subtasks.get(0).fork();
            subtasks.get(1).fork();
            subtasks.get(0).join();
            subtasks.get(1).join();
        }
        return null;
    }
    
    
    
    // MARK: - Private Implementation
    
    /**
     * Description: quick sort array partition and subtask creation for forkJoinPool.
     */
    private List<QuickSort> createSubtasks() {
        // pivot point of current partition
        int pivot = array[endIndex - 1];
        // wall index of current partition
        int wallIndex = beginIndex;
        // partition
        for(int i = beginIndex; i < endIndex - 1; i++) {
            if (array[i] < pivot) {
                if(wallIndex != i) {
                    int temp = array[i];
                    array[i] = array[wallIndex];
                    array[wallIndex] = temp;
                }
                wallIndex++;
            }
        }
        // swap pivot with first element greater than it
        array[endIndex - 1] = array[wallIndex];
        array[wallIndex] = pivot;
        // begin and end index for 2 subtasks
        int beginIndex1 = beginIndex;
        int endIndex1 = wallIndex;
        int beginIndex2 = wallIndex + 1;
        int endIndex2 = endIndex;
        
        // create a list for 2 subtasks
        List<QuickSort> subtasks = new ArrayList<QuickSort>();
        QuickSort subtask1 = new QuickSort(array, beginIndex1, endIndex1);
        QuickSort subtask2 = new QuickSort(array, beginIndex2, endIndex2);
        // add both subtasks to subtasks list
        subtasks.add(subtask1);
        subtasks.add(subtask2);
        
        return subtasks;
    }
    
    /**
     * Description: Simple insertion sort for array with size smaller or equal to 16.
     */
    private void insertionSort() {
        // iterate through array
        for(int i = beginIndex; i < endIndex - 1; i++)
            // swapping next unsorted element to right position
            for(int j = i + 1; j > 0; j--)
                // if this unsorted element is smaller, swap
                if(array[j] < array[j - 1]) {
                    int temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                } else break; // if it's larger stop swapping process
    }
}

