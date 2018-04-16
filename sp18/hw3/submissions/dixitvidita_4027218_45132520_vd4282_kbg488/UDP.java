/*
* Katelyn Ge: kbg488
* Vidita Dixit: vd4282
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.net.*;

public class UDP extends Thread {

    static String hostAddress = "localhost";
    static int udpPort = 8000; // hardcoded -- must match the server's udp port
    DatagramPacket datapacket, returnpacket;
    DatagramSocket datasocket;

    public UDP(){
    }
	
	@Override public void run() {
        try {
            datasocket = new DatagramSocket(udpPort);
            while (true) {
                byte[] buf = new byte[65000];
                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);
                String command = new String(datapacket.getData());
                String[] tokens = command.split(" ", 3);
                String response = "";
                
                tokens[0] = tokens[0].trim();
                
                if (tokens[0].equals("borrow")){
                	tokens[1] = tokens[1].trim();
                	tokens[2] = tokens[2].trim();
                	String arg2 = tokens[2].substring(0, tokens[2].lastIndexOf("\"")+1);
                	response = BookServer.borrowBook(tokens[1], arg2);
                } else if (tokens[0].equals("return")){
                	int id = Integer.parseInt(tokens[1].trim());
                	response = BookServer.returnBook(id);
                } else if (tokens[0].equals("list")){
                	tokens[1] = tokens[1].trim();
                	response = BookServer.listRecord(tokens[1]);
                } else if (tokens[0].equals("inventory")) {
                    response = BookServer.getInventory();
                } else if (tokens[0].equals("exit")){
                	//TODO: set response
                	File inventory = new File("inventory.txt");
        			BufferedWriter bw = new BufferedWriter(new FileWriter(inventory, false));
        			bw.write(BookServer.getInventory());
        			bw.close();
        			response = "";
                }

              //return response back to client
			  byte[] buffer = new byte[response.length()];
			  buffer = response.getBytes();
			  returnpacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(), datapacket.getPort());
			  datasocket.send(returnpacket);
            }
        } catch (SocketException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
	}

}
