import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class UDPServerThread implements Runnable {

	int portNumber;	
	BookServer bs;
	
	DatagramPacket datapacket, returnpacket;
	DatagramSocket datasocket;
	
	String serverMessage;
	UDPServerThread(BookServer bs, String s, DatagramSocket datasocket, DatagramPacket datapacket) {
		portNumber = bs.udpPort;
		this.serverMessage =  s;
		this.datasocket = datasocket; 
		this.datapacket = datapacket;
		this.bs = bs;
	}

	@Override
	public void run() {
		
		try {
			ArrayList<String> returnStrings = new ArrayList<>();
			parseCommands(serverMessage, returnStrings);
			
			StringBuilder returnString = new StringBuilder();
			
			for (int i = 0; i < returnStrings.size(); i++) {
				returnString.append(returnStrings.get(i));
			}
			
			byte[] returnStringArr = new byte[returnString.length()];
			returnStringArr = returnString.toString().getBytes();
		
			returnpacket = new DatagramPacket(returnStringArr, returnStringArr.length, datapacket.getAddress(),
					datapacket.getPort());
			datasocket.send(returnpacket);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void parseCommands(String serverMessage, List<String> returnStrings) {
		String[] commands = serverMessage.split(" ");
		String command = commands[0];
		
		if (command.equals("setmode")) {

		}
		else if (command.equals("borrow")) {
			String userName = commands[1];
			String[] tempArray = serverMessage.split("\"");
			String bookTitle = tempArray[1];
			bs.syncBorrow(userName, bookTitle, returnStrings);
		}
		else if (command.equals("return")) {
			int recordId = Integer.valueOf(commands[1]);
			bs.syncReturn(recordId, returnStrings);
		}
		else if (command.equals("list")) {
			String userName = commands[1];
			bs.syncList(userName, returnStrings);
		}
		else if (command.equals("inventory")) {
			bs.syncInventory(returnStrings);
		}
		else if (command.equals("exit")) {
			bs.syncExit(returnStrings);
		}
	}
}