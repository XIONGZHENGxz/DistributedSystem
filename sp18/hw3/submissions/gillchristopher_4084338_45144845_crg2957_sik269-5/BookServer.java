import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Scanner;

import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {

    // The server socket.
    private static ServerSocket listener = null;
    // other global variables
    public static volatile ArrayList<Book> inventory = new ArrayList<>();
    public static volatile LinkedHashMap<String,LinkedHashMap<Integer, Book>> records = new LinkedHashMap<>();
    public static volatile AtomicInteger recordId = new AtomicInteger(0); // ALWAYS INCREMENT (just delete record)

    public static void main (String[] args) throws IOException {
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
            Scanner sc = new Scanner(new FileReader(fileName));

            while(sc.hasNextLine()) {
                String[] tokens = sc.nextLine().split("\" ");
                String name = tokens[0] + "\"";
                int number = Integer.parseInt(tokens[1]);
                AtomicInteger quantity = new AtomicInteger(number);
                Book newBook = new Book(name, quantity);
                inventory.add(newBook);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // TODO: handle request from clients

        System.out.println("server started");

        new TcpListener(tcpPort);
        new UdpListener(udpPort);
    }

    private static class TcpListener extends Thread {
        private ServerSocket listener;
        private Socket socket;
        private int PORT;

        TcpListener(int PORT) {

            this.PORT = PORT;

            start();
        }

        public void run() {

            try {
                listener = new ServerSocket(PORT);

                while (true) {

                    socket = listener.accept();

                    try {

                        new TcpClientHandler(socket);

                    } catch (Exception e) {

                        socket.close();

                        e.printStackTrace();
                    }
                }
            } catch (IOException d) {} finally {
                try {
                    this.listener.close();
                } catch (IOException t) {}
            }
        }
    }

    private static class UdpListener extends Thread {
        private DatagramSocket datagramSocket;
        DatagramPacket datapacket, returnpacket;
        private int PORT;
        int len = 1024;

        UdpListener(int PORT){

            this.PORT = PORT;

            start();
        }

        public void run() {

            try {
                datagramSocket = new DatagramSocket(PORT);

                while (true) {

                    try {

                        byte[] buf = new byte[len];
                        datapacket = new DatagramPacket(buf, buf.length);
                        datagramSocket.receive(datapacket);

                        new UdpClientHandler(datagramSocket, datapacket);

                    } catch (Exception e) {

                        datagramSocket.close();

                        e.printStackTrace();
                    }
                }
            } catch (IOException d) {
            } finally {
                this.datagramSocket.close();
            }
        }
    }

    private static class TcpClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        public TcpClientHandler(Socket socket) {
            this.socket = socket;

            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            } catch (IOException b) {}

            start();
        }

        public void run() {

            try {
                while (true) {
                    String str = in.readLine();
                    String[] tokens = str.split(" ");
                    if (tokens[0].equals("exit")) {
                        System.out.println("I do dat exit ting");
                        exit();
                        break;
                    } else if (tokens[0].equals("borrow")) {
                        System.out.println("I do dat borrow ting");
                        String result = borrow(tokens);
                        out.println(result);
                    } else if (tokens[0].equals("return")) {
                        System.out.println("I do dat return ting");
                        String result = returnBook(tokens);
                        out.println(result);
                    } else if (tokens[0].equals("inventory")) {
                        System.out.println("I do dat inventory ting");
                        synchronized(inventory) {
                            for(Book book : inventory) {
                                out.println(book.toString());
                            }
                            out.println("done");
                        }
                    } else if (tokens[0].equals("list")) {
                        System.out.println("I do dat list ting");
                        String result = list(tokens);
                        out.println(result);
                    }
                }
                System.out.println("I actually did dat exit ting");
            } catch (IOException e) {
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private synchronized static String list(String[] tokens) {
        String studentName = tokens[1];
        if(records.containsKey(studentName)) {
            String result = "";
            LinkedHashMap<Integer,Book> studentRecord = records.get(studentName);
            for(Integer key: studentRecord.keySet()) {
                result += key + " " + studentRecord.get(key).getName() + "\n";
            }
            result = result.substring(0, result.length()-1);
            return result;
        }

        return "No record found for " + studentName;
    }

    private synchronized static String borrow(String[] tokens) {
        String studentName = tokens[1];
        String bookName = String.join(" ", Arrays.copyOfRange(tokens, 2, tokens.length));
        Book bookInServer = contains(bookName);
        // inventory has book, not out of the book
        if(bookInServer != null) {
            // student has previously checked out book
            if(records.containsKey(studentName)) {
                LinkedHashMap<Integer,Book> studentRecord = records.get(studentName);
                Integer id = recordId.incrementAndGet();
                bookInServer.getQuantity().decrementAndGet();
                studentRecord.put(id, bookInServer);
                return "Your request has been approved, " + id + " " + studentName + " " + bookInServer.getName();
            }
            // first time student checked out book
            else {
                LinkedHashMap<Integer, Book> newRecord = new LinkedHashMap<>();
                bookInServer.getQuantity().decrementAndGet();
                Integer id = recordId.incrementAndGet();
                newRecord.put(id,bookInServer);
                records.put(studentName, newRecord);
                return "Your request has been approved, " + id + " " + studentName + " " + bookInServer.getName();
            }
        }
        else {
            return "Request Failed - Book not available";
        }
    }

    private synchronized static void exit() throws FileNotFoundException {
        PrintWriter exitWriter = new PrintWriter("inventory.txt");
        for(Book book : inventory) {
            exitWriter.println(book.toString());
        }
        exitWriter.close();
    }

    private synchronized static String returnBook(String[] tokens) {
        Integer recordID = Integer.parseInt(tokens[1]);
        for(String student: records.keySet()) {
            if(records.get(student).containsKey(recordID)) {
                String bookName = records.get(student).get(recordID).getName();
                for(Book b: inventory) {
                    if(b.getName().equals(bookName)){
                        b.getQuantity().incrementAndGet();
                    }
                }
                records.get(student).remove(recordID);
                return recordID + " is returned";
            }
        }
        return recordID + " is not found, no such borrow record";
    }

    private synchronized static Book contains(String name) {
        for(Book b: inventory) {
            if(b.getName().equals(name)) {
                if(b.getQuantity().get() > 0)
                    return b;
            }
        }
        return null;
    }


    private static class UdpClientHandler extends Thread {
        private DatagramPacket dataPacket;
        String str;
        int port;
        DatagramSocket datagramSocket;
        InetAddress address;


        public UdpClientHandler(DatagramSocket datagramSocket, DatagramPacket dataPacket) {

            this.dataPacket = dataPacket;
            this.str = new String(dataPacket.getData()).trim();
            this.port = dataPacket.getPort();
            this.address = dataPacket.getAddress();
            this.datagramSocket = datagramSocket;

            start();
        }

        public void run() {
            String[] tokens = str.split(" ");
            if (tokens[0].equals("exit")) {
                try {
                    BookServer.exit();
                }
                catch (FileNotFoundException e) {}
            }
            else if (tokens[0].equals("borrow")) {
                System.out.println("I do dat borrow ting");
                String result = borrow(tokens);
                byte[] resultBytes = result.getBytes();
                DatagramPacket sPacket = new DatagramPacket(resultBytes, resultBytes.length, address, port);
                try {
                    datagramSocket.send(sPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else if (tokens[0].equals("return")) {
                System.out.println("I do dat return ting");
                String result = returnBook(tokens);
                byte[] resultBytes = result.getBytes();
                DatagramPacket sPacket = new DatagramPacket(resultBytes, resultBytes.length, address, port);
                try {
                    datagramSocket.send(sPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (tokens[0].equals("inventory")) {
                System.out.println("I do dat inventory ting");
                synchronized (inventory) {
                    String result ="";
                    for(Book b: inventory) {
                        result += b.toString() + "\n";
                    }
                    result = result.substring(0, result.length()-1);
                    byte[] resultBytes = result.getBytes();
                    DatagramPacket sPacket = new DatagramPacket(resultBytes, resultBytes.length, address, port);
                    try {
                        datagramSocket.send(sPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (tokens[0].equals("list")) {
                System.out.println("I do dat list ting");
                String result = list(tokens);
                byte[] resultBytes = result.getBytes();
                DatagramPacket sPacket = new DatagramPacket(resultBytes, resultBytes.length, address, port);
                try {
                    datagramSocket.send(sPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}