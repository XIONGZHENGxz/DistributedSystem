import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {	
	static Boolean DEBUG = false;
	static AtomicInteger recID = new AtomicInteger(1);
	int tcpPort;
	int udpPort;
	HashMap<String, Integer> libraryDatabase = new HashMap<>();
	HashMap<String, List<String>> userDatabase = new HashMap<>();
	HashMap<Integer, List<String>> recordDatabase = new HashMap<>();
	ArrayList<String> bookOrdering = new ArrayList<>();		
	HashMap<String, Thread> threadMap = new HashMap<>();
	
	public static void main(String[] args) {
		BookServer bs = new BookServer();				
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}
		
		String fileName = args[0];
		bs.tcpPort = 7000;
		bs.udpPort = 8000;

		// parse the inventory file
	
		bs.parseInventoryFile(fileName, bs.libraryDatabase, bs.bookOrdering);
		
		
		if (DEBUG) {
			System.out.println(bs.libraryDatabase.toString());
		}
				
		UDPListener UDPListener = new UDPListener(bs);
		Thread t1 = new Thread(UDPListener);
		t1.start();
		
		TCPListener TCPListener = new TCPListener(bs);
		Thread t2 = new Thread(TCPListener);
		t2.start();
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void parseInventoryFile(String filename, HashMap<String, Integer> database, List<String> bookOrdering) {
		File file = new File(filename);
		
		try {
			Scanner sc = new Scanner(file);
			
			while (sc.hasNext()) {
				String entry = sc.nextLine();
				String[] entryArray = entry.split("\"");
				
				String title = entryArray[1].trim();
				Integer count = Integer.valueOf(entryArray[2].trim());
				
				database.put(title, count);
				bookOrdering.add(title);
			}
			sc.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public synchronized void syncBorrow(String userName, String bookTitle, List<String> returnStrings) {
		boolean bookExists = libraryDatabase.containsKey(bookTitle);
		boolean hasCopy = false;
		
		if (bookExists) {
			hasCopy = libraryDatabase.get(bookTitle) != 0;
		}
		
		// CASE: book exists and there is a copy
		if (bookExists && hasCopy) {
			int rec = BookServer.recID.getAndIncrement(); 
			
			if (userDatabase.containsKey(userName)) {
				List<String> tempList = userDatabase.get(userName);
				tempList.add(rec + " \"" + bookTitle + "\"");
			}
			else {
				ArrayList<String> tempList = new ArrayList<>();
				tempList.add(rec + " \"" + bookTitle + "\"");
				userDatabase.put(userName, tempList);
			}
			
			ArrayList<String> nameAndTitlePair = new ArrayList<>(2);
			nameAndTitlePair.add(userName);
			nameAndTitlePair.add(bookTitle);
			
			recordDatabase.put(rec, nameAndTitlePair);				
			
			libraryDatabase.put(bookTitle, libraryDatabase.get(bookTitle) - 1);
			
			returnStrings.add("Your request has been approved, " + rec + " " + userName + " \"" + bookTitle + "\"\n");
		}
		// CASE: book doesn't exist in the library SHOULD RETURN FAILURE
		else if (!bookExists){
			returnStrings.add("Request Failed - We do not have this book\n");
		}
		// CASE: book exists but there is not an available copy SHOULD RETURN FAILURE
		else if (bookExists && !hasCopy){
			returnStrings.add("Request Failed - Book not available\n");
		}
	}
	
	public synchronized void syncReturn(int recordId, List<String> returnStrings) {
		if (recordDatabase.containsKey(recordId)) {
			List<String> nameAndTitlePair = recordDatabase.get(recordId);
			String userName = nameAndTitlePair.get(0);
			String title = nameAndTitlePair.get(1);
			
			List<String> userBookList = userDatabase.get(userName);
			String target = recordId + " \"" + title + "\"";
			int index = 0;
			for (int i = 0; i < userBookList.size(); i++) {
				if (target.equals(userBookList.get(i))) {
					index = i;
					break;
				}
			}
			userBookList.remove(index);
			recordDatabase.remove(recordId);
			
			// add book back to database
			libraryDatabase.put(title, libraryDatabase.get(title) + 1);
			returnStrings.add(recordId + " is returned\n");
		}
		
		else {
			returnStrings.add(recordId + " not found, no such borrow record\n");
		}
		
		
	}
	
	public synchronized void syncInventory(List<String> returnStrings) {
		for (int i = 0; i < bookOrdering.size(); i++) {
			String bookName = bookOrdering.get(i);
			int bookCount = libraryDatabase.get(bookName);
			returnStrings.add("\"" + bookName + "\" " + bookCount + "\n");
		}
	}
	
	public synchronized void syncList(String userName, List<String> returnStrings) {
		if (userDatabase.containsKey(userName) && userDatabase.get(userName).size() > 0) {
			List<String> tempList = userDatabase.get(userName);
			for (int i = 0; i < tempList.size(); i++) {
				returnStrings.add(tempList.get(i) + "\n");
			}
		}
		else {
			returnStrings.add("No record found for " + userName + "\n");
		}
	}
	
	public synchronized void syncExit(List<String> returnStrings) {
		for (int i = 0; i < bookOrdering.size(); i++) {
			String bookName = bookOrdering.get(i);
			int bookCount = libraryDatabase.get(bookName);
			returnStrings.add("\"" + bookName + "\" " + bookCount + "\n");
		}
		
		StringBuilder returnString = new StringBuilder();
		
		for (int i = 0; i < returnStrings.size(); i++) {
			returnString.append(returnStrings.get(i));
		}
		
		try {
			String filename = "inventory.txt";
		    FileWriter fw = new FileWriter(filename,false); //the true will append the new data
			fw.write(returnString.toString());
			fw.close();
		} catch(IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	}
}