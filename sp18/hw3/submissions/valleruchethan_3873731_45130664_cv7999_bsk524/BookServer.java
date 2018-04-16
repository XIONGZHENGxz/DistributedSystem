import java.io.*;
import java.util.*;
import java.net.*;

public class BookServer {


    public synchronized static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        //Initialize RecordLog and BookInventory
        RecordLog log = new RecordLog();
        BookInventory Inventory = new BookInventory();

        // parse the inventory file
        String line = null;
        String[] item = new String[3];
        String name = null;
        int amount;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                item = line.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                name = item[0];
                name = name.substring(1, name.length()-1);
                amount = Integer.valueOf(item[1]);
                Book a = new Book(name, amount);
                Inventory.add(a);
            }
            bufferedReader.close();
        }
        catch(Exception e) {
            System.out.println("Error reading file: " + fileName);
        }

        // start a UDP Server 
        UDPServer udpServer = new UDPServer(udpPort, log, Inventory);
        udpServer.start();
        
        // start a TCP Server
        TCPServer tcpServer = new TCPServer(tcpPort, log, Inventory);
        tcpServer.start();
    }
}