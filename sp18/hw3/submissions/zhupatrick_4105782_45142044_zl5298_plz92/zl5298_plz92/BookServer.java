import java.util.*;
import java.io.*;
import java.net.*;

public class BookServer {
	/* Questions
	 * 1) does the inventory need to be in the same order?
	 * 2) student checking out multiple copies of the same book? piazza says yes
	 * 3) students with the same name but are actually different people?
	 */

	// TCP port number
	private static int TCPPort = 7000;
	// UDP port number
	private static int UDPPort = 8000;
	// the Library object
	private static Library library = new Library();


	public static void main (String[] args) {

		// check argument length
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}

		// break down a line into tokens
		String[] tokens;	


		/********** Parse Input File **********/

		try { 
			Scanner scanner = new Scanner(new File(args[0])); 
			// iterate through the file
			while(scanner.hasNextLine()) {
				// get next line and split by " to obtain book name
				tokens = scanner.nextLine().split("\"");
				// eliminate space by using substring
				library.putBook(tokens[1], Integer.parseInt(tokens[2].substring(1)));	
			}
		} 
		catch (FileNotFoundException e) { e.printStackTrace(); }

		
		/********** TCP Listener **********/
		Thread listennerThread = new Thread( new Listener(library) );
		listennerThread.start();

		
		/********** UDP **********/

		// variable to hold a line of command
		String commandLine;
		//
		DatagramPacket dataPacket, returnPacket;
		//
		byte[] packageBuffer = new byte[1024];
		//
		byte[] returnMessage;	
		try {
			@SuppressWarnings("resource") // we dont wanna close it
			DatagramSocket datagramSocket = new DatagramSocket(UDPPort);
			// 
			while (true) {
				dataPacket = new DatagramPacket(packageBuffer, packageBuffer.length);
				datagramSocket.receive(dataPacket);			
				commandLine = new String(dataPacket.getData(), 0, dataPacket.getLength());
				System.out.println("UDP: " + commandLine);
				tokens = commandLine.split(" ");
				// process input and get return message
				returnMessage = processCommand(tokens).getBytes();
				// construct return UDP packet
				returnPacket = new DatagramPacket(
						returnMessage, 
						returnMessage.length, 
						dataPacket.getAddress(), 
						dataPacket.getPort()
						);
				datagramSocket.send(returnPacket);
			}
		} catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * UDP
	 * @param tokens
	 */
	private static String processCommand(String[] tokens) {
		// parsing
//		if(tokens[0].equals("setmode")) {
//			if(tokens[1].equals("U")) {
//				return "U";
//			}
//			else {
//				return "T";
//			}
//		}
		if (tokens[0].equals("borrow")) {
			// parse book name
			String bookName = "";
			for(int i = 2; i < tokens.length; i++) bookName = bookName + tokens[i] + " ";
			// take away the quotes on both sides and the space at the end
			bookName = bookName.substring(1, bookName.length() - 2);
			// try borrow the book and print the result message
			return library.borrowBook(tokens[1], bookName);
		} else if (tokens[0].equals("return")) {
			// try return this record and print out the result message
			return library.returnBook(Integer.parseInt(tokens[1]));
		} else if (tokens[0].equals("inventory")) {
			// print out inventory info
			return library.getInventory();
		} else if (tokens[0].equals("list")) {
			// try print out the student record
			return library.getStudentRecord(tokens[1]);
		} else if (tokens[0].equals("exit")) {
			return library.getInventory();
		}
		
		return "";
	}
}