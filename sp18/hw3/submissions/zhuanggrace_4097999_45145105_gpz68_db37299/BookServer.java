
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class BookServer {
    
    private static Integer recordID;
    private static HashMap<String, Integer> library;
    private static HashMap<Integer, Record> log;
    private static HashMap<String, ArrayList<Record>> studentLog;
    
    
    public BookServer() {
        library = new LinkedHashMap<String, Integer>();
        log = new HashMap<Integer, Record>();
        studentLog = new HashMap<String, ArrayList<Record>>();
        recordID = 0;
    }
    
    public static synchronized String borrow (String name, String book) {
        if (library.containsKey(book)) {
            int num = library.get(book);
            if (num == 0) {
                return "Request Failed - Book not available";           
            }
            
            else {
                recordID++;   
                library.put(book, num - 1);
                log.put(recordID, new Record(name, book, recordID));
                if (studentLog.containsKey(name)) {
                    ArrayList<Record> updated = studentLog.get(name);
                    updated.add(new Record(name, book, recordID));
                    studentLog.put(name, updated);
                }
                else {
                    ArrayList<Record> temp = new ArrayList<Record>();
                    temp.add(new Record(name, book, recordID));
                    studentLog.put(name, temp);
                }
                String s = new String("Your request has been approved, " + recordID.toString() + " " + name + " \"" + book + "\"");
                return s;
            }
        }
        else {
            return "Request Failed - We do not have this book";
        }
    }
    
    public static synchronized String returns (Integer id) {
        String s = id.toString();

        if (log.containsKey(id)) {
            String book = log.get(id).bookName;
            String student = log.get(id).studentName;
            library.put(book, library.get(book) + 1);
            ArrayList<Record> updated = studentLog.get(student);
            for (Record r: updated) {
                if (r.recordID == id) {
                    updated.remove(r);
                    break;
                }
            }
            if (updated.size() != 0) {
                studentLog.put(student, updated);
            }
            
            else {
                studentLog.remove(student);
            }
            s = s + " is returned";
            log.remove(id);
            return s;
        }
        
        else {
            s = s + " not found, no such borrow record";
            return s;
        }
    }
    
    public static synchronized String list(String name) {
        String s = new String("");
        if (!studentLog.containsKey(name)) {
            s = "No record found for " + name;
            return s;
        }
        
        else {
            for(Record r: studentLog.get(name)) {
                s = s + r.recordID.toString() + " \"" + r.bookName + "\"\n";
            }
        }
        s = s.substring(0, s.length() - 1);
        return s;
    }
    
    public static synchronized String inventory() {
        String s = new String("");
        for (HashMap.Entry<String, Integer> entry: library.entrySet()) {
            s = s + "\"" + entry.getKey() + "\" " + entry.getValue().toString() + "\n";
        }
        s = s.substring(0, s.length() - 1);
        return s;
    }
    
    
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

    
    BookServer server = new BookServer();
    Parser parse = new Parser();
    
    // parse the inventory file
    
    Scanner sc = null;
    try {
        sc = new Scanner(new FileReader(fileName));
    } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    
    while(sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = parse.parse(cmd);
        int temp = Integer.parseInt(tokens[1]);
        
        server.library.put(tokens[0], temp);
    }
    
    DatagramPacket commandPacket;

    int port = udpPort;
    int len = 1024;
    try {
        DatagramSocket datasocket = new DatagramSocket(port);
        byte[] buf = new byte[len];
        while(true) {
            commandPacket = new DatagramPacket(buf, buf.length);
            datasocket.receive(commandPacket);
            String command = new String(commandPacket.getData(),0, 7);
            if(command.equals("Connect")) {
                System.out.println("Server has received connection!");
                UDPWorker worker = new UDPWorker(commandPacket.getPort(), commandPacket.getAddress(), server);
                worker.start();
            }
            else {
                System.out.println("invalid connection!");
            }
        }
    } catch (SocketException e) {
        System.err.println(e);
    } catch (IOException e) {
        System.err.println(e);
    }
  }
}
