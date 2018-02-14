//UT-EID=asm3539


import java.util.*;
import java.util.concurrent.*;

public class PMerge{
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
	  
	  ExecutorService es = Executors.newFixedThreadPool(numThreads);
	  
	  List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(numThreads);
	  
	  int sizeChunks = (A.length+B.length)/numThreads;
	  
	  int numChunksA = numThreads/2;
	  int numChunksB = numThreads-numChunksA;
	  
	  int chunkSizeA = sizeChunks;
	  int chunkSizeB = sizeChunks;
	  for(int i =0;i<numChunksA;i++) {
		  int begin = i*chunkSizeA;
		  //checking if its the last chunk
		  int end = begin +chunkSizeA-1;
		  if(i==(numChunksA-1)) {
		  end= (A.length-1);
		  }
		  ParMerge p = new ParMerge(A,B,C,0,begin,end);
		  tasks.add(p);
		}
	  
	  for(int i =0;i<numChunksB;i++) {
		  int begin = i*chunkSizeB;
		  //checking if its the last chunk
		  int end = begin+chunkSizeB-1;
		  if(i==(numChunksB-1)) {
		  end= (A.length-1);
		  }		  
		  ParMerge p = new ParMerge(A,B,C,1,begin,end);
		  tasks.add(p);
		}
	  try {
		List<Future<Void>> result = es.invokeAll(tasks);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  }  
    
  }


