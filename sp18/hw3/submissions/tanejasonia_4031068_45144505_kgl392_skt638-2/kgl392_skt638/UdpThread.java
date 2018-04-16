import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public class UdpThread extends Thread {
	DatagramPacket packet;
	NameTable table;
	boolean udpMode;
	DatagramSocket datasocket;
	int udpPort = 8000;

	
	public UdpThread( NameTable table){
		this.table = table;
	}
	
	public void run(){
		try {
			datasocket = new DatagramSocket(udpPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileWriter out = null;
		while(true){
		byte[] buf = new byte[6000]; 
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
			datasocket.receive(packet);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        String command = new String(packet.getData());
		String retString = "";
		String tokens[] = command.split(" ", 3);
		tokens[0] = tokens[0].trim();
		if (tokens[0].equals("setmode")) {
			if(tokens[1].trim().equals("T"))
				table.setMode(false);
			else
				table.setMode(true);
			
		} else if (tokens[0].equals("borrow")) {
			tokens[1] = tokens[1].trim();
			tokens[2] = tokens[2].trim();
			String arg2 = tokens[2].substring(0, tokens[2].lastIndexOf("\"")+1);
			arg2 = arg2.replaceAll("\"", "");
			int retVal = table.borrow(tokens[1], arg2);
			//System.out.println(retVal);
			if(retVal == 0)
				retString = "Request Failed - Book not available";
			else if (retVal == -1)
				retString= "Request Failed - We do not have this book";
			else
				retString = "Your request has been approved, " +retVal + " " + tokens[1].trim() + " " + tokens[2].trim() ;
		} else if (tokens[0].equals("return")) {                  
			int val = Integer.parseInt(tokens[1].trim());
			int retVal = table.returnBook(val);
			if(retVal == -1)
				retString = val + " not found, no such borrow record";
			else
				retString=val + " is returned";
			
		} else if(tokens[0].equals("inventory")){
			retString = table.printInventory();
		} else if (tokens[0].equals("list")){
			retString = table.printList(tokens[1].trim());
			if(retString.equals("")){
				retString="No record found for " + tokens[1].trim();
			}
		} else if (tokens[0].equals("exit")){
			retString = table.printInventory();
		}
		//System.out.println(retString);
		DatagramPacket returnPacket = new DatagramPacket(retString.getBytes(), retString.getBytes().length, 
		        packet.getAddress(), packet.getPort());
		    try {
				datasocket.send(returnPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//theClient.close();
	}

}
