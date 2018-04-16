//package homework3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.text.html.HTMLDocument.Iterator;

public class BookServer {

	static HashMap<String, ArrayList<Integer>> people = new HashMap<String, ArrayList<Integer>>();
	static HashMap<Integer, String> record_ID = new HashMap<Integer, String>();
	static HashMap<Integer, String> record_name = new HashMap<Integer, String>();
	static ArrayList<Node> lib_list = new ArrayList<Node>();
	static ArrayList<Node> curr_lib_list = new ArrayList<Node>();
	static int record_id = 1;
	static DatagramSocket udp_dataSocket = null;
	static DatagramSocket tcp_dataSocket = null;

	public static void main(String[] args) throws FileNotFoundException {
		int tcpPort;
		int udpPort;
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}
		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;
		int len = 1024;

		DatagramPacket dataPacket;

		Thread tcp_thread = new TCPServer(tcpPort);
		tcp_thread.start(); 
		
		/*
		//System.out.println("SECOND TEST: TyYOYO ");
		StringBuilder sb = new StringBuilder();
		sb.append("/Users/justinliang/Documents/workspace/Concurrent/src/homework3/");
		sb.append(args[0]);
		File file = new File(sb.toString());
		//Scanner sc = new Scanner (file);
		 * */
		 
		Scanner sc = new Scanner (new FileReader (args[0]));
		while (sc.hasNextLine()) {
			String info = sc.nextLine();
			String[] tokens = info.split("\"");
			String book_name = "\"" + tokens[1] + "\"";
			String[] num_tokens = tokens[2].split(" ");
			int book_number = Integer.parseInt(num_tokens[1]);
			// book name is in token 1 and book number is in token 0
			lib_list.add(new Node(book_name, book_number));
			curr_lib_list.add(new Node(book_name, book_number));
		}
		sc.close();
/*
		for (Node key : lib_list) {
			System.out.println(key.name + " " + key.amount);
		}
*/
		// parse the inventory file

		// TODO: handle request from clients
		
		try {
			udp_dataSocket = new DatagramSocket(udpPort);
			while (true) { //always udp

				byte[] buf = new byte[len];
				dataPacket = new DatagramPacket(buf, buf.length);
				udp_dataSocket.receive(dataPacket);
				String cmd = new String(dataPacket.getData(), 0, dataPacket.getLength());
				
				Thread t1 = new UDP_thread(cmd, dataPacket);
				t1.start();

			}
		} catch (SocketException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

	}

	public synchronized static String borrow(String[] cmd) {

		for (int i = 0; i < curr_lib_list.size(); i++) {
			// System.out.println(cmd[0]+ " " + cmd[1] );
			Node lib_book = curr_lib_list.get(i);
			if (lib_book.name.compareTo(cmd[1]) == 0) { // found the book
				if (lib_book.amount == 0) {
					return "Request Failed - Book not available";
				} else {
					lib_book.amount--;

					String ret = "Your request has been approved, " + record_id + " " + cmd[0] + " " + cmd[1];

					if (people.containsKey(cmd[0])) { //person borrowed a book
						people.get(cmd[0]).add(record_id);
						record_ID.put(record_id, cmd[1]);
						record_name.put(record_id, cmd[0]);
					} else { // not in the hashmap person did not borrow a book yet
						ArrayList<Integer> ids = new ArrayList<Integer>();
						ids.add(record_id);
						people.put(cmd[0], ids);

						record_ID.put(record_id, cmd[1]);
						record_name.put(record_id, cmd[0]);
					}
					record_id++;
					return ret;
				}
			}
		}
		return "Request Failed - We do not have the book";

	}

	public synchronized static String list(String name) {
		StringBuilder sb = new StringBuilder();
		if (!people.containsKey(name)) {
			return "No record found for " + name + "\n";
		}
		ArrayList<Integer> record_ids = people.get(name);
		for (int i = 0; i < record_ids.size(); i++) {
			sb.append(record_ids.get(i));
			
			sb.append(" ");
			sb.append(record_ID.get(record_ids.get(i)));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public synchronized static String inventory () {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0 ; i < curr_lib_list.size(); i++) {
			sb.append(curr_lib_list.get(i).name);
			sb.append(" ");
			sb.append(curr_lib_list.get(i).amount);
			sb.append("\n");
		}
		//sb.append("\0");
		return sb.toString();
		
	}
	
	public synchronized static String return_book (int id) {
		Integer ID = new Integer(id);
		if (record_ID.containsKey(ID)) {
			String book_name = record_ID.get(id);
			record_ID.remove(id);
			String person_name = record_name.get(id);
			record_name.remove(id);
			ArrayList<Integer> hash_list = people.get(person_name);
			hash_list.remove(ID);
			
			Node ret_node = find_node(book_name);
			
			ret_node.amount++;
			
			return id + " is returned";
			
		}
		else {
			return id + " not found, no such borrow record";
		}
		
		
	}
	
	public static Node find_node (String book_name) {
		for (int i = 0 ; i < curr_lib_list.size(); i++ ) {
			if (book_name.compareTo(curr_lib_list.get(i).name) == 0) {
				return curr_lib_list.get(i);
			}
		}
		return null;
	}
	
	public static void send(DatagramPacket returnPacket) {
		try {
			udp_dataSocket.send(returnPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//public static void changeMode(String )
		
}