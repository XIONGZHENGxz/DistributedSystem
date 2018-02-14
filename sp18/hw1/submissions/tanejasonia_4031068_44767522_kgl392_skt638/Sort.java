public class Sort implements Runnable {
    private int[] a;
    private int numThreads;

    public Sort(int[] a, int numThreads) {
        this.a = a;
        this.numThreads = numThreads;
    }

    public void run() {
        PMerge.mergeSort(a, numThreads);
    }
}