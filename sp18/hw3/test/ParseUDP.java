import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class ParseUDP implements Runnable{
	String command;
	Thread t;
	DatagramSocket datasocket;
	int clientId;
	boolean FirstMessage = true;
	int port;
	DatagramPacket dataPacket;
	InetAddress responseAddress;
	int returnPort;

	
	public ParseUDP(String cmd, InetAddress returnaddress, int returnport) throws SocketException{
		command = cmd;
		String[] tokens = command.split(" ");
		int index = (tokens.length) -1;
		String temp = tokens[index].trim();
		clientId = Integer.parseInt(temp);
		
		this.returnPort = returnport;

		datasocket = new DatagramSocket(8000 + clientId);
		


		
	}
	
	public void sendToClient(String message) throws IOException{
		InetAddress IPAddress = InetAddress.getByName("localhost");		
		DatagramPacket returnPacket = new DatagramPacket(message.getBytes(), message.getBytes().length,IPAddress, returnPort);
		datasocket.send(returnPacket);
		

	}
	  
	@Override
	public void run() {
		boolean go = true;
		while(go){			
			String[] tokens = null;
			if(FirstMessage == true){
				tokens = command.split(" ");
				FirstMessage = false;

			}
			else{
				byte[] buf = new byte[1024];
				dataPacket = new DatagramPacket(buf, buf.length);
				try {
					datasocket.receive(dataPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				command = new String(dataPacket.getData());
				tokens = command.split(" ");
				
			}
			
	
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
					sendToClient(reply);					
				}
				else if(status == -2){
					//send book out of stock UDP
					reply=("Request Failed - Book not available");
					sendToClient(reply);
				}
				else{
					//send transaction success UDP
					reply=("Your request has been approved, " + status + " " + name + " " + title);
					sendToClient(reply);
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
					sendToClient(reply);
				}
				else{
					reply = (tokens[1] + " is returned");
					sendToClient(reply);
				}
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			  
			  
		  } else if (tokens[0].equals("inventory")) {
		    try {
				String reply = BookServer.printInventory();
				reply = reply.substring(2, reply.length());
				/*
				String[] newReply = reply.split("\n");
				
				String sendReply = "";
				for(int i = 1; i < newReply.length; i++){
					if(i != newReply.length -1){
						sendReply += (newReply[i]+"\n");		
					}
					else{
						sendReply += (newReply[i] + "\n");
					}
				}
				*/
				sendToClient(reply);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		    
		  } else if (tokens[0].equals("list")) {
			  String name = tokens[1];
			  try {
				String reply = BookServer.printCheckedOut(name);
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
				
				sendToClient(sendReply);
				
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
		    
		  } else if (tokens[0].equals("exit")) {
			  try {
				//Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("inventory.txt"), "utf-8"));
				//writer.write(BookServer.printInventory());
				String reply = "0";
				sendToClient(reply);
				go = false;
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
				

				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  } else {
			 System.out.println("ERROR: No such command");
		  }
		}
	
		
	}
		
	
	
}