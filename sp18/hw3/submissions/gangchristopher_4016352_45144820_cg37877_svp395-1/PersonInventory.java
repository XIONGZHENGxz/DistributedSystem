import java.util.*;
import java.io.*;

public class PersonInventory {
	private ArrayList<String> names;
	private ArrayList<Integer> recordIDs;
	private String name;

	public PersonInventory(String name) {
		this.name = name;
		this.names = new ArrayList<String>();
		this.recordIDs = new ArrayList<Integer>();
	}

	// Person returns record ID to book inventory
	void returnBook(BookInventory bookInventory, String fileName, int recordID) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

			int index = recordIDs.indexOf(recordID);
			if (containsRecordID(recordID))
			{
				out.println(recordID + " is returned");
				bookInventory.returnBook(names.get(index));
				recordIDs.remove(index);
				names.remove(index);
			} else {
				out.println(recordID + " is not found");
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Person borrows book from book inventory
	boolean borrowBook(BookInventory bookInventory, String bookName, int recordID, String fileName) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

			if (bookInventory.getQuantity(bookName) > 0) {
				bookInventory.borrowBook(bookName);
				names.add(bookName);
				recordIDs.add(recordID);
				out.println("Your request has been approved, " + recordID + " " + this.name + " \"" + bookName + "\"");
				out.close();
				return true;
			} else if (bookInventory.getQuantity(bookName) == 0) {
				out.println("Request Failed - Book not available");
				out.close();
				return false;
			} else if (bookInventory.getQuantity(bookName) == -1) {
				out.println("Request Failed - We do not have this book");
				out.close();
				return false;
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// Write to output file
	void listBooks(String fileName) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

			for (int i = 0; i < names.size(); i++) {
				out.println(recordIDs.get(i) + " \"" + names.get(i) + "\"");
			}

			if (names.size() == 0) {
				out.println("No record found for " + name);
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String getName() {
		return name;
	}

	boolean containsRecordID(int recordID) {
		return recordIDs.indexOf(recordID) != -1;
	}
}