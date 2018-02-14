
//UT-EID= mjo588
//UT-EID= ta9887

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PMerge implements Runnable {
	private int[] in;
	private int[] other;
	private int[] out;
	private int begin;
	private int end;

	public PMerge(int[] in, int[] other, int[] out, int begin, int end) {
		super();
		this.in = in;
		this.other = other;
		this.out = out;
		this.begin = begin;
		this.end = end;
	}

	public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
		ExecutorService es = Executors.newSingleThreadExecutor();
		ArrayList<PMerge> subs = new ArrayList<>();

		if (C.length <= numThreads) {
			for (int x = 0; x < A.length; x++) {
				subs.add(new PMerge(A, B, C, x, x + 1));
			}
			for (int x = 0; x < B.length; x++) {
				subs.add(new PMerge(B, A, C, x, x + 1));
			}
		} else {
			int subSize = C.length / numThreads;
			int aThreads = (int) Math.ceil((double) A.length / subSize);
			for (int x = 0; x < aThreads; x++) {
				int end = (x + 1) * subSize;
				if (end > A.length)
					end = A.length;
				subs.add(new PMerge(A, B, C, x * subSize, end));
			}
			int bThreads = numThreads - aThreads;
			subSize = B.length / bThreads;
			for (int x = 0; x < bThreads; x++) {
				int end = (x + 1) * subSize;
				if (end > B.length)
					end = B.length;
				subs.add(new PMerge(B, A, C, x * subSize, end));
			}
		}

		ArrayList<Future<?>> futures = new ArrayList<>();
		for (PMerge sub : subs) {
			futures.add(es.submit(sub));
		}
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		es.shutdown();
	}

	@Override
	public void run() {
		for (int x = begin; x < end; x++) {
			int y = Arrays.binarySearch(other, in[x]);
			if (y < 0)
				y = -y - 1;
			else {
				out[x + y + 1] = in[x];
			}
			out[x + y] = in[x];
		}
	}
}