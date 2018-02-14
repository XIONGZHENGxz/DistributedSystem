/* Shashank Kambhampati: skk834
 * Shrikara Murthy: svm456
 */
import java.util.concurrent.*;

public class PMerge{ 

    public static int a[];
    public static int b[];
    public static int c[];

    //real method
    public static int parallelMerge(int[] A, int[] B, int[] C, int numThreads){
        //initialize stuff
        a = A;
        b = B;
        c = C;
        ExecutorService exec = Executors.newFixedThreadPool(numThreads);
       
        //create a bunch of tasks
        for(int x = 0; x < A.length; x++){
            final int i = x; 
            exec.submit(() -> changePosition(i, true));
       }
        for(int x = 0; x < B.length; x++){
            final int i = x;
            exec.submit(() -> changePosition(i, false));
        }

        try {
            exec.shutdown();
            exec.awaitTermination(1, TimeUnit.MINUTES);
        } catch(Exception e){
            System.out.println("RIP");
            return 0;
        }
        return 1;     
    }

    //takes the index of t and whether it belongs to A
    //finds out what place it belongs at in C
    private static void changePosition(int t, boolean isA){
        int index = t;
        if(isA){
           index += find(b, a[t], true);
           c[index] = a[t];
        }
        else{
           index += find(a, b[t], false);
           c[index] = b[t];
        }
       
    }

    //binary search of array a with target t
    //returns where t should be if it doesn`t find it
    private static int find(int[] a, int t, boolean isA){
        int low = 0;
        int high = a.length - 1;
        int mid;

        while(low <= high) {
            mid = (low + high) / 2;

            if(a[mid] < t ||  (isA && a[mid] == t)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }
}
