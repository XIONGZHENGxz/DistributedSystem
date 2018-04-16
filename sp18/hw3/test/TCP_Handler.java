import com.sun.security.ntlm.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCP_Handler implements Runnable {

    private Scanner sc;
    private PrintWriter wr;
    private Socket s;

    public TCP_Handler(Socket s) {
        //System.out.println("Created TCP socket!");
        this.s = s;
        try {
            sc = new Scanner(s.getInputStream());
            wr = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeThisSocket() {
        //System.out.println("Closing TCP socket...");
        sc.close();
        wr.close();
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true) {

            if (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                //System.out.println("Server received TCP cmd: " + cmd);
                String[] tokens = cmd.split(" ");

//                if (BookServer.debug) {
//                    BookServer.writeCommand(cmd);
//                }

                String[] response = BookServer.makeTransaction(tokens[0], cmd);
                if (response == null) {
                    closeThisSocket();
                    break;
                } else {
                    for (String s : response) {
                        wr.println(s);
                    }
                    wr.println(":::end:::");
                    wr.flush();
                }
            } // end if

        } // end of while

    }

}
