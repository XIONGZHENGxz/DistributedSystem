import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TCPWorker extends Thread {
	private Socket client;
	private String clientCommand;
	RecordLog log;
	BookInventory Inventory;
	
	public TCPWorker(Socket c, String cmd, RecordLog log, BookInventory Inventory) {
		client = c;
		clientCommand = cmd;
		this.log = log;
		this.Inventory = Inventory;
	}
	
	public void run() {
		String[] tokens = clientCommand.split(" ");
		PrintStream ps;
		String returnMessage = "";
		// fulfill the client's request and then send a message back to client

		if (tokens[0].equals("borrow")) {
			String name = tokens[1];
			String book = "";
			for(int i = 2; i < tokens.length; i++){
				book = book.concat(tokens[i] + " ");
			}
			String book1 = book.substring(1, book.length()-2);
			book = book.trim();
			boolean borrow = Inventory.checkout(book1);
			if(borrow){
				log.add(name, book1, log.getLength() + 1);
				returnMessage = "Your request has been approved, " + log.getLength() + " " + name + " " + book + "\n";
			}
			else{
				returnMessage = "Request Failed - We do not have this book" + "\n";
			}
		}

		else if (tokens[0].equals("return")) {
			int id = Integer.valueOf(tokens[1]);
			boolean returned = log.returnBook(id);
			if(returned){
				Inventory.returnBook(log.getBook(id));
				returnMessage = (id + " is returned"+ "\n");
			}
			else{
				returnMessage = (id + " not found, no such borrow record"+ "\n");
			}

		}

		else if (tokens[0].equals("inventory")) {
			for(Book a : Inventory.list){
				returnMessage = returnMessage.concat("\"" + a.getName() + "\" " + a.getAmount() + "\n");
			}
			returnMessage.trim();
		}

		else if (tokens[0].equals("list")) {
			String name = tokens[1];
			ArrayList<Record> list = log.userOrders(name);
			if(list.size() == 0){
				returnMessage = "‘No record found for " + name + "\n";
			}
			else{
				for(Record a : list){
					returnMessage = returnMessage.concat(a.getId() + " " + "\"" + a.getBook() + "\" " + "\n");
				}
			}

		}

		else if (tokens[0].equals("exit")) {
			String filename = "Inventory.txt";
			Writer writer;
			for(Book a : Inventory.list){
				returnMessage = returnMessage.concat("\"" + a.getName() + "\" " + a.getAmount() + "\n");
			}
			returnMessage.trim();
			try {
				Files.write(Paths.get(filename), returnMessage.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			ps = new PrintStream(client.getOutputStream(), true);
			ps.print(returnMessage);
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	}





