import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BookServer {
    static Map<String, Book> books;
    static Map<Integer, Record> records;
    static Map<String, Student> students;
    static ArrayList<String> bookTitles;
    static boolean readMode;
    static AtomicInteger numAccess;
    static int uniqueIds;
    static Lock l;
    static int connectId;
    static int nextRecordID;

    public static void main (String[] args) {
        int tcpPort;
        int udpPort;

        l = new ReentrantLock();
        connectId = 1;

        readMode = true;
        numAccess = new AtomicInteger(0);

        if (args.length != 1) {
          System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
          System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        nextRecordID = 1;
        books = new HashMap<String, Book>();
        records = new HashMap<Integer, Record>();
        students = new HashMap<String, Student>();
        bookTitles = new ArrayList<String>();
        uniqueIds = 1;

        // parse the inventory file
        try {
            parseInventoryFile(fileName);
        } catch (Exception e) {
            System.err.println(e);
        }

        // TODO: handle request from clients
        new Thread(new UDPThread(udpPort)).start();
        //new Thread(new UDPThread(udpPort)).start();
        //new Thread(new TCPThread(tcpPort)).start();
        //new Thread(new TCPThread(tcpPort)).start();
    }

    private static void parseInventoryFile(String fileName) throws Exception {
        Scanner sc = new Scanner(new FileReader(fileName));
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split("\" ");
            String title = tokens[0] + "\"";
            int stock = Integer.parseInt(tokens[1]);
            Book b = new Book(title, stock);
            bookTitles.add(title);
            books.put(title, b);
        }
    } 

    public static void requestRead(){
        l.lock();
    }

    public static void requestWrite(){
        l.lock();
    }

    public static void releaseRead(){
        l.unlock();
    }

    public static void releaseWrite(){
        l.unlock();
    }

    public static String handleArgs(String[] args){
        String value = new String();
        switch (args[0]) {
            case "borrow":
                requestWrite();
                String studentName = args[1];
                String bookTitle = args[2];
                for (int i = 3; i < args.length; i++)
                    bookTitle += " " + args[i];
                value = borrowBook(studentName, bookTitle);
                releaseWrite();
            break;

            case "return":
                requestWrite();
                int recordID = Integer.parseInt(args[1]);
                value = returnBook(recordID);
                releaseWrite();
            break;

            case "list":
                requestRead();
                studentName = args[1];
                value = list(studentName);
                releaseRead();
            break;

            case "inventory":
                requestRead();
                value = doInventory();
                releaseRead();
            break;

            case "exit":
                requestRead();
                exit();
                releaseRead();
            break;

            default: //TODO non-valid case

            break;
        }
        return value;
    }

    private static String borrowBook(String name, String title) {
        if (!books.keySet().contains(title))
        return "Request Failed - We do not have this book";
        
        Book b = books.get(title);
        if (b.stockLeft == 0)
        return "Request Failed - Book not available";

        b.stockLeft -= 1;
        if (!students.keySet().contains(name)) 
        students.put(name, new Student(name));

        Student s = students.get(name);
        Record r = new Record(nextRecordID, name, title);
        s.records.add(r);
        records.put(nextRecordID, r);
        nextRecordID += 1;
        return ("Your request has been approved, " + r.recordID + " " + s.name + " " + b.title);
    }

    private static String returnBook(int recordID) {
        if (!records.keySet().contains(recordID))
            return (recordID + " not found, no such borrow record");

        Record r = records.get(recordID);
        Student s = students.get(r.studentName);
        Book b = books.get(r.bookTitle);
        records.remove(recordID);
        s.records.remove(s.records.indexOf(r));
        b.stockLeft += 1;

        return (recordID + " is returned");
    }

    private static String list(String name) {
        if (!students.keySet().contains(name) || students.get(name).records.isEmpty())
        return ("No record found for " + name);
        
        Student s = students.get(name);
        String retVal = "";
        for (Record r: s.records) {
        retVal += r.recordID + " " + r.bookTitle + "~newLine~";
        }
        return retVal.substring(0, retVal.length() - 9);
  }

  private static String doInventory() {
    String retVal = "";
    for (String title : bookTitles) {
      Book b = books.get(title);
      retVal += b.title + " " + b.stockLeft + "~newLine~";
    }
    return retVal.substring(0, retVal.length() - 9);
  }

  private static String exit() {
    String inv = "";
    for (String title : bookTitles) {
      Book b = books.get(title);
      inv += b.title + " " + b.stockLeft + "\n";
    }
    inv = inv.substring(0, inv.length() - 1);

    String filename = "inventory.txt";
      boolean appendMode = false;
      try(FileWriter fw = new FileWriter(filename, appendMode);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
      {
        out.println(inv);
        System.out.println(inv);
      } catch (IOException e) {
          System.err.println(e);
      }
      return "";
  }
    public static void startThread(int port){
        System.out.println("Starting thread on port "+port);
        if(port<8000){
            new Thread(new TCPThread(port)).start();
        } else {
            new Thread(new UDPThread(port)).start();
        }
    }

  static class Book {
    String title;
    int stockTotal;
    int stockLeft;

    public Book(String t, int st) {
      title = t;
      stockTotal = st;
      stockLeft = stockTotal;
    }
  }
  
  static class Record {
    int recordID;
    String studentName;
    String bookTitle;

    public Record(int r, String s, String b) {
      recordID = r;
      studentName = s;
      bookTitle = b;
    }
  }

  static class Student {
    String name;
    List<Record> records;

    public Student(String n) {
      name = n;
      records = new ArrayList<Record>();
    }
  }

}

class UDPThread extends Thread{

    int udpPort;
    int newPort;
    static Lock l = new ReentrantLock();

    public UDPThread(int port){
        udpPort = port;
        newPort = udpPort;
    }

    @Override
    public void run() {
        DatagramPacket datapacket, returnpacket;
        boolean done = false;
        boolean master = udpPort == 8000;
        int udpLen = 1024;

        try {
            while(!done || master) {
                DatagramSocket datasocket = new DatagramSocket(udpPort);
                boolean connected = true;
                byte[] buf = new byte[udpLen];
                datapacket = new DatagramPacket(buf, buf.length);
                while (connected) {
                    datasocket.receive(datapacket);
                    String rcvd = new String(datapacket.getData(), 0, datapacket.getLength());
                    System.out.println("UDP " + udpPort + " recieved " + rcvd);
                    String[] args = new String[3];
                    args = rcvd.split(" ");

                    String rtrn = BookServer.handleArgs(args);
                    //l.lock();
                    if (rcvd.equals("request U")) {
                        if (master) {
                            newPort = 8000 + BookServer.connectId++;
                        }
                        connected = false;
                        rtrn = "port " + newPort;
                    } else if (rcvd.equals("request T")) {
                        newPort = 7000 + BookServer.connectId++;
                        rtrn = "port " + newPort;
                        done = true;
                        connected = false;
                    } else if (rcvd.equals("exit")) {
                        done = true;
                        connected = false;
                    }
                    //l.unlock();
                    System.out.println("UDP returning " + rtrn);

                    byte[] value = new byte[rtrn.length()];
                    value = rtrn.getBytes();

                    returnpacket = new DatagramPacket(
                            value,
                            value.length,
                            datapacket.getAddress(),
                            datapacket.getPort());
                    datasocket.send(returnpacket);

                    if (udpPort != newPort) {
                        BookServer.startThread(newPort);
                        newPort = udpPort;
                    }
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                datasocket.close();
            }
            System.out.println("Thread is dead on port "+udpPort);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class TCPThread extends Thread{
    int tcpPort;
    int newPort;
    static Lock l = new ReentrantLock();

    public TCPThread(int port){
        tcpPort = port;
        newPort = tcpPort;
    }

    @Override
    public void run() {
        boolean done = false;
        while(!done) {
            boolean connected = true;
            try {
                ServerSocket serverSocket = new ServerSocket(tcpPort);
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while(connected){
                    String rcvd = in.readLine();
                    if (rcvd == null) {
                        rcvd = new String();
                    }
                    System.out.println("TCP recieved " + rcvd);
                    String[] args = new String[3];
                    args = rcvd.split(" ");

                    String rtrn = BookServer.handleArgs(args);
                    //l.lock();
                    if (rcvd.equals("request U")) {
                        newPort = 8000 + BookServer.connectId++;
                        rtrn = "port " + newPort;
                        done = true;
                        connected = false;
                    } else if (rcvd.equals("request T")) {
                        newPort = 7000 + BookServer.connectId++;
                        rtrn = "port " + newPort;
                        done = true;
                        connected = false;
                    } else if (rcvd.equals("exit")){
                        done = true;
                        connected = false;
                    }
                    //l.unlock();
                    System.out.println("TCP returning " + rtrn);
                    out.println(rtrn);
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(newPort!=tcpPort) {
            BookServer.startThread(newPort);
        }
        System.out.println("Thread on port "+tcpPort+" dead");
    }
}