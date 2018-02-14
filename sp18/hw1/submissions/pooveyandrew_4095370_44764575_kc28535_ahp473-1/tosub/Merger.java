import java.util.Arrays;

public class Merger implements Runnable {
    boolean first;
    int a[];
    int b[];
    int c[];
    int start;
    int end;
    public Merger(){}
    public Merger(int start, int end, int [] a, int[] b, int[]c , boolean first) {
        this.start = start;
        this.end = end;
        this.a = a;
        this.b = b;
        this.c = c;
        this.first = first;
    }

    public static void main(String[] args) {
        int[] a = {1, 3, 5};
        int[] b = {1,2,4,6};
        Merger m = new Merger();
        System.out.println(m.binarySearch(b, 3));
    }

    @Override
    public void run() {
        for(int k = start; k < end; k++) {
            int newIndex = k + binarySearch(b, a[k]);
            System.out.println("adding:" + a[k] + "\nk:" + k +"\nbin search:" + binarySearch(b, a[k]) + "\nadding at:" + (k + binarySearch(b, a[k])));
            c[newIndex] = a[k];
            System.out.println(Arrays.toString(c));
        }
    }

    private int binarySearch(int arr[], int target) {
        if(arr.length == 0) return -1;
        int high = arr.length - 1;
        int low = 0;
        if(arr[0] > target) return 0;
        if(arr[high] < target) return high+1;
        while(high > low) {
            int mid = (high + low) / 2;
            if(arr[mid] == target) {
                if(first) return mid;
                else return mid +1;
            }

            //check if it is in the middle
            if(arr[mid - 1] <= target && arr[mid] >= target) return mid;
            if(arr[mid] <= target && arr[mid +1] >= target) return mid+1;

            //set the next low and high
            if(arr[mid] < target) low = mid + 1;
            else high = mid - 1;
        }
        return -7;
    }
}
