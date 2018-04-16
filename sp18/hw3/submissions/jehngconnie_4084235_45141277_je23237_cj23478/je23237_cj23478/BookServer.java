import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {
	  Inventory inventory;
	  private int MAX_THREADS = 50;
	  private boolean terminate;
	  private ExecutorService threadPool;
	  private UDPListener udpLst;
	  private TCPListener tcpLst;
	  int tcpPort = 7000;
	  int udpPort = 8000;
	  String inputFile;

	  private static BookServer instance;
	  private BookServer(String s){
		  inputFile = s;
	  }
	  public static void main (String[] args) {
		  if(args.length>0)
		  	System.out.println("Server starting using input file: " + args[0]);
		  else
			System.out.println("Server starting using default");

		 // System.out.println("Directory: " + System.getProperty("user.dir"));		  
		  String file = args.length == 0 ? null : args[0];
		  instance = new BookServer(file);
		  instance.runServer();
	  }
	  public void runServer(){
		  try {
			inventory = new Inventory(inputFile);
			this.terminate = true;
			udpLst = new UDPListener(udpPort);
			tcpLst = new TCPListener(tcpPort);
		  } catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  threadPool = Executors.newFixedThreadPool(MAX_THREADS);
		  threadPool.execute(udpLst);
		  threadPool.execute(tcpLst);	
		  /* pool no longer accepting new tasks */
		  threadPool.shutdown();
		  /* wait for threads to finish */
		  try {
			// Java concurrency description: Long.MAX_VALUE makes timeout indefinite
			if(threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//	    String list = instance.inventory.bookInventory();
//    	System.out.println("~~~~INVENTORY PRINTER");
//	    try {
//	    	BufferedWriter out = new BufferedWriter(new FileWriter("inventory.txt", false));
//			out.write(list);
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	System.out.println("~~~~~INVENTORY LIST ON EXIT: \n"+list);
	  }

	protected static String commandLineHandler(String sentence) throws IOException {
		String[] current = sentence.split(" ");
		String response = null;
		System.out.println("received command: " + current[0].trim());
		switch (current[0].trim()) {
		case "borrow":
			String title = sentence.replace(current[0] + " " + current[1], " ");
			System.out.println("CLIENT TRYING TO BORROW " + title.trim());
			response = instance.inventory.borrow(current[1].trim(), title.trim());
			break;
		case "return":
			response = instance.inventory.returnBook(current[1].trim());
			break;
		case "list":
			response = instance.inventory.list(current[1].trim());
			break;
		case "inventory":
			response = instance.inventory.bookInventory();
			break;
		case "exit":
			//exit();
			response = instance.inventory.bookInventory();
			break;
		}
		return response;
	}
    /* kill all function */
    public static void exit() {
    	// tell all other threads to stop
    	long curTid = Thread.currentThread().getId();
    	instance.udpLst.shutdown(curTid);
    	instance.tcpLst.shutDown(curTid);
    	instance.terminate = true;
    }
    public static boolean stopListening(){
    	return instance.terminate;
    }
    public static void stopAllListeningThreads(){
    	instance.threadPool.shutdownNow();
    }
    
}