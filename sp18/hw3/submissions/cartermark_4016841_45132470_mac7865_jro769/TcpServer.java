import java.io.*;
import java.net.*;

public class TcpServer extends Thread {
	ServerSocket socket = null;
	String line;
	BufferedReader is;
	PrintStream os;
	Socket clientSocket = null;

	public TcpServer() {
		try {
			socket = new ServerSocket(7000);
		} catch(IOException e) {
			System.out.println(e);
		}
	}

	public void run() {
		while(true) {
			try {
				clientSocket = socket.accept();
		        is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				os = new PrintStream(clientSocket.getOutputStream());
				boolean running = true;
				synchronized(this) {
					while(running) {
						line = is.readLine();
						String[] commandTokens = line.split(":");
						switch(commandTokens[0]) {
							case "borrow":
								//System.out.println("borrow");
								int recordID = BookServer.borrow(commandTokens[1], commandTokens[2]);
								os.println("1");
								if(recordID != -1) {
									//request approved
									os.println("Your request has been approved, " + recordID + " " + commandTokens[1] + " " + commandTokens[2]);
								} else {
									//request denied
									os.println("Request Failed - Book not available");
								}
								break;
							case "return":
								//System.out.println("return");
								boolean response = BookServer.returnBook(Integer.valueOf(commandTokens[1]));
								os.println("1");
								if(response) {
									//System.out.println(commandTokens[1] + " is returned");
									os.println(commandTokens[1] + " is returned");
								} else {
									//System.out.println(commandTokens[1] + " not found, no such borrow record");
									os.println(commandTokens[1] + " not found, no such borrow record");
								}
								break;
							case "list":
								os.println(BookServer.getListLength(commandTokens[1]));
								//os.println("1");
								os.print(BookServer.printList(commandTokens[1]));
								break;
							case "inventory":
								os.println(BookServer.getInventoryLength());						
								os.print(BookServer.printInventory());
								//System.out.println("inventory");
								break;
							case "exit":
								os.println("0");
								BookServer.outputInventory();
								//os.println("exit");
								//System.out.println("exit");
								running = false;
								break;
						}
						//os.println(line);
					}
				}
			} catch(Exception e) {
				System.out.println(e);
			}
		}
	}
}