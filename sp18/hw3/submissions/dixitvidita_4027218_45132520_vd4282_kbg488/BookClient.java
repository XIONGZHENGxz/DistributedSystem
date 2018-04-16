/*
* Katelyn Ge: kbg488
* Vidita Dixit: vd4282
 */

import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.net.*;

public class BookClient {
	
	static String hostAddress = "localhost";
	static int tcpPort = 7000; // hardcoded -- must match the server's tcp port
	static int udpPort = 8000; // hardcoded -- must match the server's udp port
	static int clientId;
	static boolean mode = true;
	static DatagramSocket datasocket;
	static File file;
	static BufferedWriter bw;
	static boolean firstLine = true;
	
	public static void main (String[] args) throws IOException {

		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String commandFile = args[0];
		clientId = Integer.parseInt(args[1]);
		String filename = "out_" + clientId + ".txt";
		
		try {
			file = new File(filename);
			PrintWriter pw = new PrintWriter(filename);
			pw.close();
			bw = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        try {
			Scanner sc = new Scanner(new FileReader(commandFile));
			while(sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if (tokens[0].equals("setmode")) {
					// TODO: set the mode of communication for sending commands to the server 
					if (tokens[1].equalsIgnoreCase("u")){
					    mode = true;
                    }
                    else if (tokens[1].equalsIgnoreCase("t")){
					    mode = false;
				    }else{
                        System.out.println("ERROR: No such command");
                        continue;
                    }
				}
				else if (tokens[0].equals("borrow")||tokens[0].equals("return")||
                        tokens[0].equals("inventory")||tokens[0].equals("list")){
				    if (mode){
                        udpMessage(cmd);
                    }
                    else{
				        tcpMessage(cmd);
                    }
                } else if (tokens[0].equals("exit")) {
				    if (mode){
                        udpMessage(cmd);
                    }
                    else{
				        tcpMessage(cmd);
                    }
				    bw.close();
                	break;
                }
                else {
                    System.out.println("ERROR: No such command");
                    continue;
                }
			}
            //datasocket.close();
		} catch (FileNotFoundException e) {

		}
    }
	
	public static void udpMessage(String args) {
		DatagramPacket sPacket, rPacket;
		byte[] rbuffer = new byte[65000];
		try {
			InetAddress ia = InetAddress.getByName(hostAddress);
			DatagramSocket datasocket = new DatagramSocket();
			byte[] buffer = new byte[args.length()];
			buffer = args.getBytes ();
			sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
			datasocket.send(sPacket);
			//receiving data back from server
			rPacket = new DatagramPacket(rbuffer, rbuffer.length);
			datasocket.receive(rPacket);
			String retString = new String(rPacket.getData(), 0, rPacket.getLength());
			if (retString.equals("")) return;
			if (firstLine) {
				bw.write(retString);
				firstLine = false;
			} else {
				bw.newLine();
				bw.write(retString);
			}
			//datasocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			
		} catch (IOException e) {
			
		}
	}

	public static void tcpMessage(String args){
	    Socket s;
	    try{
	        s = new Socket(hostAddress, tcpPort);
            Scanner sc = new Scanner(s.getInputStream());
            PrintStream pout = new PrintStream(s.getOutputStream());
            pout.println(args);
            pout.flush();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.isEmpty()) break;
                if (line.equals("")) break;
    			if (firstLine) {
    				bw.write(line);
    				firstLine = false;
    			} else {
    				bw.newLine();
    				bw.write(line);
    			}
            }
           // s.close();
        }catch ( UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}