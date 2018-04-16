/*
* Katelyn Ge: kbg488
* Vidita Dixit: vd4282
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class TCP extends Thread {

    Socket client;

    public TCP(Socket s){
        this.client = s;
    }

    @Override public void run(){
        try{
            Scanner sc = new Scanner(client.getInputStream());
            PrintWriter pout = new PrintWriter(client.getOutputStream());
                String command = sc.nextLine();
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
                
                pout.println(response);
                pout.flush();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
