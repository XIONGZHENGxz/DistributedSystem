
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class UDPWorker extends Thread {
    int port;
    InetAddress iaddr;
    String command;
    int len = 1024;
    BookServer library;
    
    public UDPWorker(int port, InetAddress iaddr, BookServer library) throws IOException {
        this.port = port;
        this.iaddr = iaddr;
        this.library = library;
    }
    
    @Override
    public void run() {
        DatagramPacket commandPacket, returnPacket;
        
        try {
            DatagramSocket datasocket = new DatagramSocket();
            // byte[] cmdBuffer = new byte[len];
            byte[] respBuffer = new byte[len];
            Parser parser = new Parser();
            
            returnPacket = new DatagramPacket("Acknowledge".getBytes(),"Acknowledge".length(), iaddr, port);
            datasocket.send(returnPacket);
            
            while (true) {
                byte[] cmdBuffer = new byte[len];
                commandPacket = new DatagramPacket(cmdBuffer, cmdBuffer.length);
                datasocket.receive(commandPacket);

                String command = new String(commandPacket.getData());
                System.out.println("UDP received: " + command);
                String[] tokens = parser.parse(command);
    
                if (tokens[0].equals("setmode")) {
                    // TODO: set the mode of communication for sending commands to the server
                    if (tokens[1].equals("T")) {

                        int serverPort = datasocket.getLocalPort();
                        //datasocket.close();
                        
                        Thread t = new TCPWorker(serverPort, iaddr, library); // need my socket
                        t.start();
                        break;
                    }
                } else if (tokens[0].equals("borrow")) {
                    System.out.println("Borrowing in UDP Worker\n");
                    respBuffer = library.borrow(tokens[1], tokens[2]).getBytes();
                } else if (tokens[0].equals("return")) {
                    System.out.println("Returning in UDP Worker\n");
                    respBuffer = library.returns(Integer.parseInt(tokens[1])).getBytes();
                } else if (tokens[0].equals("inventory")) {
                    System.out.println("Inventory in UDP Worker\n");
                    respBuffer = library.inventory().getBytes();
                } else if (tokens[0].equals("list")) {
                    System.out.println("Listing in UDP Worker\n");
                    respBuffer = library.list(tokens[1]).getBytes();
                } else if (tokens[0].equals("exit")) {
                    System.out.println("Exiting in UDP Worker\n");
                    FileWriter outputWriter = new FileWriter("inventory.txt");
                    outputWriter.write(library.inventory());
                    outputWriter.flush();
                    outputWriter.close();
                    datasocket.close();
                    return;
                } else {
                    System.out.println("ERROR: No such command");
                }
                
                returnPacket = new DatagramPacket(respBuffer, respBuffer.length, commandPacket.getAddress(), commandPacket.getPort());
                datasocket.send(returnPacket);
            }
        } catch (SocketException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
