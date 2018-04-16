package hw3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class BookServer {
    static Library library;

    public static void main (String[] args) {
        int tcpPort;
        int udpPort;

        library = new Library();

        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        // parse the inventory file
        File file = new File(fileName);

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNext()) {
                String line = sc.nextLine();
                String name = line.substring(0, line.lastIndexOf(" "));
                int number = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));

                library.add(name, number);
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // TODO: handle request from clients

        // wait for UDP requests
        Thread udp = new ServerUDPThread(udpPort, library);
        udp.start();

        // wait for TCP requests
        try {
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;
            while ((s = listener.accept()) != null) {
                Thread t = new ServerTCPThread(s, library);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

