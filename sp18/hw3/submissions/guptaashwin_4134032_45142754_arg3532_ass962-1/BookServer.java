import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {
    static final LinkedHashMap<String, Integer> inventory = new LinkedHashMap<String, Integer>();
    static final HashMap<Integer, String> recordToBookName = new HashMap<Integer, String>();
    static final HashMap<String, ArrayList<Integer>> studentToRecords = new HashMap<String, ArrayList<Integer>>();

    static AtomicInteger nextUniqueId = new AtomicInteger(1);
    private static final int maxLength = 1024;

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
            Scanner scan = new Scanner(new FileReader(fileName));
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                String[] parsedLine = line.split("\" ");
                String bookName = parsedLine[0] + "\"";
                int quantity = Integer.parseInt(parsedLine[1]);
                inventory.put(bookName, quantity);
            }
//            for(String key : inventory.keySet()){
//                System.out.println(key + ": " + inventory.get(key));
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // TODO: handle request from clients
        //  Search for UDP Sockets from separate thread
        UDPThread udpSearching = new UDPThread(udpPort);
        udpSearching.start();

        // Search for TCP Sockets from another thread
        TCPThread tcpSearching = new TCPThread(tcpPort);
        tcpSearching.start();
    }

    public static String borrow(String studentName, String bookName){
        String response = "";
        int currentUniqueId = -1;
        synchronized (inventory){
            if(!inventory.containsKey(bookName)){
                response = "Request Failed - We do not have this book";
                return response;
            }
            else if(inventory.get(bookName) <= 0){
                response = "Request Failed - Book not available";
                return response;
            }
            else {
                inventory.put(bookName, inventory.get(bookName) - 1);
                currentUniqueId = nextUniqueId.get();
                nextUniqueId.incrementAndGet();
            }
        }

        synchronized (recordToBookName){
            recordToBookName.put(currentUniqueId, bookName);
            response = "Your request has been approved, " + currentUniqueId + " " + studentName + " " + bookName;
        }

        synchronized (studentToRecords){
            if(!studentToRecords.containsKey(studentName)){
                ArrayList<Integer> records = new ArrayList<Integer>();
                records.add(currentUniqueId);
                studentToRecords.put(studentName, records);
            }
            else {
                studentToRecords.get(studentName).add(currentUniqueId);
            }
        }
        return response;
    }

    public static String returnRecordId(int recordId){
        String bookName = "";
        synchronized (studentToRecords){
            String studentToRemoveFrom = "";
            for(String student : studentToRecords.keySet()){
                if(studentToRecords.get(student).contains(recordId)){
                    studentToRemoveFrom = student;
                    break;
                }
            }
            if(!studentToRemoveFrom.equals("")){
                studentToRecords.get(studentToRemoveFrom).remove(new Integer(recordId));
            }
        }
        synchronized (recordToBookName){
            if(!recordToBookName.containsKey(recordId)){
                return recordId + " not found, no such borrow record";
            }
            else {
                bookName = recordToBookName.get(recordId);
                recordToBookName.remove(recordId);
            }
        }
        synchronized (inventory){
            inventory.put(bookName, inventory.get(bookName) + 1);
        }
        return recordId + " is returned";
    }

    public static String listRecords(String studentName){
        ArrayList<Integer> temp = new ArrayList<Integer>();
        synchronized (studentToRecords){
            if (!studentToRecords.containsKey(studentName) || studentToRecords.get(studentName).size() == 0){
                return "No record found for " + studentName;
            }
            temp.addAll(studentToRecords.get(studentName));
        }
        StringBuilder response = new StringBuilder();
        synchronized (recordToBookName){
            response.append(temp.size()).append('\n');
            for(Integer i : temp){
                response.append(i).append(" ").append(recordToBookName.get(i)).append('\n');
            }
        }
        return response.toString().substring(0, response.length() - 1);
    }

    public static String inventory(){
        StringBuilder response = new StringBuilder();
        synchronized (inventory){
            response.append(inventory.keySet().size()).append('\n');
            for (String bookName : inventory.keySet()){
                response.append(bookName).append(" ").append(inventory.get(bookName)).append('\n');
            }
        }
        return response.toString().substring(0, response.length() - 1);
    }
}