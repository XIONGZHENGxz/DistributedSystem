//package homework3;

import java.net.DatagramPacket;

public class UDP_thread extends Thread {
	String cmd;
	DatagramPacket dataPacket;
	
	public UDP_thread (String cmd, DatagramPacket dataPacket) {
		this.cmd = cmd;
		this.dataPacket = dataPacket;
	}
	public void run() {

            //System.out.println("Does it work? " + cmd);
            DatagramPacket returnPacket;
         // choose the command
			String[] tokens = cmd.split(" ");
			System.out.println("UDP: " + cmd);
			
			if (tokens[0].equals("borrow")) {
				// System.out.println("In borrow");
				String[] num_tokens = cmd.split("\"");
				String[] name_title = new String[2]; // book name and
														// book title
														// will be
														// passed into
														// parameter
				name_title[0] = tokens[1];
				name_title[1] = "\"" + num_tokens[1] + "\"";

				String ret_str = BookServer.borrow(name_title);
				byte[] buffer = new byte[ret_str.length()];
				buffer = ret_str.getBytes();
				returnPacket = new DatagramPacket(buffer, buffer.length, dataPacket.getAddress(),
						dataPacket.getPort());

				BookServer.send(returnPacket);
			} else if (tokens[0].equals("return")) {
				//System.out.println("return : " + tokens[1]);
				String ret_str = BookServer.return_book(Integer.parseInt(tokens[1]));
				byte[] buffer = new byte[ret_str.length()];
				buffer = ret_str.getBytes();
				returnPacket = new DatagramPacket(buffer, buffer.length, dataPacket.getAddress(),
						dataPacket.getPort());
				BookServer.send(returnPacket);
				
				// TODO: send appropriate command to the server and
				// display the
				// appropriate responses form the server
			} else if (tokens[0].equals("inventory") || tokens[0].equals("exit")) {
				
				//System.out.println("inventory");
				String ret_str = BookServer.inventory();
				byte[] buffer = new byte[ret_str.length()];
				buffer = ret_str.getBytes();
				returnPacket = new DatagramPacket(buffer, buffer.length, dataPacket.getAddress(),
						dataPacket.getPort());
				BookServer.send(returnPacket);
				
				// TODO: send appropriate command to the server and
				// display the
				// appropriate responses form the server
			} else if (tokens[0].equals("list")) {
				//System.out.println("list : " + tokens[1]);
				String ret_str = BookServer.list(tokens[1]);
				byte[] buffer = new byte[ret_str.length()];
				buffer = ret_str.getBytes();
				returnPacket = new DatagramPacket(buffer, buffer.length, dataPacket.getAddress(),
						dataPacket.getPort());
				BookServer.send(returnPacket);
				// TODO: send appropriate command to the server and
				// display the
				// appropriate responses form the server
			} else {
				System.out.println("ERROR: No such command");
			}

			// System.out.println(dataPacket.getData());

    }  
}
