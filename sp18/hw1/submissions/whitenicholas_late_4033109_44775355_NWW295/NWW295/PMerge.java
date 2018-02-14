//UT-EID= nww295


import java.util.*;
import java.util.concurrent.*;


public class PMerge implements Callable<Integer>{

    protected static ExecutorService pool = Executors.newCachedThreadPool();
    protected static Future<Integer> threads[];
    public int[] merge;
    public static int[] output;
    public static int[] first;
    public static int[] second;
    public int abIndex;
    public boolean isA;



    public PMerge(int[] mergeArr, int index, boolean isA) {
      this.merge = mergeArr;
      this.abIndex = index;
      this.isA = isA;
    }

    public Integer call() {
        int mainIndex = 0, bIndex = 0, aIndex = 0;
        if(this.merge == null) {
            return 0;
        }
        for(int i = 0; i < this.merge.length; i++) {
            int begin = 0;
            int middle = 0;
            int end = 0;

            if (isA) {
                end = second.length - 1;
                while (end >= begin) {
                    middle = (end + begin) / 2;
                    if (first[this.abIndex + i] < second[middle]) {
                        end = middle-1;
                    } else if (first[this.abIndex+i] == second[middle]) {
                        output[(this.abIndex+i) + middle] = first[this.abIndex+i];
                        break;
                    } else {
                        begin = middle+1;
                    }
                }
                output[this.abIndex+i+end+1] = first[this.abIndex+i];
            } else {
                end = first.length - 1;
                while(end >= begin) {
                    middle = (end+begin)/2;
                    if(second[this.abIndex+i] < first[middle]) {
                        end = middle-1;
                    } else {
                        begin = middle+1;
                    }
                }
                output[this.abIndex+i+end+1] = second[this.abIndex + i];
            }
        }
        return 0;
    }

    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
      ExecutorService service = Executors.newSingleThreadExecutor();
      output = C;
      first = A;
      second = B;
      int totalThreads = 0;
      if(A == null || B == null || A.length < 1 || B.length < 1 || numThreads < 1) {
          service.shutdown();
          pool.shutdown();
          return;
      }

      else if((A.length + B.length) < numThreads) {
          threads = (Future<Integer>[])(new Future[numThreads]);
          int countThreads = 0;
          int index = 0;
          int arrIndex = 0;

          while(countThreads < (A.length + B.length)) {
              if(index < A.length) {
                  PMerge aMerge = new PMerge(Arrays.copyOfRange(A, index, index+1), index, true);
                  Future<Integer> aFut = service.submit(aMerge);
                  threads[arrIndex] = aFut;
                  arrIndex++;
                  countThreads++;
              }
              if(index < B.length) {
                  PMerge bMerge = new PMerge(Arrays.copyOfRange(B, index, index+1), index, false);
                  Future<Integer> bFut = service.submit(bMerge);
                  threads[arrIndex] = bFut;
                  arrIndex++;
                  countThreads++;
              }

              index++;
          }

          /*while(countThreads < numThreads) {
              PMerge merge = new PMerge(null, 0, false);
              Future<Integer> fut = service.submit(merge);
              threads[arrIndex] = fut;
              countThreads++;
          }*/

          totalThreads = countThreads;

      }

      else {
          threads = (Future<Integer>[])(new Future[numThreads]);
          int numElem = (A.length + B.length)/numThreads;
          if((A.length + B.length)%numThreads != 0) {
              numElem++;
          }
          int index = 0;
          int countThreads = 0;
          int arrIndex = 0;

          while(index < A.length || index < B.length) {
              if(index+numElem < A.length) {
                  PMerge newP = new PMerge(Arrays.copyOfRange(A, index, index + numElem), index, true);
                  Future<Integer> futA = service.submit(newP);
                  threads[arrIndex] = futA;
                  arrIndex++;
                  countThreads++;
              }
              else if(index < A.length) {
                  PMerge newP = new PMerge(Arrays.copyOfRange(A, index, A.length), index, true);
                  Future<Integer> futA = service.submit(newP);
                  threads[arrIndex] = futA;
                  arrIndex++;
                  countThreads++;

              }
              if(index+numElem < B.length) {
                  PMerge newB = new PMerge(Arrays.copyOfRange(B, index, index+numElem), index, false);
                  Future<Integer> futB = service.submit(newB);
                  threads[arrIndex] = futB;
                  arrIndex++;
                  countThreads++;
              }
              else if(index < B.length) {
                  PMerge newB = new PMerge(Arrays.copyOfRange(B, index, B.length), index, false);
                  Future<Integer> futB = service.submit(newB);
                  threads[arrIndex] = futB;
                  arrIndex++;
                  countThreads++;
              }

              index = index+numElem;

          }

          totalThreads = countThreads;
      }
      boolean flag = false;
      while(!flag) {
          flag = true;
          for(int i = 0; i < totalThreads; i++) {
              try {
                  if((int)threads[i].get() != 0) {
                      flag = false;
                  }
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } catch (ExecutionException e) {
                  e.printStackTrace();
              }
          }
      }
      service.shutdown();
      pool.shutdown();
  }
}
