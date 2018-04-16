import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer {

    // static boolean debug = false;

    private static int recordId = 1;
    private static List<Book> serverBooks;
    private static List<Student> serverStudents;
    private static List<Record> serverRecords;

    private static DatagramSocket dataSocket;
    private static final int tcpPort = 7000;
    private static final int udpPort = 8000;

    private static FileWriter commandHistoryWriter;
    private static AtomicInteger responseCount;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }

//        if (debug) {
//            responseCount = new AtomicInteger(0);
//            try {
//                commandHistoryWriter = new FileWriter("command_history.txt", true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        // init server
        serverBooks = new LinkedList<>();
        serverStudents = new LinkedList<>();
        serverRecords = new LinkedList<>();
        parseInput(args[0]); // parse the inventory
        //printServerState();

        // start thread to handle TCP connections
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket listener = new ServerSocket(tcpPort);
                    while (true) {
                        Socket s;
                        while ((s = listener.accept()) != null) {
                            new Thread(new TCP_Handler(s)).start();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // start thread to handle UDP connections
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket dataPacket;
                try {

                    dataSocket = new DatagramSocket(udpPort);

                    while (true) {

                        //synchronized (dataSocket) { // java guarantees send/receive atomicity
                            byte[] buf = new byte[1000];
                            dataPacket = new DatagramPacket(buf, buf.length);
                            dataSocket.receive(dataPacket);
                        //}

                        new Thread(new UDP_Handler(dataPacket)).start();

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private synchronized static void parseInput(String fileName) {
        // parses the given file into serverBooks

        File file = new File(fileName);

        try {

            Pattern titlePattern = Pattern.compile("(\".*\")\\s([0-9]+)"); // of form "The Great Gatsby" 15
            Matcher m;
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {

                String title;
                int count;

                String line = sc.nextLine();

                if((m = titlePattern.matcher(line)).matches()) {
                    serverBooks.add(
                            new Book(m.group(1), Integer.parseInt(m.group(2)))
                    );
                }

            }

        } catch (FileNotFoundException|NumberFormatException e) {
            e.printStackTrace();
        }

    }

    // keyword is one of the following: borrow, return inventory, list, or exit
    // cmd is the full command sent to the server
    // processes the cmd. returns the response or null if there isn't one needed
    static synchronized String[] makeTransaction(String keyword, String cmd) {

        String[] response;

        switch (keyword) {

            case "borrow":
                response = borrow(cmd);
                break;
            case "return":
                response = returnBook(cmd);
                break;
            case "inventory":
                response = inventory();
                break;
            case "list":
                response = list(cmd);
                break;
            case "exit":
                outputInventoryToFile();
                response = null;
                break;
            default:
                response = null;

        } // end switch statement

//        if (response != null && debug)
//            response[0] = String.format("%d --- %s", responseCount.getAndIncrement(), response[0]);
        return response;

    }

    private synchronized static String[] borrow(String cmd) {

        String studentName = cmd.split(" ")[1];

        Pattern titlePattern = Pattern.compile("(\"[^\"]*\")");
        Matcher matcher = titlePattern.matcher(cmd);
        String title;
        if (matcher.find()) {
            title = matcher.group(1).trim();
        } else {
            throw new IllegalArgumentException("Invalid command");
        }

        // find desired book
        Book desiredBook = null;
        for (Book b : serverBooks) {
            if (b.getTitle().equals(title)) { // found book
                desiredBook = b;
                break;
            }
        }

        String response;
        if (desiredBook != null && desiredBook.getCount() != 0) { // book exists and available

            // create record and update serverRecords
            Record r = new Record(desiredBook);
            serverRecords.add(r);

            // find and update student, or create student
            Student desiredStudent = null;
            for (Student s : serverStudents) {
                if (s.getName().equals(studentName)) { // a student exists by this name
                    desiredStudent = s;
                    s.getRecords().add(r);
                    r.setStudent(s);
                }
            }
            if (desiredStudent == null) { // no student exists by this name
                Student s = new Student(studentName);
                s.getRecords().add(r);
                r.setStudent(s);
                serverStudents.add(s);
            }

            // update book
            desiredBook.checkoutBook(r);
            response = String.format("Your request has been approved, %d %s %s", r.id(), studentName, title);
        } else if (desiredBook != null && desiredBook.getCount() == 0) {
            response = "Request Failed - Book not available";
        } else {
            response = "Request Failed - We do not have this book";
        }

        //printServerState();

        return new String[] {response};

    }

    private synchronized static String[] returnBook(String cmd) {

        int recordId = Integer.parseInt(cmd.split(" ")[1]);

        Record desiredRecord = null;
        for (Record r : serverRecords) {
            if (r.id() == recordId) {
                desiredRecord = r;
                break;
            }
        }

        String response;
        if (desiredRecord != null) {
            desiredRecord.getBookCheckedOut().returnBook(desiredRecord.id()); // return book, remove record from book object
            serverRecords.remove(desiredRecord); // remove from serverRecords
            Student s = desiredRecord.getStudent();
            s.getRecords().remove(desiredRecord);
            if (s.getRecords().isEmpty())
                serverStudents.remove(s);
            response = String.format("%d is returned", recordId);
        } else {
            response = String.format("%d not found, no such borrow record", recordId);
        }

        //printServerState();

        return new String[] {response};

    }

    private synchronized static String[] inventory() {
        return serverBooks.toString().replaceAll("]", "")
                                     .replaceAll("\\[", "")
                                     .split(", ");
    }

    private synchronized static String[] list(String cmd) {

        String studentName = cmd.split(" ")[1];
        // find and update student, or create student
        Student desiredStudent = null;
        for (Student s : serverStudents) {
            if (s.getName().equals(studentName)) { // a student exists by this name
                desiredStudent = s;
            }
        }

        if (desiredStudent == null)
            return new String[] {String.format("No record found for %s", studentName)};

        // fill response with the list of books
        String[] response = new String[desiredStudent.getRecords().size()];
        for (int i = 0; i < desiredStudent.getRecords().size(); i++) {
            response[i] = desiredStudent.getRecords().get(i).stringForList();
        }

        return response;
    }

    private synchronized static void outputInventoryToFile() {
        try {
            FileWriter fw = new FileWriter("inventory.txt", false); // now start appending on this file
            for (Book b : serverBooks) {
                fw.append(String.format("%s\n",b.toString()));
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void sendUDPResponse(DatagramPacket responsePacket) {
        if (dataSocket == null || responsePacket == null)
            throw new NullPointerException("Server ERROR: socket or packet is null");

        try {
            //synchronized (dataSocket) { // java guaruntees send/receive atomicity
            dataSocket.send(responsePacket);
            //System.out.println("Successfully sent packet!");
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printServerState() {

        System.out.println();
        System.out.println("Books:");
        StringBuilder bookBuilder = new StringBuilder();
        for (Book b : serverBooks) {
            bookBuilder.append(b.detailedString());
            bookBuilder.append("\n");
        }
        System.out.println(bookBuilder.toString());

        System.out.println("\nStudents:");
        StringBuilder stuBuilder = new StringBuilder();
        for (Student s : serverStudents) {
            stuBuilder.append(s.detailedString());
            stuBuilder.append("\n");
        }
        System.out.println(stuBuilder.toString());

        System.out.println("\nRecords:");
        StringBuilder recBuilder = new StringBuilder();
        for (Record r : serverRecords) {
            recBuilder.append(r);
            recBuilder.append("\n");
        }
        System.out.println(recBuilder.toString());
        System.out.println();

    }

//    static synchronized void writeCommand(String cmd) {
//
//        if (!debug || cmd == null) return;
//        try {
//            commandHistoryWriter.append(cmd);
//            commandHistoryWriter.append("\n");
//            commandHistoryWriter.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

}
