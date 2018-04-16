//package homework3;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TCP_thread extends Thread {
	Socket s;
	public TCP_thread ( Socket s ) {
		this.s = s;
	}
	
	public void run() {
		PrintStream pout = null;
		Scanner sc = null;
		BufferedReader in = null;

		try {
			pout = new PrintStream(s.getOutputStream());
			sc = new Scanner(s.getInputStream());
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true) {
			
			

			
			String cmd = "";
			//System.out.println("gets to TCP_thread");
			
			try {
				//while(!in.ready()) {  }
				cmd = in.readLine();
				//System.out.println("cmd: "+cmd);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//System.out.println("WTF");

			System.out.println("TCP cmd " + cmd);
			
			if (cmd == null) {
				try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sc.close();
				break;
			}
			String[] tokens = cmd.split(" ");

			if (tokens[0].equals("borrow")) {
				
				String[] num_tokens = cmd.split("\"");
				String[] name_title = new String[2]; // book name and
														// book title
														// will be
														// passed into
														// parameter
				name_title[0] = tokens[1];
				name_title[1] = "\"" + num_tokens[1] + "\"";

				String ret_str = BookServer.borrow(name_title);
							
				pout.println(ret_str);

			} else if (tokens[0].equals("return")) {
				String ret_str = BookServer.return_book(Integer.parseInt(tokens[1]));
				System.out.println("RETURN STRING: "+ret_str);
				pout.println(ret_str);
			} else if (tokens[0].equals("inventory") || tokens[0].equals("exit")) {
				String ret_str = BookServer.inventory();
				
				System.out.println("INVENTORY: " + ret_str);
				pout.println(ret_str);
				if (tokens[0].equals("exit")) {
					sc.close();
					try {
						s.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				// appropriate responses form the server
			} else if (tokens[0].equals("list")) {
				// System.out.println("list : " + tokens[1]);
				String ret_str = BookServer.list(tokens[1]);
				pout.println(ret_str);

			} else {
				System.out.println("ERROR: No such command");
			}
		}
	}
}
