import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;
import java.util.*;

public class BookServer {
	
	private static Inventory lib; 
	private static RecordList rec;

	public static void main (String[] args) {

		int tcpPort;
		int udpPort;
		Scanner sc;

		if (args.length != 1) {
      		System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      		System.exit(-1);
    	}

		lib = new Inventory();
		rec = new RecordList();
    	String fileName = args[0];
    	tcpPort = 7000;
    	udpPort = 8000;

    	// parse the inventory file
		try {
			sc = new Scanner(new FileReader(fileName));
			String input;
			String[] tokens; 
			String bookName;

			while (sc.hasNextLine())
			{
				input = sc.nextLine();	
				tokens = input.split(" "); 

				if(tokens.length < 2)
				{
					System.out.println("Error in inventory file");
					//System.out.println(input);
					//System.out.println(tokens[0] + "\n" + tokens[1] + "\n" + tokens[2]);
					System.exit(-1);
				}	

				bookName = tokens[0];
				for (int i = 1; i < tokens.length-1; i++) {
					bookName += " " + tokens[i];
				}

				lib.add(bookName, Integer.parseInt(tokens[tokens.length - 1]));	
			}

			//lib.list();			

		} catch (Exception e) {
			e.printStackTrace();
		}

    	// TODO: handle request from clients
		//System.out.println("Book Server Online:");
		(new TCPListenerThread(lib, rec)).start();		
		(new UDPListenerThread(lib, rec)).start();
  	}
}
