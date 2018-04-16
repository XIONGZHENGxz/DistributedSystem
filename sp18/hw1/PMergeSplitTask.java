
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;

import java.util.ArrayList;


public class PMergeSplitTask implements Callable<Boolean> {

    int[] arr, A, result;
    int offset;
    
    public PMergeSplitTask(int[] input, int[] a, int[] result, int offset) {
        arr = input;
        this.A = a;
        this.result = result;
        this.offset = offset;
    }

    public Boolean call() {
        ArrayList<ForkJoinTask> taskList = new ArrayList<ForkJoinTask>();

        for(int i=0; i<arr.length; i++) {
            ForkJoinTask task = ForkJoinTask.adapt(new PMergeStoreTask(arr[i], i, A, result, offset));
            task.fork();

            taskList.add(task);
        }

        for (ForkJoinTask task : taskList) {
            task.join();
        }

        return true;
    }
}
