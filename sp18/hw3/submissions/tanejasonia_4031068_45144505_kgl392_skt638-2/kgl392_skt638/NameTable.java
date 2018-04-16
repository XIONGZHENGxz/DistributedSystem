import java.util.*;
import java.util.Map.Entry;

import javax.rmi.CORBA.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
public class NameTable {
	class NameEntry {
		public String name;
		public InetSocketAddress addr;
		public HashMap<Integer, String> student_books;
		public NameEntry(String pName){
			name = pName;
			student_books = new HashMap<Integer, String>();
		}
	}
	public DatagramSocket datasocket;
	HashMap<String, Integer> inventory;
	ArrayList<String> orderedInventory;
	ArrayList<NameEntry> table = new ArrayList<NameEntry>(); 
	Integer recordID=0;
	boolean udpMode= true;
	
	public NameTable(HashMap<String,Integer> inventory, DatagramSocket datasocket, ArrayList<String> orderedInventory){
  		this.inventory = inventory;
  		this.datasocket = datasocket;
 		this.orderedInventory = orderedInventory;
  	}
	public synchronized Integer borrow(String student, String bookName) {
		for (String book: inventory.keySet()){
			if (book.equals(bookName)){
				//update inventory
				int count = inventory.get(bookName);
				if(count == 0)
					return 0;
				count--;
				inventory.put(bookName, count);	
				recordID++;
				this.insert(student, bookName, recordID);
				return recordID;
			}
		}
		
				return -1;
	}
	public synchronized void insert(String studentName, String bookName, int recordID) {
		for (NameEntry entry: table){
			if (studentName.equals(entry.name)) { 
				entry.student_books.put(recordID, bookName);
				return;
			}
		}
		//student does not exist
		NameEntry temp = new NameEntry(studentName);
		temp.student_books.put(recordID, bookName);
		table.add(temp);
		return;
	}
	public synchronized Integer blockingFind(String procName, String bookName) {
	//	System.out.println("blockingFind " + procName);
		Integer addr = borrow(procName, bookName);
		while (addr == null) {
			addr = borrow(procName, bookName);
		}
		return addr;
	}
	
	public synchronized int returnBook(Integer id){
		for (NameEntry student: table){
			if (student.student_books.containsKey(id)){
				//update student book
				String bookName = student.student_books.get(id);
				student.student_books.remove(id);
				//update inventory
				int count = inventory.get(bookName);
				count++;
				inventory.put(bookName, count);	
				return id;
			}
		}
		return -1;
		
	}
	public synchronized void clear() {
		table.clear();
	}
	
	public synchronized void setMode(boolean udp){
		this.udpMode = udp;
	}
	
	public synchronized void exitProgram(){
        try {
        	FileWriter inventoryOut = new FileWriter("inventory.txt", true);
            String inventoryList = this.printInventory();
			inventoryOut.write(inventoryList);
			inventoryOut.flush();
	        inventoryOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized String printInventory(){
		String retString="";
		boolean found = false;
		// loop through ordered array list that holds all the keys
		 		for (String i : orderedInventory){
		             for (Map.Entry<String, Integer> entry: inventory.entrySet()){
		                 // find specific key value pair
		                 if(i == entry.getKey()){
		                	 retString = retString + "\"" + entry.getKey() + "\" " + entry.getValue() + "@";
		                	 found = true;
		                 }
		             }
		         }
		 		if(found)
		 			retString = retString.substring(0, retString.length()-1);
		 
		return retString;
	}
	
	public synchronized String printList(String studentName){
		String found = "";
		boolean hasPerson = false;
		for(NameEntry entry: table){
			if(entry.name.equals(studentName)){
				for(Map.Entry<Integer, String> e: entry.student_books.entrySet()){
					found = found + e.getKey() + " " +  "\"" + e.getValue() +  "\"" + "@";
					hasPerson= true;
				}
				
			}
		}
		if(hasPerson)
			found = found.substring(0, found.length()-1);
			
			
		return found;
	}
	
	public synchronized void udpSend(DatagramPacket packet){
		try{
			datasocket.send(packet);
		}
		catch(Exception e){
		      e.printStackTrace();
		    }
	}
}