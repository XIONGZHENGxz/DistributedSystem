import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class BookServer {

    private static final String serverID = "\033[41;30mServer:\033[0m ";
    private static Library library;
    private static Path p;
    private static PrintWriter out;

    public static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }

        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        // parse the inventory file
        try {
            library = new Library(fileName);
        } catch (FileNotFoundException fnf) {
            System.err.println("ERROR: File "+ fileName + " not found.");
            System.exit(-2);
        }

        // TODO: handle request from clients
        try {
            p = Paths.get("./inventory.txt");
            Files.deleteIfExists(p);
            out = new PrintWriter(Files.newOutputStream(p, CREATE, APPEND), true);
            final ExecutorService taskExecutor = Executors.newCachedThreadPool();
            int length = 256;
            byte[] buffer = new byte[length];
            byte[] buffer2 = new byte[length];
            DatagramSocket mainSocket = new DatagramSocket(udpPort);
            DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);
            ConcurrentSocketFactory socketFactory = new ConcurrentSocketFactory(udpPort, tcpPort);

            System.out.println("Main Thread: INFO: Accepting connections...");
            while (true) {
                mainSocket.receive(rPacket);
                String[] message = NetworkConnector.byteArrayToString(rPacket.getData()).split(",");
                System.out.println("Main Thread: INFO: Message received, allocating task to new thread");
                DatagramSocket newUDPSocket = socketFactory.newUDPSocket();
                String host = newUDPSocket.getLocalAddress().getHostName();
                int port = newUDPSocket.getLocalPort();
                SocketAddress sendAddress = new InetSocketAddress(message[0], Integer.parseInt(message[1]));
                taskExecutor.submit(
                        new RequestHandler(
                                socketFactory,
                                newUDPSocket,
                                sendAddress)
                );
                DatagramPacket sPacket = new DatagramPacket(buffer2, buffer2.length, sendAddress);
                String sMessage = host + "," + String.valueOf(port);
                sPacket.setData(sMessage.getBytes());
                System.out.println("Sending " + sMessage + "...");
                mainSocket.send(sPacket);
                System.out.println("Sent");
            }
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private static class RequestHandler implements Runnable {

        private ServerNetworkConnector network;

        RequestHandler(
                ConcurrentSocketFactory socketFactory,
                DatagramSocket initSocket,
                SocketAddress sendAddress)
        {
            network = new ServerNetworkConnector(socketFactory, initSocket, sendAddress);
        }

        @Override
        public void run() {
            try {
                boolean done = false;
                while (!done) {
                    done = handleRequest();
                }
            } catch (IOException e) {
                System.err.println("ERROR: Request Handler" + e.getMessage());
            }
            System.out.println("Shutting down connection");
        }

        private boolean handleRequest() throws IOException {
            System.out.println(serverID + "Accepting Requests...");
            String request = network.receive();
            System.out.println("Request received: " + request);
            return handleCommand(request.split(","));
        }

        private boolean handleCommand(String[] rMessage) throws IOException {
            switch (rMessage[0]) {
                case "setmode":
                    network.setMode(rMessage[1]);
                    if (rMessage[1].equals("T")) {
                        network.receive().equals("tcp connection established");
                        network.send("all clear");
                    } else {
                        network.send("localhost," + network.udpSocket.getLocalPort());
                    }
                    break;
                case "borrow":
                    borrowBook(network, rMessage[1], rMessage[2]);
                    break;
                case "return":
                    returnBook(network, Integer.parseInt(rMessage[1]));
                    break;
                case "list":
                    list(network, rMessage[1]);
                    break;
                case "inventory":
                    inventory(network);
                    break;
                case "exit":
                    network.send("exit successful");
                    return true;
            }
            return false;
        }

        private void inventory(NetworkConnector network) throws IOException {
            Map<String, Integer> inventory = library.inventory;
            StringBuilder str = new StringBuilder();
            synchronized (library.inventory) {
                for (String bookName: inventory.keySet()) {
                    str.append("");
                    str.append(bookName);
                    str.append(" ");
                    str.append(inventory.get(bookName));
                    str.append("|");
                }
            }
            if (str.length() != 0) {
                network.send(str.substring(0, str.length() - 1));
                out.println(str.substring(0, str.length() - 1).replace("|", "\n"));
            } else {
                network.send(str.toString());
                out.println(str.toString().replace("|", "\n"));
            }
        }

        private void list(ServerNetworkConnector network, String studentName) throws IOException {
            SortedMap<Integer, String> borrowRecord = library.getBorrowRecord(studentName);
            if (borrowRecord.isEmpty()) {
                network.send("No record found for " + studentName);
            } else {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<Integer, String> entry: borrowRecord.entrySet()) {
                    str.append(entry.getKey());
                    str.append(" ");
                    str.append(entry.getValue());
                    str.append("|");
                }
                network.send(str.substring(0, str.length() - 1));
            }
        }

        private void returnBook(ServerNetworkConnector network, int recordID) throws IOException {
            if (library.returnBook(recordID)) {
                network.send(recordID + " is returned");
            } else {
                network.send(recordID + " not found, no such borrow record");
            }
        }

        private void borrowBook(ServerNetworkConnector network, String studentName, String bookName) throws IOException {
            int recordID = library.borrow(studentName, bookName);
            switch (recordID) {
                case -1:
                    network.send("Request Failed - We do not have this book");
                    break;
                case -2:
                    network.send("Request Failed - Book not available");
                    break;
                default:
                    network.send(String.format("Your request has been approved, %d %s %s", recordID, studentName, bookName));
                    break;
            }
        }

    }

}
