
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TCPWorker extends Thread {
    
    Socket theClient;
    int myPort;
    InetAddress iaddr;
    BookServer library;
    
    public TCPWorker(int port, InetAddress iNet, BookServer books) {
        myPort = port;
        iaddr = iNet;
        library = books;
    }
    
    public void run() {
        try {
            
            System.out.println("TCP Server Up");
            System.out.println(myPort);
            ServerSocket server = new ServerSocket(myPort);
            theClient = server.accept();
            System.out.println("Accepted " + theClient.getPort());
            
            Parser parse = new Parser();
            Scanner sc = new Scanner(theClient.getInputStream());
            PrintWriter pout = new PrintWriter(theClient.getOutputStream());

            while(true) {
                String command = sc.nextLine();
                System.out.println("TCP received: " + command);

                String[] tokens = parse.parse(command);
                if (tokens[0].equals("setmode")) {
                    if (tokens[1].equals("U")) {
                        int clientPort = theClient.getPort();
                        theClient.close();
                        Thread t = new UDPWorker(clientPort, iaddr, library);
                        t.start();
                        break;
                    }
                }

                else if (tokens[0].equals("borrow")) {
                    pout.append(1 + "\n");
                    pout.print(library.borrow(tokens[1], tokens[2]));
                    pout.flush();
                }

                else if (tokens[0].equals("return")) {
                    System.out.println("TCP is returning");
                    pout.print(1 + "\n");
                    pout.print(library.returns(Integer.parseInt(tokens[1])));
                    pout.flush();
                }

                else if (tokens[0].equals("list")) {
                    pout.print(countLines(library.list(tokens[1])) + "\n");
                    pout.print(library.list(tokens[1]));
                    pout.flush();
                }

                else if (tokens[0].equals("inventory")) {
                    pout.print(countLines(library.inventory()) + "\n");
                    pout.print(library.inventory());
                    pout.flush();
                }

                else if (tokens[0].equals("exit")) {
                    System.out.println("TCP is exiting");
                    pout.print(0 + "\n");
                    pout.flush();
                    pout.close();
                    sc.close();
                    FileWriter outputWriter = new FileWriter("inventory.txt");
                    outputWriter.write(library.inventory());
                    outputWriter.flush();
                    outputWriter.close();
                    theClient.close();
                    break;
                }
                pout.print("\n");
                pout.flush();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private int countLines(String text) {
        int numLines = text.split("[\n|\r]").length;
        System.out.println("\n" + text + "\nhas " + numLines + " line(s).\n");
        return numLines;
    }
}
