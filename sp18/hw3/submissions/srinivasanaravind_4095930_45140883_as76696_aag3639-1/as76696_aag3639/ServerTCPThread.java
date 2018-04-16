package hw3;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ServerTCPThread extends Thread {
    Socket theClient;
    Library library;

    public ServerTCPThread(Socket theClient, Library library) {
        this.theClient = theClient;
        this.library = library;
    }

    @Override
    public void run() {
        try {
            Scanner sc = new Scanner(theClient.getInputStream());
            PrintWriter pout = new PrintWriter (theClient.getOutputStream());

            while(sc.hasNextLine()) {
                String command = sc.nextLine();
                String[] split = command.split("_%_");

                if (split[0].equals("borrow")) {
                    pout.println(library.borrow(split[1], split[2]));
                    pout.flush();
                } else if (split[0].equals("return")) {
                    if (library.returnBook(Integer.parseInt(split[1]))) {
                        pout.println(Integer.parseInt(split[1]) + " is returned");
                        pout.flush();
                    } else {
                        pout.println(Integer.parseInt(split[1]) + " not found, no such borrow record");
                        pout.flush();
                    }
                } else if (split[0].equals("list")) {
                    pout.println(library.list(split[1]));
                    pout.flush();
                } else if (split[0].equals("inventory")) {
                    pout.println(library.getInventory());
                    pout.flush();
                } else if (split[0].equals("exit")) {
                    library.updateInventory();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.run();
    }
}
