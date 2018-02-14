//UT-EID=apl652, rs48368


import java.util.concurrent.*;


public class PMerge{

    private enum ArrayLetter {
        A, B
    }

    // If A and B have duplicates, A will always be placed at the lowest index
    public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){

        /** ---------- Merger Class ---------- **/
        class Merger implements Runnable {
            private int threadNum;
            private ArrayLetter currentArray;
            private int index;
            private int indicesHandled;

            public Merger(int threadNum) {
                this.threadNum = threadNum;
                this.currentArray = ArrayLetter.A;  // Autoset to A
            }

            @Override
            public void run() {
                init();

                int totalLess = 0;
                int value = 0;
                while (indicesHandled > 0) {
                    value = getCurrentValue();
                    totalLess = countLessInA(value) + countLessInB(value);
                    C[totalLess] = value;

                    indicesHandled--;
                    index++;
                    setCurrentArray();
                }
            }

            /**
             * Initializes 'this.index', the starting index, and
             * 'this.indicesHandled', the indices left to handle.
             * Sets the currentArray to A or B base
             */
            private void init() {
                int totalIndices = A.length + B.length;
                int indicesPerThread = totalIndices / numThreads;
                int mod = totalIndices % numThreads;

                // Setting 'index' and 'indicesHandled'
                if (threadNum >= mod) {
                    index = (threadNum * indicesPerThread) + mod;
                    indicesHandled = indicesPerThread;
                }
                // threadNum < mod
                else {
                    index = threadNum * (indicesPerThread + 1);
                    indicesHandled = indicesPerThread + 1;
                }

                // Set this.currentArray
                setCurrentArray();
            }

            /**
             * Sets this.currentArray to B if needed
             */
            private void setCurrentArray() {
                if (index >= A.length && currentArray == ArrayLetter.A) {
                    currentArray = ArrayLetter.B;
                    index = index - A.length;
                }
            }

            /**
             * Return value of "this.index" in "this.currentArray"
             */
            private int getCurrentValue() {
                if (currentArray == ArrayLetter.A) {
                    return A[index];
                }
                return B[index];
            }

            /**
             * Returns the number of values in A less than (or equal to
             * depending on this.currentArray) 'val'
             *
             * This is weird because we take care of duplicates between arrays
             */
            private int countLessInA(int val) {
                int count = 0;
                int i = 0;

                while (i < A.length) {
                    if ((currentArray == ArrayLetter.A && (A[i] >= val))
                            || (currentArray == ArrayLetter.B && (A[i] > val))) {
                        break;
                    }
                    i++;
                    count++;
                }
                return count;
            }

            /**
             * Returns the number of values in B less than 'val'
             */
            private int countLessInB(int val) {
                int count = 0;
                int i = 0;

                while (i < B.length && B[i] < val) {
                    i++;
                    count++;
                }
                return count;
            }
        }
        /** ---------- End Merger Class ---------- **/

        // Error Handling
        int maxThreads = A.length + B.length;
        int effectiveNumThreads = numThreads;

        if (effectiveNumThreads > maxThreads) {
            effectiveNumThreads = maxThreads;
        }

        // Start all threads
        ExecutorService es = Executors.newCachedThreadPool();
        int threadNum = 0;
        while (threadNum < effectiveNumThreads) {
            es.execute(new Merger(threadNum));
            threadNum++;
        }

        // Wait for all to finish
        es.shutdown();
        try {
            es.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    } /** ---------- End parallelMerge() ---------- **/
}
