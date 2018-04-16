import java.io.IOException;
import java.net.*;

public class UdpServer extends Thread {
	private DatagramSocket socket;
	

	public UdpServer() {
		try {
			socket = new DatagramSocket(8000);
		} catch(Exception e) {
			e.printStackTrace();
		} 
	}

	public void run() {


		while(true) {
			try {
				byte[] buf = new byte[4096];
				//System.out.println("here8");
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				//packet = new DatagramPacket(buf, buf.length, address, port);
				String received = new String(packet.getData(), 0, packet.getLength());
				//System.out.println("here9");
				String[] commandTokens = received.split(":");
				String return_message = ""; 
				//System.out.println(received);
				switch(commandTokens[0]) {
					case "borrow":
						//System.out.println("borrow");
					
						int recordID = BookServer.borrow(commandTokens[1], commandTokens[2]);
						//os.println("1");
						if(recordID != -1) {
							//request approved
							return_message = "Your request has been approved, " + recordID + " " + commandTokens[1] + " " + commandTokens[2];
					
						} else {
							//request denied
							return_message ="Request Failed - Book not available";
						}
						return_message += "\n";
						break;
					case "return":
						//System.out.println("return");
						boolean response = BookServer.returnBook(Integer.valueOf(commandTokens[1]));
						//os.println("1");
						if(response) {
							//System.out.println(commandTokens[1] + " is returned");
							return_message =commandTokens[1] + " is returned";
						} else {
							//System.out.println(commandTokens[1] + " not found, no such borrow record");
							return_message =commandTokens[1] + " not found, no such borrow record";
						}
						return_message += "\n";
						break;
					case "list":
						//System.out.println("list");
						//return_message = BookServer.getListLength(commandTokens[1]);
						return_message = BookServer.printList(commandTokens[1]);
						break;
					case "inventory":
						//os.println(BookServer.getInventoryLength());						
						return_message = BookServer.printInventory();
						//System.out.println("inventory");
						break;
					case "exit":
				
						//os.println("1");
						//System.out.println("exit");
						break;
				}
				//System.out.println("here0");
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
//				if(!commandTokens[0].equals("list"))
//					return_message += "\n";
				buf = return_message.getBytes();
		        DatagramPacket return_packet 
		          = new DatagramPacket(buf, buf.length, address, port);
		       // System.out.println("here2");
				//System.out.println(received);
				socket.send(return_packet);
			} catch(Exception e) {
				e.printStackTrace();
			}

		}
	}
}