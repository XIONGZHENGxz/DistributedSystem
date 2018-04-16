import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpController implements Runnable {
	
	DatagramPacket packet;
	DatagramSocket server;
	Library library;
	
	public UdpController(DatagramPacket _packet, DatagramSocket _server, Library _library) {
		packet = _packet;
		server = _server;
		library = _library;
	}

	@Override
	public void run() {
		String command = new String(packet.getData(), 0, packet.getLength());
		String[] tokens = StringUtils.quoteSplit(command);
		
		String message = "";
        if (tokens[0].equals("borrow")) {
        	message = library.borrowBook(tokens[1], tokens[2]);
        } else if (tokens[0].equals("return")) {
        	message = library.returnRecordId(Integer.parseInt(tokens[1]));
        } else if (tokens[0].equals("inventory")) {
        	message = library.inventory();
        } else if (tokens[0].equals("list")) {
        	message = library.list(tokens[1]);
        } else if (tokens[0].equals("exit")) {
        	try {
        		message = library.exit();
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        } else {
        	message = "Invalid command.";
        }
        
        byte[] bytes = message.getBytes();
        DatagramPacket datapacket = new DatagramPacket(bytes, bytes.length, packet.getAddress(), packet.getPort());
        
        try {
			server.send(datapacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
