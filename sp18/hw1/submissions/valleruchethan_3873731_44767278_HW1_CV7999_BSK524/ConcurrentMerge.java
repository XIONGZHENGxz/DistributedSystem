//UT-EID= CV7999, bsk524

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class ConcurrentMerge extends RecursiveAction {
    private int[] A;
    private int[] B;
    private int[] C;
    private int[] splitA;
    private int[] splitB;
    private int global_count;

    public ConcurrentMerge(int[] A, int[] B, int[] splitA, int[] splitB, int[] C, int count) {
        this.A = A;
        this.B = B;
        this.splitA = splitA;
        this.splitB = splitB;
        this.global_count = count;
        this.C = C;
    }

    /*
     * compute() should recursively call splitArrayB (aka binary search) to ensure that array B is divided
     * correctly. Once the array is divided correctly, it can go ahead and merge.
     * This is where you would use the invokeAll() method from ForkJoinPool
     */
    @Override
    protected void compute() {
        //if count != 0 || splitA.length == 1 then go, else for loop to make splitA.length subtasks to run splitArrayB and then they all call mergeAB
        if(global_count == 0){
            splitArrayB();
        }
        if(global_count != 0 || splitA.length == 1){
            mergeAB();
        }
        else{
            List<ConcurrentMerge> subtasks = new ArrayList<>();
            ConcurrentMerge temp;
            for(int i = 0; i < splitA.length; i++){
                global_count++;
                temp = new ConcurrentMerge(A, B, splitA, splitB, C, global_count);
                subtasks.add(temp);
            }
            invokeAll(subtasks);
        }
    }

    /*
     * This method should take in array B and enter in the
     * LAST index of each "subarray" into dividedB. For instance:
     *
     * dividedB[0] contains the last index of array B that thread0 would
     * process (so it would look at the range:= 0 to dividedB[0] inclusive)
     */
    private void splitArrayB() {
        //find upper bound. anything below goes within.
        //calculate highest A, loop through b til above that, then iterate b index til at end of b
        int a_count = 0;
        int b_count = 0;
        int highA = A[splitA[a_count]];
        boolean first = true;
        for(int i = 0; i < B.length; i++){
            if(highA >= B[i]){
                first = false;
            }
            else if(highA < B[i]){
                if(first == false){
                    splitB[b_count] = i - 1;
                    b_count++;
                    highA = A[splitA[a_count]];
                    a_count++;
                }
                else{
                    while(a_count != splitA.length){
                        if(highA < B[i]){
                            splitB[b_count] = - 1;
                            b_count++;
                            a_count++;
                            highA = A[splitA[a_count]];
                        }
                        else{
                            splitB[b_count] = i;
                            b_count++;
                            a_count++;
                            highA = A[splitA[a_count]];
                            break;
                        }
                    }
                }
            }
        }
        if(b_count != splitB.length){
            splitB[b_count] = splitB.length - 1;
            b_count++;
            while(b_count < splitB.length){
                splitB[b_count] = -1;
                b_count++;
            }
            return;
        }
        splitB[b_count - 1] = b_count - 1;
    }

    /*
     * Merge A and B into C for this thread
     */
    private void mergeAB() {
        //merge only for the indexes of this global_count
        boolean a_end = false;
        boolean b_end = false;
        boolean start = false;
        int a_count = splitA[global_count - 1];
        if(a_count == A.length - 1){
            a_count = splitA[global_count - 2] + 1;
        }
        int b_count = splitB[global_count - 1];
        if(b_count == B.length - 1){
            if(global_count != splitB.length){
               start = true;
                b_count = 0;
            }
            else{
                b_count = splitB[global_count - 2] + 1;
            }
        }
        if(b_count == -1){
            b_end = true;
            b_count = 0;
            if(splitB[0] == B.length - 1){
                b_count = B.length;
            }
        }
        int c_count = a_count + b_count;
        while(!a_end || !b_end){
            if(a_end){
                C[c_count] = B[b_count];
                b_count++;
            }
            else if(b_end){
                C[c_count] = A[a_count];
                a_count++;
            }
            //if a > b
            else if(A[a_count] > B[b_count]){
                C[c_count] = B[b_count];
                b_count++;
                c_count++;
            }
            //if a <= b
            else{
                C[c_count] = A[a_count];
                a_count++;
                c_count++;
            }
            if(!start){
                if(a_count == splitA[global_count - 1] + 1){
                    a_end = true;
                }
                if(b_count == splitB[global_count - 1] + 1){
                    b_end = true;
                }
            }
            else{
                if(a_count == splitA[global_count - 1] + 1){
                    a_end = true;
                }
                if(b_count == splitB.length){
                    b_end = true;
                }
            }
        }
    }

}