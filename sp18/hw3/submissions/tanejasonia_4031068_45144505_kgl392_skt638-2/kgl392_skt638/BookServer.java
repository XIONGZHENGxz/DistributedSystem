import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class BookServer {
	static NameTable table;
	
	public BookServer(HashMap<String,Integer> inventory, DatagramSocket ds, ArrayList<String> orderedInventory) {
		table = new NameTable(inventory, ds, orderedInventory);
	}
	
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    boolean udp = true;
    HashMap <String,Integer> inventory = new HashMap <String, Integer>();
    ArrayList<String> orderedInventory = new ArrayList<String>();
    DatagramSocket datasocket = null;
    ServerSocket listener = null;
    Socket s;
    
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;
    int len = 1024;
    
    // parse the inventory file
 	getInventory(inventory, fileName, orderedInventory);
 	
 	//initialize UDP port
	try {
		 listener = new ServerSocket(tcpPort);

	} catch (SocketException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	byte[] buf = new byte[len];
	BookServer ns = new BookServer(inventory, datasocket, orderedInventory);
	//System.out.println("BookServer started:");
    Thread udpThread = new UdpThread(BookServer.table);
    udpThread.start();
	while(true){
	
				try {
					while ( (s = listener.accept()) != null) {
						// System.out.println("TCP started");
						ServerThread t = new ServerThread(BookServer.table, s, udp);
						t.start();
					}
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
	// what if there are quotations in the book name
	
	  }
  
  	public static void getInventory(HashMap<String,Integer> inventory, String fileName,  ArrayList<String> orderedInventory ){
  	    try {
  	        // FileReader reads text files in the default encoding.
  	        FileReader fileReader = new FileReader(fileName);

  	        // Always wrap FileReader in BufferedReader.
  	        BufferedReader bufferedReader = new BufferedReader(fileReader);

  	        String line;
  			while((line = bufferedReader.readLine()) != null) {
  	            String [] list = line.split("\"");
  	          //  System.out.println(list[2]);
  	    		list[2] = list[2].replaceAll(" ", "");
  	    		inventory.put(list[1], Integer.valueOf(list[2]));
  	    		orderedInventory.add(list[1]);
  	        }
  			
  	        // Always close files.
  	        bufferedReader.close();         
  	    }
  	    catch(FileNotFoundException ex) {
  	        System.out.println(
  	            "Unable to open file '" + 
  	            fileName + "'");                
  	    } catch (IOException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
		return;
  	}
}