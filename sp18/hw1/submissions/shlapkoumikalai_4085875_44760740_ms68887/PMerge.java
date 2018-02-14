//UT-EID=ms68887


import java.util.ArrayList;


public class PMerge{
  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads){
	  //no point having more than 1 thread per element
	  if(numThreads > C.length)
	  {
		  numThreads = C.length;
	  }
	  ArrayList<Merger> threads = new ArrayList<Merger>(numThreads);
	  //distribute threads for A and B, A and B not the same
	  int elementNum = (A.length + B.length)/numThreads;
	  //set up A threads
	  int aThreads = A.length / elementNum;
	  if(aThreads == 0) aThreads = 1; //means the elementNum > A.length, and just need 1 thread to process A
	  //if i have 5 threads for 2 lists of 5, this will give 2 threads for A of size 2, ignoring the last element
	  //need to make sure that the last thread covers anything extra at the end
	  int index = 0;
	  for(index = 0; index < aThreads; index++)
	  {
		  if(index == aThreads - 1)
		  {
			  threads.add(index, new Merger(A, B, C, index*elementNum, (A.length - index*elementNum) + 1));
		  }
		  else
		  {
			  threads.add(index, new Merger(A, B, C, index*elementNum, elementNum));
		  }
	  }
	  
	  //set up B threads (till end of thread list)
	  for(int i = index; i < numThreads; i++)
	  {
		  if(i == numThreads - 1)
		  {
			  threads.add(i, new Merger(B, A, C, (i - index)*elementNum, (B.length - (i - index)*elementNum) + 1));
		  }
		  else
		  {
			  threads.add(i, new Merger(B, A, C, (i - index)*elementNum, elementNum));
		  }
	  }
	  
	  for(int i = 0; i < numThreads; i++)
	  {
		  threads.get(i).start();
	  }
	  
	  for(int i = 0; i < numThreads; i++)
	  {
		  try 
		  {
			threads.get(i).join();
		  }
		  catch (InterruptedException ignore) {}
	  }
	  
  }
}

class Merger extends Thread {
	private int[] input;
	private int[] compare;
	private int[] result;
	private int start;
	private int numOfElements;
	
	Merger(int[] input, int[] compare, int[] result, int start, int numOfElements)
	{
		this.input = input;
		this.compare = compare;
		this.result = result;
		this.start = start;
		this.numOfElements = numOfElements;
	}
	
	public void run()
	{
		int end = start + numOfElements;
		if(end > input.length)
		{
			end = input.length;
		}
		
		for(int i = start; i < end; i++)
		{
			int newIndex = i + comparisonSearch(compare, input[i]);
			//test this, originally just this
			//result[newIndex] = input[i];
			if(result[newIndex] == input[i])
			{
				result[newIndex - 1] = input[i];
			}
			else
			{
				result[newIndex] = input[i];
			}
		}
	}
	
    private int comparisonSearch(int arr[], int x)
    {
    	for(int i = 0; i < arr.length; i++)
    	{
    		if(arr[i] > x)
    		{
    			return i;
    		}
    	}
    	return arr.length;
    }
}