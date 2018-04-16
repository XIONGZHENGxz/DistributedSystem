import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer {

    static HashMap<String, Integer> libraryInventory;
    static HashMap<String, Student> studentDatabase;
    static HashMap<Integer, String> recIDMap;
    static Integer currRecID = 0;
    ExecutorService threadPool;

    public static synchronized String parseTitle(String line) {
        Pattern P1 = Pattern.compile("\"([^\"]*)\"");
        Matcher m1 = P1.matcher(line);
        String title = null;
        while (m1.find()) {
            //System.out.println(m1.group(0));
            title = m1.group(1);
        }
        return title;
    }

    public static synchronized void initLibrary(File fileName, Library library) throws FileNotFoundException {
        // parse the inventory file
        Scanner sc = new Scanner(fileName);

        //initialize the library inventory
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Pattern P1 = Pattern.compile("\"([^\"]*)\"");
            Pattern P2 = Pattern.compile("\\d+");
            Matcher m2 = P2.matcher(line);
            Matcher m1 = P1.matcher(line);
            //add book and quantity to library inventory
            while (m1.find() && m2.find()) {
                //System.out.println(m1.group(0));
                String book = m1.group(1);
                int quantity = Integer.parseInt(m2.group(0));
                library.getInventory().put(book, quantity);
            }
        }
    }

    //Create a new server thread for each new command


    public static class TCPServerThread extends Thread {
        Socket theClient;
        int udpPort;

        public TCPServerThread(Socket s, int udpPort) {
            this.theClient = s;
            this.udpPort = udpPort;
        }

        @Override

        //Handle all types of commands here
        public synchronized void run() {
            BufferedReader inFromClient = null;
            //DataOutputStream outToClient = null;
            PrintStream outToClient = null;
            Scanner sc = null;
            Scanner parseCmd = null;

            try {
                //create input stream attached to socket
                sc = new Scanner(theClient.getInputStream());
                inFromClient = new BufferedReader(new InputStreamReader(theClient.getInputStream()));
                //create output stream, attached to socket
                outToClient = new PrintStream(theClient.getOutputStream());
                //outToClient = new DataOutputStream(theClient.getOutputStream());
                System.out.println("handling client command");
                while (sc.hasNext()) {
                    String command = sc.nextLine();
                    System.out.println("this is the command: " + command);

                    //parseCmd = new Scanner(command);
                    //String cmdType = parseCmd.next();
                    /*String[] tokens = command.split(" ");
                    if (tokens[0].equals("setmode")) {
                        if (tokens[1].equals("U")) {
                            System.out.println("UDP: enters this");
                            UDPServer udp = new UDPServer(udpPort);
                            udp.start();
                            break;
                        }
                    }*/
                    //TODO process different commands
                    String output = runCommand(command, "tcp");
                    outToClient.println(output);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


    public synchronized static String borrow(String student, String book) {
       // System.out.println("This is the student " + student + " book " + book);
        String checkedOut = "Request Failed - Book not available";
        String bookNotAvail = "Request Failed - We do not have this book";
        Student temp;
        //check to see if the book exists
        if (!libraryInventory.containsKey(book)) {
            return bookNotAvail;
        } else {
            if (libraryInventory.get(book) == 0) {
                return checkedOut;
            }
            if (!studentDatabase.containsKey(student)) {
                temp = new Student(student);
                //System.out.println("Added " + temp.name + " to the database");
                studentDatabase.put(student, temp);
            } else {
                temp = studentDatabase.get(student);
            }
        }
        Integer quantity = libraryInventory.get(book);
        libraryInventory.put(book, quantity - 1);
        currRecID++;
        recIDMap.put(currRecID, student);
        temp.addBook(currRecID, book);
        StringBuilder ret = new StringBuilder("Your request has been approved,");
        ret.append(" " + currRecID);
        ret.append(" " + student);
        ret.append(" " + book);
        return ret.toString();

    }


    private synchronized static String list(String name, String protocol) {
        //if record doesn't exist return string
        StringBuilder ret = new StringBuilder();
       // System.out.println(name);
        if (!studentDatabase.containsKey(name)) {
            return ("No record found for " + name);
        } else {

            //find the student's records and return them
            HashMap<Integer, String> books = studentDatabase.get(name).getBooks();
            Iterator it = books.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ret.append(entry.getKey() + " \"" + entry.getValue()+ "\"" + "\n");
            }
        }
        if(protocol.equals("tcp")) {
            ret.append("DONE");
        }
        return ret.toString();
    }

    public synchronized static String inventory(String protocol) {
        StringBuilder ret = new StringBuilder();
        Iterator it = libraryInventory.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            ret.append("\"" + entry.getKey() + "\"" + " " + entry.getValue() + "\n");
        }
       // System.out.println("This is the output for inventory: \n" + ret.toString());
        if (protocol.equals("tcp")) {
            ret.append("DONE");
        }
        return ret.toString();

    }

    public synchronized static String ret(Integer recordID) {
        String noBorrow = recordID.toString() + " not found, no such borrow record";
        String ret = recordID.toString() + " is returned";
        if (!recIDMap.containsKey(recordID)) {
            return noBorrow;
        } else {
            //update student
            String name = recIDMap.get(recordID);
            Student student = studentDatabase.get(name);
            String book = student.getBooks().get(recordID);
            student.removeBook(recordID);
            //update library
            int count = libraryInventory.get(book);
            libraryInventory.put(book, count + 1);
            //update recordmap
            recIDMap.remove(recordID);
            return ret;
        }
    }

    public synchronized static String runCommand(String cmd, String protocol) {
        String[] tokens = cmd.split(" ");
        if (tokens[0].equals("borrow")) {
            String title = parseTitle(cmd);
            return borrow(tokens[1], title);
        } else if (tokens[0].equals("return")) {
            Integer recID = Integer.parseInt(tokens[1]);
            return ret(recID);

            // TODO appropriate responses form the server

        } else if (tokens[0].equals("inventory")) {
            return inventory(protocol);


        } else if (tokens[0].equals("list")) {
            return list(tokens[1], protocol);
            //outToServer.writeBytes(cmd);

            // appropriate responses form the server

        } else if (tokens[0].equals("exit")) {
            //System.out.println("Trying to return the inventory after exiting");
            //tell server to stop processing commands

            return inventory(protocol);

        } else {
            System.out.println("ERROR: No such command");
        }
        return "fk";
    }

    public static class UDPThread extends Thread {
        private DatagramSocket ds;
        private DatagramPacket dp;
        DatagramPacket returnpacket;
        String cmd;

        public UDPThread(DatagramPacket dp, DatagramSocket ds) {
            this.ds = ds;
            this.dp = dp;
        }

        public synchronized void run() {
            byte[] rbuff;
            try {
                cmd = new String(dp.getData(), 0, dp.getLength());
                String output = runCommand(cmd, "udp");
                rbuff = new byte[output.length()];
                rbuff = output.getBytes();
                returnpacket = new DatagramPacket(rbuff,
                        rbuff.length,
                        dp.getAddress(),
                        dp.getPort());
                ds.send(returnpacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static class UDPServer extends Thread {

        public int udpPort;
        private DatagramSocket ds;
        private DatagramPacket dp;

        public UDPServer(int udpPort) {
            this.udpPort = udpPort;
        }

        public synchronized void run() {
            try {
                ds = new DatagramSocket(udpPort);
                int len = 1024;
                byte[] buff = new byte[len];
                while (true) {
                    try {
                        dp = new DatagramPacket(buff, buff.length);
                        ds.receive(dp);
                        UDPThread t = new UDPThread(dp, ds);
                        t.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }


    }

    public static void main(String[] args) throws IOException {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;
        Library library = new Library();
        initLibrary(new File(fileName), library);
        libraryInventory = library.getInventory();
        studentDatabase = new HashMap<>();
        recIDMap = new HashMap<>();
        currRecID = 0;

        UDPServer udp = new UDPServer(udpPort);
        udp.start();


        //creating TCP socket, listening for multiple threads
        try {
            //DatagramSocket ds = new DatagramSocket(udpPort);
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket server;
            while ((server = listener.accept()) != null) {
               // System.out.println("New Client... Creating new thread");
                Thread t = new TCPServerThread(server, udpPort);
                t.start();
            }
        } catch (IOException e) {
            System.err.print("Server Aborted: " + e);
        }
        // TODO: handle request from clients
    }


}


