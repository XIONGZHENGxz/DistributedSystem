

import java.util.*;
import java.io.*;
import java.net.*;


public class Librarian implements Runnable{


	// Mark: - Properties

	// the Library object
	private Library library;
	// socket for TCP connection
	private Socket socket;
	//
	InputStream inputStream;
	//
	OutputStream outputStream;
	
	// Mark: - Constructors

	Librarian(Library library, Socket socket) {
		this.socket = socket;
//		try {
//			this.inputStream = this.socket.getInputStream();
//			this.outputStream = this.socket.getOutputStream();
//		} catch (IOException e) { e.printStackTrace(); }
		this.library = library;
	}


	// Mark: - Override Methods

	@Override
	public void run() {
		Scanner scanner = null;
		PrintWriter outputWriter = null;
		String message = "";
		String[] tokens;
		String fix = "";
		try {
			scanner = new Scanner(socket.getInputStream());
			outputWriter = new PrintWriter(socket.getOutputStream());
			
			while (true) {
			    if (scanner.hasNextLine()) {
//		    		while(scanner.hasNext()) {
//		    			message = message + scanner.next() + " ";
//		    		}
//		    		fix = message.substring(0, message.length() - 1); // take out the extra space
			    	message = scanner.nextLine();
					tokens = message.split(" ");
					System.out.println("TCP: " + message);
					//System.out.println("Librarian: " + processCommand(tokens));
					outputWriter.println(processCommand(tokens));
					outputWriter.flush(); // change by Patrick
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


	// Mark: - Private Implementation

	/**
	 * TCP
	 * @param tokens
	 */
	private String processCommand(String[] tokens) {
		// parsing
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
			// exit the thread
			return library.getInventory();
			//System.exit(-1);
		}
		
		return "";
	}

}
