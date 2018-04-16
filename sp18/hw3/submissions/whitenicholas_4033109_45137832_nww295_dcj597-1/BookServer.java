//nww295
import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.util.*;
import java.net.*;

public class BookServer {
	public static LinkedHashMap<String, Integer> bookItems = new LinkedHashMap<>();
	public static int tcpPort = 7000;
	public static int udpPort = 8000;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("ERROR: Provide arguments:");
			System.out.println("\t(1) inventory text file");
			System.exit(-1);
		}

		String inventory = args[0];
		// parse input text file
		try {
			FileReader fileRead = new FileReader(inventory);
			BufferedReader buffRead = new BufferedReader(fileRead);
			String newLine = null;
			String[] lineBook = new String[2];
			while((newLine = buffRead.readLine()) != null) {
				newLine = newLine.trim();
				lineBook = newLine.split("\"");
				lineBook[2] = lineBook[2].trim();
				if(lineBook.length == 3) {
					bookItems.put(lineBook[1], Integer.valueOf(lineBook[2]));
				}
			}
			buffRead.close();
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			e.printStackTrace();
		}


		// init UDP thread
		Thread udpThread = new UDPServer();
		udpThread.start();
		
		// init TCP thread
		
		Thread tcpThread = new TCPServer();
		tcpThread.start();
	
	}



}



		
