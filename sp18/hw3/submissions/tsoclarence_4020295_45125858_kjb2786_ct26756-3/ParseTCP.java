import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Scanner;

class ParseTCP implements Runnable{
	String command;
	Thread t;
	Socket socket;
	PrintWriter pout;
	Scanner in;

	public ParseTCP( Socket data) throws IOException{
		socket = data;
		pout = new PrintWriter(socket.getOutputStream());
		in = new Scanner(socket.getInputStream());

	}
	
	public void sendToClient(Socket socket, String message) throws IOException{
		pout.println(message);
		pout.flush();
		
	}
	  
	@Override
	public void run() {
		boolean go = true;
		while(go){
			String command= in.nextLine();
			String[] tokens = command.split(" ");

	
		  if (tokens[0].equals("borrow")) {
			  String[] isolateTitle = command.split("\"");
			  String title = isolateTitle[1];
			  title = "\"" + title + "\"";
			  String name = tokens[1];
			  String reply ="";
			  Transaction trans = new Transaction(name, title);
			  
			  try {
				int status = BookServer.borrowBook(trans);
				if(status == -1){
					//send book does not exist UDP
					reply = "Request Failed - We do not have this book";
					sendToClient(socket, "1\n" +reply);					
				}
				else if(status == -2){
					//send book out of stock UDP
					reply=("Request Failed - Book not available");
					sendToClient(socket, "1\n" +reply);
				}
				else{
					//send transaction success UDP
					reply=("Your request has been approved, " + status + " " + name + " " + title);
					sendToClient(socket, "1\n" +reply);
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			  
		    // TODO: send appropriate command to the server and display the
		    // appropriate responses form the server
		  } else if (tokens[0].equals("return")) {
			  int transID = Integer.parseInt(tokens[1]);
			  Transaction trans = BookServer.transactions.get(transID);
			  
			  String reply ="";
			  
			  try {
				int status = BookServer.returnBook(trans, transID );
				
				if(status == -1){
					reply = (tokens[1] + "  not found, no such borrow record");
					sendToClient(socket, "1\n" +reply);
				}
				else{
					reply = (tokens[1] + " is returned");
					sendToClient(socket, "1\n" +reply);
				}
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			  
			  
		  } else if (tokens[0].equals("inventory")) {
		    try {
				String reply = BookServer.printInventory();
			    sendToClient(socket, reply);

			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		    
		  } else if (tokens[0].equals("list")) {
			  String name = tokens[1];
			  try {
				String reply = BookServer.printCheckedOut(name);
				sendToClient(socket, reply);
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
		    
		  } else if (tokens[0].equals("exit")) {
			  try {
				Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("inventory.txt"), "utf-8"));
				writer.write(BookServer.printInventory());
				go = false;
				String reply = "0";
				sendToClient(socket,reply);
				String fileName = "inventory.txt";
				File file = new File(fileName);
				PrintWriter poutFile = new PrintWriter(new FileWriter(file));
				reply = BookServer.printInventory();
				String[] newReply = reply.split("\n");
				
				String sendReply = "";
				for(int i = 1; i < newReply.length; i++){
					if(i != newReply.length -1){
						sendReply += (newReply[i]+"\n");		
					}
					else{
						sendReply += (newReply[i]);
					}
				}
				poutFile.println(sendReply);
				poutFile.flush();
				return;
				
				
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  } else {
			 System.out.println("ERROR: No such command");
		  }
		}
		
		
	}
	
}