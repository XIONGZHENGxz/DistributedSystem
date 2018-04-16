//eid_1: ajl3287
//eid_2: br24964
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer {
    private static final String INVENTORY_FILENAME = "inventory.txt";
    private static final int DEFAULT_TCP_PORT = 7000;
    private static final int DEFAULT_UDP_PORT = 8000;
    private ServerSocket tcpSock;
    private DatagramSocket udpSock;
    private final Map<String, Integer> bookshelf;
    private final Map<String, ArrayList<Record>> studentRecords;
    private final List<String> bookshelfOrder;
    private final Map<Integer, Record> requestIdMap;
    private AtomicInteger requestId;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Invalid number of arguments");
            System.exit(1);
        }

        try {
            BookServer server = new BookServer(args[0]);
            server.start();
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input file");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private BookServer(String inputFile) throws IOException, IllegalArgumentException {
        this(inputFile, DEFAULT_TCP_PORT, DEFAULT_UDP_PORT);
    }

    private BookServer(String inputFile, int tcpPort, int udpPort) throws IOException, IllegalArgumentException {
        tcpSock = new ServerSocket(tcpPort);
        udpSock = new DatagramSocket(udpPort);

        bookshelf = new HashMap<>();

        requestId = new AtomicInteger(1);

        studentRecords = new HashMap<>();
        requestIdMap = new HashMap<>();

        ArrayList<String> order = parseInputFile(inputFile);
        bookshelfOrder = Collections.unmodifiableList(order);
    }

    private ArrayList<String> parseInputFile(String fileName) throws IOException, IllegalArgumentException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        ArrayList<String> order = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            StringBuilder bookName = new StringBuilder();

            int idx = 0;
            if (line.charAt(idx) != '"')
                throw new IllegalArgumentException(fileName);

            bookName.append(line.charAt(idx));
            idx++;

            for (; idx < line.length(); idx++) {
                bookName.append(line.charAt(idx));
                if (line.charAt(idx) == '"')
                    break;
            }

            if (idx + 2 >= line.length())
                throw new IllegalArgumentException(fileName);

            synchronized (bookshelf) {
                try {
                    Integer count = Integer.parseInt(line.substring(idx + 2));
                    String book = bookName.toString();
                    bookshelf.put(bookName.toString(), count);
                    order.add(book);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(fileName);
                }
            }
        }

        return order;
    }

    private void acceptTCP() throws IOException {
        while (true) {
            Socket sock = tcpSock.accept();
            new Thread(new TCPHandler(sock)).start();
        }
    }

    private void acceptUDP() throws IOException {
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        final int BUF_SIZE = 256;
        byte[] buf = new byte[BUF_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (true) {
            udpSock.receive(packet);
            int port = packet.getPort();
            InetAddress address = packet.getAddress();
            String message = new String(packet.getData(), 0, packet.getLength());
            pool.submit(new UDPHandler(address, port, message));
        }
    }

    private void start() {
        try {
            Thread tcpAccepter = new Thread(() -> {
                try {
                    acceptTCP();
                } catch (IOException e) {
                    System.err.println("TCP server socket failed unexpectedly");
                }
            });
            Thread udpAcceptor = new Thread(() -> {
                try {
                    acceptUDP();
                } catch (IOException e) {
                    System.err.println("UDP server socket failed unexpectedly");
                }
            });

            tcpAccepter.start();
            udpAcceptor.start();
            tcpAccepter.join();
            udpAcceptor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private abstract class Handler implements Runnable {
        abstract void sendMessage(String message) throws IOException;

        abstract String receiveMessage();

        abstract void closeConnection() throws IOException;

        private String getInventory() {
            StringBuilder inventory = new StringBuilder();

            synchronized (bookshelf) {
                for (String name : bookshelfOrder) {
                    inventory.append(name)
                            .append(' ')
                            .append(bookshelf.get(name))
                            .append('\n');
                }
            }
            inventory.deleteCharAt(inventory.length() - 1);

            return inventory.toString();
        }

        private void handleRequest(String message) throws IOException {
            String response = "", book, student;
            Integer id;
            ArrayList<Record> records;
            List<String> list = new ArrayList<>();
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(message);
            while (m.find())
                list.add(m.group(1));

            String[] command = list.toArray(new String[list.size()]);

            switch (command[0]) {
                case "exit":
                    File inventoryFile = new File(INVENTORY_FILENAME);
                    if (inventoryFile.exists() && !inventoryFile.delete())
                        System.err.println("Couldn't delete old inventory file");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(inventoryFile));
                    writer.write(getInventory());
                    writer.close();
                    break;
                case "borrow":
                    student = command[1];
                    book = command[2];

                    if (!bookshelf.containsKey(book)) {
                        sendMessage("Request Failed - We do not have this book");
                        break;
                    }

                    synchronized (bookshelf) {
                        Integer count = bookshelf.get(book);
                        if (count <= 0)
                            response = "Request Failed - Book not available";
                        else {
                            bookshelf.put(book, count - 1);
                            id = requestId.getAndIncrement();

                            Record record;
                            synchronized (studentRecords) {
                                if (!studentRecords.containsKey(student))
                                    studentRecords.put(student, new ArrayList<>());

                                record = new Record(student, book, id);
                                studentRecords.get(student).add(record);
                            }
                            requestIdMap.put(id, record);
                            response = "Your request has been approved, " + id + ' ' + student + ' ' + book;
                        }
                    }

                    sendMessage(response);
                    break;
                case "return":
                    id = Integer.parseInt(command[1]);
                    boolean returned = false;

                    synchronized (bookshelf) {
                        Record record = requestIdMap.get(id);
                        if (record != null && !record.returned) {
                            returned = true;
                            record.returned = true;
                            bookshelf.put(record.book, bookshelf.get(record.book) + 1);
                        }
                    }

                    sendMessage(id + (returned ? " is returned" : " not found, no such borrow record"));
                    break;
                case "list":
                    student = command[1];

                    synchronized (studentRecords) {
                        records = studentRecords.get(student);
                        if (records != null) {
                            StringBuilder studentRecordBuilder = new StringBuilder();
                            for (Record record : records) {
                                if (!record.returned) {
                                    studentRecordBuilder
                                            .append(record.id)
                                            .append(' ')
                                            .append(record.book)
                                            .append('\n');
                                }
                            }
                            if (studentRecordBuilder.length() > 0)
                                studentRecordBuilder.deleteCharAt(studentRecordBuilder.length() - 1);
                            response = studentRecordBuilder.toString();
                        }
                        if (response.length() == 0)
                            response = "No record found for " + student;
                    }

                    sendMessage(response);
                    break;
                case "inventory":
                    sendMessage(getInventory());
                    break;
                default:
                    sendMessage("Invalid command");
            }
        }

        public void run() {
            try {
                String message;
                while ((message = receiveMessage()) != null) {
                    handleRequest(message);
                    if (message.equals("exit"))
                        break;
                }
                closeConnection();
            } catch (IOException e) {
                System.err.println("Connection closed unexpectedly");
            }
        }
    }

    private class TCPHandler extends Handler {
        Socket client;
        BufferedReader in;
        DataOutputStream out;

        TCPHandler(Socket socket) throws IOException {
            client = socket;
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new DataOutputStream(client.getOutputStream());
        }

        @Override
        void sendMessage(String message) throws IOException {
            out.writeBytes(message);
            out.write('\n');
            out.write('\n');
        }

        @Override
        String receiveMessage() {
            String message = null;
            try {
                message = in.readLine();
            } catch (IOException e) {
                System.err.println(e.toString());
            }

            return message;
        }

        @Override
        void closeConnection() throws IOException {
            in.close();
            out.close();
            client.close();
        }
    }

    private class UDPHandler extends Handler {
        InetAddress address;
        int port;
        String message;

        UDPHandler(InetAddress address, int port, String message) {
            this.address = address;
            this.port = port;
            this.message = message;
        }

        @Override
        void sendMessage(String message) throws IOException {
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            udpSock.send(packet);
        }

        @Override
        String receiveMessage() {
            String out = message;
            message = null;
            return out;
        }

        @Override
        void closeConnection() {
        }
    }

    private class Record {
        final String student;
        final String book;
        final Integer id;
        boolean returned;

        Record(String student, String book, Integer id) {
            this.student = student;
            this.book = book;
            this.id = id;
            this.returned = false;
        }
    }
}
