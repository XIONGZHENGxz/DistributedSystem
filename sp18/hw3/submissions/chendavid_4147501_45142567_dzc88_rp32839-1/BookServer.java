import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class BookServer {
	public static void main (String[] args) {
		int tcpPort;
		int udpPort;
	    if (args.length != 1) {
	      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
	      System.exit(-1);
	    }
		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;
		
		// parse the inventory file
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Scanner scan = new Scanner(fr);
		HashMap<String, Integer> inventory = new LinkedHashMap<String, Integer>();
		Library library = new Library(inventory);
		
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] tokens = StringUtils.quoteSplit(line);
			String title = tokens[0];
			Integer quantity = new Integer(tokens[1]);
			inventory.put(title, quantity);
		}
		
		// TODO: handle request from clients
		Thread udpServer = new Thread(new UdpServer(udpPort, library));
		udpServer.start();
		Thread tcpServer = new Thread(new TcpServer(tcpPort, library));
		tcpServer.start();
	}
}