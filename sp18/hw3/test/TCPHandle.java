/*
Mohamed Nasreldin man2766
Hamza Ghani hhg263
*/

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class TCPHandle extends Thread {
    private parseInventory inventory;
    private Socket sock;
    private Scanner input;
    private PrintWriter out;

    public TCPHandle(parseInventory inventory, Socket sock) throws IOException {
        this.inventory = inventory;
        this.sock = sock;
        this.input = new Scanner(sock.getInputStream());
        this.out = new PrintWriter(sock.getOutputStream());
    }

    public void run(){
        String command;
        try {
            while (input.hasNextLine()) {
                command = input.nextLine();
                String result = inventory.Command(command);
                out.println(result);
                out.println("end");
                out.flush();
            }
            sock.close();
        } catch (IOException e) {}
    }
}
