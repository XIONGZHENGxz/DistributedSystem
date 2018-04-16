//package hwk3;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class TCPClientHandler extends Thread {
    Socket socket;
    Scanner in;
    PrintStream out;

    public TCPClientHandler(Socket s) throws IOException {
        socket = s;
        in = new Scanner(socket.getInputStream());
        out = new PrintStream(socket.getOutputStream());
    }

    public void run(){
        while (in.hasNextLine()) {
            String request = in.nextLine();
            String[] tokens = request.split(" ");
            if (tokens[0].equals("setmode")) {
                if (tokens[1].equals("U")) break;
            } else {
                String returnString = BookServer.performCommand(tokens);
                out.println(returnString);
                out.println("end");
                out.flush();
            }
        }
    }
}
