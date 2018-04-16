/*
* Katelyn Ge: kbg488
* Vidita Dixit: vd4282
 */


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;

public class BookServer {


    static HashMap<String, Integer> inventory = new HashMap<String, Integer>();
    static ArrayList<String> inventoryOrder = new ArrayList<String>();
    static int requestID = 1;
    static HashMap<Integer, String> borrowRecord = new HashMap<Integer, String>();
    static ArrayList<Integer> borrowOrder = new ArrayList<Integer>();

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
	    DatagramPacket datapacket, returnpacket;
	    
	    try {
	    	FileReader fileReader = new FileReader(new File(fileName));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] split = line.split(" ");
				String title = "";
				for (int i = 0; i < split.length-2; i++) {
					title += split[i] + " ";
				}
				title += split[split.length-2];
				int quantity = Integer.parseInt(split[split.length-1]);
				inventory.put(title, quantity);
				inventoryOrder.add(title);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    // done parsing inventory file

        //instantiate and run UDP thread here
        UDP udpThread = new UDP();
        udpThread.start();

        //searching for TCP connections
        try {
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;
            while ((s = listener.accept()) != null) {
                TCP tcpThread = new TCP(s);
                tcpThread.start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
	}

	//synchronized methods to borrow/return/list from inventory and display current inventory
    public synchronized static String borrowBook(String student, String book){
	    String response = "";
	    if (inventory.containsKey(book)) {
	    	if (inventory.get(book) == 0) {
	    		response = "Request Failed - Book not available";
	    	} else {
	    		inventory.put(book, inventory.get(book)-1);
	    		borrowRecord.put(requestID, student + " " + book);
	    		borrowOrder.add(requestID);
	    		response = "Your request has been approved, " + requestID + " " + student + " " + book;
	    		requestID += 1;
	    	}
	    } else {
	    	response = "Request Failed - We do not have this book";
	    }
	    return response;
    }

    public synchronized static String returnBook(int id){
        String response = "";
        if (borrowRecord.containsKey(id)) {
        	String[] tokens = borrowRecord.get(id).split(" ", 2);
        	if (inventory.containsKey(tokens[1])) {
        		inventory.put(tokens[1], inventory.get(tokens[1])+1);
        	} else {
        		inventory.put(tokens[1], 1);
        	}
        	borrowRecord.remove(id);
        	for (int i = 0; i < borrowOrder.size(); i++) {
        		if (borrowOrder.get(i) == id) {
        			borrowOrder.remove(i);
        			break;
        		}
        	}
        	response = id + " is returned";
        } else {
        	response = id + " not found, no such borrow record";
        }
        return response;
    }

    public synchronized static String listRecord(String name){
        String response = "";
        String nameTrim = name.trim();
        boolean records = false;
        for (int i = 0; i < borrowOrder.size(); i++) {
        	int id = borrowOrder.get(i);
        	String record = borrowRecord.get(id);
        	if (record.contains(nameTrim)) {
        		records = true;
        		String[] tokens = record.split(" ", 2);
        		response += id + " " + tokens[1] + "\n";
        	}
        }
        
        response = response.trim();

        if (!records) {
        	response = "No record found for " + nameTrim;
        }
        
        return response;
    }

    public synchronized static String getInventory(){
	    String response="";
        for (int i = 0; i < inventoryOrder.size(); i++) {
            String title = inventoryOrder.get(i);
            int quantity = inventory.get(title);
            response += title + " " + quantity + "\n";
        }
        response = response.trim();
        return response;
    }
}