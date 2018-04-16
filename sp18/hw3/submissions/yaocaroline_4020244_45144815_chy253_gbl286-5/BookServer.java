//package hwk3;

import javax.xml.crypto.Data;
import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.*;

public class BookServer {
	private static ArrayList<Book> checkedOut = new ArrayList<Book>(); ///record number of the index
	private static ArrayList<Book> inventory = new ArrayList<Book>();
	private static ArrayList<Student> studentList = new ArrayList<Student>();

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}

		try {
			// parse the inventory file
			String fileName = args[0];
			Scanner sc = new Scanner(new FileReader(fileName));

			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split("\"");
				String[] amount = tokens[2].split(" ");
				int quant = Integer.parseInt(amount[1]);
				Book newItem = new Book(tokens[1], quant);
				inventory.add(newItem);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int tcpPort = 7000;
		int udpPort = 8000;

		// start server udp listener thread
		Thread udp = new UDPThread(udpPort);
		udp.start();

		// start server tcp listener thread
		Thread tcp = new TCPThread(tcpPort);
		tcp.start();
	}

	public static String performCommand(String[] tokens){
		String returnString = new String("");
		if (tokens[0].equals("borrow")) {
			returnString = checkout(tokens);
		} else if (tokens[0].equals("return")) {
			returnString = bookReturn(tokens);
		} else if (tokens[0].equals("inventory")) {
			returnString = getInventory();
		} else if (tokens[0].equals("list")) {
			returnString = getChecked(tokens);
		} else if (tokens[0].equals("exit")) {
			// TODO: send appropriate command to the server
			returnString = exit();
		} else {
			returnString = "ERROR: No such command";
		}
		return returnString;
	}

	public static synchronized String exit(){
		//TODO: inform server to stop processing commands from this client
		try {
			PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");
			String[] out = getInventory().split("\\n");
			for(String s : out){
				writer.println(s);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "client exit";
	}

	public static synchronized String getChecked(String[] tokens) {
		// TODO Auto-generated method stub
		boolean hasRecord = false;
		String name = tokens[1];
		StringBuilder retString = new StringBuilder("");
		int i = 0;
		if (studentList.size() == 0) {
			retString.append("No record found for ");
			retString.append(name);
			return retString.toString();
		}
		while (i < studentList.size() && !studentList.get(i).name.equals(name)) {
			i++;
		}
		if (i >= studentList.size() || !studentList.get(i).name.equals(name)) {
			retString.append("No record found for ");
			retString.append(name);
		} else {
			for (int item = 0; item < studentList.get(i).records.size(); item++) {
				int recordno = studentList.get(i).records.get(item);
				if (checkedOut.get(recordno - 1) != null) {
					hasRecord = true;
					retString.append(String.valueOf(recordno));
					retString.append(" ");
					// skip records of returned books
					if (checkedOut.get(recordno - 1) != null) {
						retString.append(checkedOut.get(recordno - 1).bookname);
					}
					if (item != studentList.get(i).records.size() - 1) {
						retString.append("\n");
					}
				}
			}
			if (!hasRecord) {
				retString.append("No record found for ");
				retString.append(name);
			}
		}

		return retString.toString();
	}

	public static synchronized String checkout(String[] tokens) {
		boolean newStudent = false;
		String name = tokens[1];
		int st = 0;
		Student student;
		while (st < studentList.size() && !studentList.get(st).name.equals(name)) {
			st++;
		}
		if (st < studentList.size() && studentList.get(st).name.equals(name)) {
			student = studentList.get(st);
		} else {
			student = new Student(name);
			newStudent = true;
		}
		StringBuilder retString = new StringBuilder("");
		StringBuilder bookNameBuild = new StringBuilder("");
		String bookName;
		if (tokens[2].contains("\"")) {
			int tokenIndex = 3;
			//not just one word
			if (tokens[2].charAt(tokens[2].length() - 1) != '\"') {
				bookNameBuild.append(tokens[2].substring(1));
				while ((tokenIndex < tokens.length) && (!tokens[tokenIndex].contains("\""))) {
					bookNameBuild.append(" ");
					bookNameBuild.append(tokens[tokenIndex]);
					tokenIndex++;
				}
				if (tokens[tokenIndex].contains("\"")) {
					bookNameBuild.append(" ");
					bookNameBuild.append(tokens[tokenIndex].substring(0, tokens[tokenIndex].length() - 1));
					bookName = bookNameBuild.toString();

					int searchIndex = 0;
					while ((searchIndex < inventory.size()) && !(inventory.get(searchIndex).getName().equals(bookName))) {
						searchIndex++;
					}

					if (((searchIndex < inventory.size()) && (inventory.get(searchIndex).getName().equals(bookName)))) {
						if (inventory.get(searchIndex).getquantity() > 0) {
							//add the book into the checkedOut list
							Book checkedOutBook = inventory.get(searchIndex);
							checkedOut.add(checkedOutBook);
							//update the inventory and student's book list
							int recordNum = checkedOut.size();
							checkedOutBook.bookOut(student, recordNum);
							if(newStudent) studentList.add(student);
							retString.append("Your request has been approved, ");
							retString.append(String.valueOf(recordNum));
							retString.append(" ");
							retString.append(tokens[1]);
							retString.append(" ");
							retString.append('"' + bookName + '"');

							//retString.append(" ("+studentList.size() + " students)");

						} else {
							retString.append("Request Failed - Book not available");
						}
					}
				} else {
					retString.append("Request Failed - Book not available");
				}
			}
			//one word title
			else {
				bookNameBuild.append(tokens[2].substring(1, tokens[2].length() - 1));
				bookName = bookNameBuild.toString();

				int searchIndex = 0;
				while ((searchIndex < inventory.size()) && !(inventory.get(searchIndex).getName().equals(bookName))) {
					searchIndex++;
				}

				if (((searchIndex < inventory.size()) && (inventory.get(searchIndex).getName().equals(bookName)))) {
					if (inventory.get(searchIndex).getquantity() > 0) {
						//add the book into the checkedOut list
						Book checkedOutBook = inventory.get(searchIndex);
						checkedOut.add(checkedOutBook);
						//update the inventory and student's book list
						int recordNum = checkedOut.size();
						checkedOutBook.bookOut(student, recordNum);
						if(newStudent) studentList.add(student);
						retString.append("Your request has been approved, ");
						retString.append(String.valueOf(recordNum));
						retString.append(" ");
						retString.append(tokens[1]);
						retString.append(" ");
						retString.append('"' + bookName + '"');

						//retString.append(" ("+studentList.size() + " students)");
					} else {
						retString.append("Request Failed - Book not available");
					}
				}
			}
		} else {
			retString.append("Request Failed - Book not available");
		}
		return retString.toString();
	}
	// sets returned book in checkedOut list to null object
	public static synchronized String bookReturn(String[] tokens) {
		StringBuilder retString = new StringBuilder("");
		int recordNo = Integer.valueOf(tokens[1]);
		retString.append(String.valueOf(recordNo));
		if(recordNo < 1 || recordNo > checkedOut.size()){
			retString.append(" not found, no such borrow record");
			return retString.toString();
		}
		Book bookRecord = checkedOut.get(recordNo - 1);
		// if book returned already
		if (bookRecord == null) {
			retString.append(" not found, no such borrow record");

//		if (!bookRecord.returnBook(recordNo)) {
//			retString.append(" not found, no such borrow record");
		} else {
			checkedOut.set(recordNo - 1, null);
			for (Student s : studentList) {
				if (s.records.contains(recordNo)) {
					bookRecord.returnBook(s, recordNo);
					// remove student from list if they don't have checked out books
					if (s.records.isEmpty()) studentList.remove(s);
					retString.append(" ");
					retString.append("is returned");
					break;
				}
			}
		}
		return retString.toString();
	}

	public static synchronized String getInventory() {
		StringBuilder retString = new StringBuilder("");
		for (int element = 0; element < inventory.size(); element++) {
			retString.append('"' + inventory.get(element).bookname + '"');
			retString.append(" ");
			retString.append(String.valueOf(inventory.get(element).quantity));
			if (element != inventory.size() - 1) {
				retString.append("\n");
			}
		}
		return retString.toString();

	}

}
