import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookServer{

    private static int recordNumber = 0;
    private static Map<String, Integer> inventory = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
    
    // static LinkedHashMap<String, Integer> inventory;
    private static ConcurrentHashMap<String, HashMap<Integer, String>> studentRecords = new ConcurrentHashMap<String, HashMap<Integer, String>>();
    private static ConcurrentHashMap<Integer, String> record = new ConcurrentHashMap<Integer, String>();



    public static synchronized void updateInventoryFile(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("inventory.txt"));
            String[] bookInventory = inventoryString().split("\n");

            for(String i : bookInventory){
                writer.write(i);
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static synchronized String inventoryString(){
        String returnString = "";
        for(String name : inventory.keySet()){
            returnString+=name+ " " + inventory.get(name) + "\n";
        }
        return returnString;
    }


    public static synchronized void getInventory(String fileName){
        inventory = new LinkedHashMap<String, Integer>();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(fileName));
            String line = null;
            while((line = in.readLine()) != null){
                int quotationLocation = 1;
                while(line.charAt(quotationLocation) != '\"'){
                    quotationLocation++;
                }
                String bookName = line.substring(0, quotationLocation+1);
                Integer amount = Integer.parseInt(line.substring(quotationLocation+2));
                inventory.put(bookName, amount);

            }
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public static synchronized String getStudentRecord(String student){
        String returnString = "";
        if(studentRecords.containsKey(student)){
            if(studentRecords.get(student).size() == 0){
                returnString = "No record found for " + student;
            }else{
                HashMap<Integer, String> temp = studentRecords.get(student);
                for(Integer i : temp.keySet()){
                    returnString += i + " " + temp.get(i) + "\n";
                }
            }


        }
        else returnString = "No record found for " + student;
        return returnString;
    }


    public static synchronized byte[] borrowBook(String[] commandInfo){
        byte[] response = null;
        String studentName = commandInfo[1];

        String bookName = "";
        for(int i = 2; i <commandInfo.length; i++){
            bookName += commandInfo[i] + " ";
        }
        bookName = bookName.trim();

        if(inventory.containsKey(bookName)){
            if(inventory.get(bookName) == 0){
                response = "Request Failed - Book not available".getBytes();
            }else{
                inventory.put(bookName, inventory.get(bookName)-1);
                if(studentRecords.containsKey(studentName)){
                    HashMap<Integer, String> temp = studentRecords.get(studentName);
                    temp.put(++recordNumber, bookName);
                    studentRecords.put(studentName, temp);
                }else{
                    studentRecords.put(studentName, new HashMap<Integer, String>());
                    studentRecords.get(studentName).put(++recordNumber, bookName);
                }

                record.put(recordNumber, studentName);
                response = ("Your request has been approved, " + recordNumber + " " + studentName + " " + bookName).getBytes();
            }
        }else{
            response = "Request Failed - We do not have this book".getBytes();
        }

        updateInventoryFile();

        return response;
    }

    public static synchronized byte[] returnBook(String[] commandInfo){
        byte[]response = null;

        int number = Integer.parseInt(commandInfo[1].trim());
        if(record.containsKey(number)){
            String student = record.get(number);
            String book = studentRecords.get(student).get(number);
            int numberBooks = inventory.get(book);
            inventory.put(book, ++numberBooks);
            response = (number + " is returned").getBytes();
            studentRecords.get(student).remove(number);
            record.remove(number);
        }else {
            response = (number + " not found, no such borrow record").getBytes();
        }

        updateInventoryFile();

        return response;
    }

    public static synchronized byte[] inventory(){
        return inventoryString().getBytes();
    }

    public static synchronized byte[] list(String[] commandInfo){
        String student = commandInfo[1].trim();
        return getStudentRecord(student).getBytes();
    }

    public static void main(String[]args) {
        int tcpPort;
        int udpPort;
        if(args.length != 1){
            System.out.println("Error: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        //Parse the inventory file
        getInventory(fileName);

        try{
            DatagramSocket datasocket = new DatagramSocket(udpPort);
            DatagramPacket datapacket, returnpacket;

            while(true){

                byte[] buf = new byte[1024];

                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);

                DatagramSocket serversocketUDP = new DatagramSocket();
                ServerSocket serversocketTCP = new ServerSocket(0);

                byte[] rbuf = new byte[1024];
                String udpUniquePort = Integer.toString(serversocketUDP.getLocalPort());
                String tcpUniquePort = Integer.toString(serversocketTCP.getLocalPort());
                rbuf = (udpUniquePort + " " + tcpUniquePort ).getBytes();
                returnpacket = new DatagramPacket(rbuf, rbuf.length, datapacket.getAddress(), datapacket.getPort());
                datasocket.send(returnpacket);

                Thread workerThread = new Thread(new BookWorker(serversocketUDP, serversocketTCP));
                workerThread.start();
            }


        }catch (Exception e){

        }

    }


}
