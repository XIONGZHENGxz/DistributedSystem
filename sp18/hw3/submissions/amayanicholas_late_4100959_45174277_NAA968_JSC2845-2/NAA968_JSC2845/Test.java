import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    public static void main(String args[]) {

        ExecutorService taskRunner = Executors.newFixedThreadPool(10);

        AtomicInteger numThreads = new AtomicInteger(2);

        String[] args1 = new String[] {"src/cmdFile","1"};
        String[] args2 = new String[] {"src/cmdFile","2"};
        String[] args3 = new String[] {"src/cmdFile","3"};
        String[] args4 = new String[] {"src/cmdFile","4"};
        String[] args5 = new String[] {"src/cmdFile","5"};

        taskRunner.submit(() -> BookClient.main(args1));
//        taskRunner.submit(() -> BookClient.main(args2));
//        taskRunner.submit(() -> BookClient.main(args3));
//        taskRunner.submit(() -> BookClient.main(args4));
//        taskRunner.submit(() -> BookClient.main(args5));

        try {
            taskRunner.shutdown();
            taskRunner.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {}

    }
}
