import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Collections;

public class BookServer {
    public class BorrowRecord implements Comparable<BorrowRecord> {
        public Integer number;
        public String person;
        public String book;

        public BorrowRecord(Integer number, String person, String book) {
            this.number = number;
            this.person = person;
            this.book = book;
        }

        @Override
        public int compareTo(BorrowRecord o)
        {
            return(number - o.number);
        }
    }
    public ArrayList<String> inventoryOrder;
    public Map<String, Integer> inventory;
    public Map<Integer, BorrowRecord> records = new HashMap<Integer, BorrowRecord>();

    public Integer recordCount = 1;

    public ReentrantLock invLock = new ReentrantLock();
    public ReentrantLock recordsLock = new ReentrantLock();
    public ReentrantLock recordCountLock = new ReentrantLock();
    // public ReentrantLock lock = new ReentrantLock();

    public String borrow(String book, String person) {
        // lock.lock();
        invLock.lock();
        recordsLock.lock();
        recordCountLock.lock();

        if (!inventory.containsKey(book) || inventory.get(book) <= 0) {
            invLock.unlock();
            recordsLock.unlock();
            recordCountLock.unlock();
            
            if(!inventory.containsKey(book))
                return "Request Failed - We do not have this book\n";
            else
                return "Request Failed - Book not available\n";
        }

        inventory.put(book, inventory.get(book)-1);

        Integer newRecordCount = recordCount;
        recordCount++;

        BorrowRecord br = new BorrowRecord(newRecordCount, person, book);

        records.put(newRecordCount, br);

        invLock.unlock();
        recordsLock.unlock();
        recordCountLock.unlock();
        // lock.unlock();
        
        return "Your request has been approved, " + newRecordCount.toString() + " " + person + " " + book + "\n";
    }

    public String returnBook(Integer record) {
        invLock.lock();
        recordsLock.lock();

        if (!records.containsKey(record)) {
            invLock.unlock();
            recordsLock.unlock();
            return record.toString() + " not found, no such borrow record\n";
        }

        String person = records.get(record).person;
        String book = records.get(record).book;

        inventory.put(book, inventory.get(book)+1);
        records.remove(record);

        invLock.unlock();
        recordsLock.unlock();

        return record.toString() + " is returned\n";
    }

    public String list(String person) {
        recordsLock.lock();

        List<BorrowRecord> borrows = new ArrayList<BorrowRecord>();

        for (Map.Entry<Integer, BorrowRecord> entry : records.entrySet()) {
            String p = entry.getValue().person;

            if (p.equals(person))
                borrows.add(entry.getValue());
        }

        if (borrows.size() == 0) {
            recordsLock.unlock();
            return "No record found for " + person + "\n";
        }

        String result = "";

        for (BorrowRecord br : borrows) {
            result += br.number + " " + br.book + "\n";
        }

        Collections.sort(borrows);

        recordsLock.unlock();
        return result;
    }

    public String printInventory() {
        invLock.lock();

        String result = "";
        for(int i = 0; i<inventoryOrder.size(); i++){
        	String b = inventoryOrder.get(i);
        	Integer n = inventory.get(b);
        	result += b + " " + n.toString() + "\n";
        }

        invLock.unlock();
        return result;
    }

    public String getBookName(String s) {
        int first = s.indexOf("\"");
        int last = s.lastIndexOf("\"") + 1;
        return s.substring(first, last);
    }

    public void clientExit() {
        PrintWriter printWriter = null;

        try {
            FileWriter fileWriter = new FileWriter("inventory.txt");
            printWriter = new PrintWriter(fileWriter);

            printWriter.print(printInventory());

        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (printWriter != null)
                printWriter.close();
        }
    }

    public class UdpWorker implements Runnable {
        private DatagramSocket sock;
        private DatagramPacket pack;
        
        public UdpWorker(DatagramSocket sock, DatagramPacket pack) {
            this.sock = sock;
            this.pack = pack;
        }

        @Override
        public void run() {
            InetAddress address = pack.getAddress();
            int port = pack.getPort();
            byte[] buf = pack.getData();
            String cmd = new String(buf, 0, buf.length);
            String[] tokens = cmd.split(" ");
            String response = null;

            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokens[i].trim();
            }

            if (tokens[0].equals("borrow")) {
                String book = getBookName(cmd);
                String student = tokens[1];

                response = borrow(book, student);
            } else if (tokens[0].equals("return")) {
                Integer rec = Integer.parseInt(tokens[1]);
                response = returnBook(rec);
            } else if (tokens[0].equals("inventory")) {
                response = printInventory();
            } else if (tokens[0].equals("list")) {
                String student = tokens[1];
                response = list(student);
            } else if (tokens[0].equals("exit")) {
                clientExit();
            } else {
                System.out.println("ERROR: No such command");
            }

            if (response != null) {
                byte[] resp = response.getBytes();
                pack = new DatagramPacket(resp, resp.length, address, port);

                try {
                    sock.send(pack);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public class UdpListener implements Runnable {
        private DatagramSocket sock;
        
        public UdpListener(DatagramSocket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket udpPacket = new DatagramPacket(buf, buf.length);

                try {
                    sock.receive(udpPacket);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                new Thread(new UdpWorker(sock, udpPacket)).start();
            }
        }
    }

    public class TcpWorker implements Runnable {
        private Socket sock;

        public TcpWorker(Socket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            BufferedReader read = null;
            DataOutputStream write = null;
            PrintWriter print = null;

            try {
                read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                write = new DataOutputStream(sock.getOutputStream());
                print = new PrintWriter(write, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    String cmd = read.readLine();

                    if (cmd == null)
                        break;

                    String[] tokens = cmd.split(" ");
                    String response = null;

                    for (int i = 0; i < tokens.length; i++) {
                        tokens[i] = tokens[i].trim();
                    }

                    if (tokens[0].equals("borrow")) {
                        String book = getBookName(cmd);
                        String student = tokens[1];

                        response = borrow(book, student);
                    } else if (tokens[0].equals("return")) {
                        Integer rec = Integer.parseInt(tokens[1]);
                        response = returnBook(rec);
                    } else if (tokens[0].equals("inventory")) {
                        response = printInventory();
                    } else if (tokens[0].equals("list")) {
                        String student = tokens[1];
                        response = list(student);
                    } else if (tokens[0].equals("exit")) {
                        clientExit();
                    } else {
                        System.out.println("ERROR: No such command");
                    }

                    if (response != null) {
                        response += "224cbea3-ffc8-48e9-8aad-1079474b35db\n";
                        print.println(response);
                        print.flush();
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                    break;
                }
            }
        }
    }

    public class TcpListener implements Runnable {
        private ServerSocket sock;
        
        public TcpListener(ServerSocket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket clientSock = sock.accept();

                    new Thread(new TcpWorker(clientSock)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main (String[] args) {
        if (args.length != 1) {
          System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
          System.exit(-1);
        }

        BookServer bookserver = new BookServer(args[0]);
    }

    public BookServer(String fileName) {
        int tcpPort;
        int udpPort;

        tcpPort = 7000;
        udpPort = 8000;
  
        // parse the inventory file
        inventory = new HashMap<String, Integer>();
        inventoryOrder = new ArrayList<String>();

        Scanner inventoryScanner = null;
        try {
            inventoryScanner = new Scanner(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
  
        while (inventoryScanner.hasNextLine()) {
            String line = inventoryScanner.nextLine();
            String[] tokens = line.split(" ");
            String name = "";
  
            for (int i = 0; i < tokens.length-1; i++)
                name += tokens[i]+" ";
            name = name.substring(0, name.length()-1);
            String numStr = tokens[tokens.length-1];
  			inventoryOrder.add(name);
            inventory.put(name, Integer.parseInt(numStr));
        }

        DatagramSocket udpSocket = null;
        ServerSocket tcpSocket = null;
        try {
            udpSocket = new DatagramSocket(udpPort);
            tcpSocket = new ServerSocket(tcpPort);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        UdpListener udpListen = new UdpListener(udpSocket);
        Thread udpThread = new Thread(udpListen);

        TcpListener tcpListen = new TcpListener(tcpSocket);
        Thread tcpThread = new Thread(tcpListen);

        udpThread.start();
        tcpThread.start();

        try {
            udpThread.join();
            tcpThread.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        
        try {
            udpSocket.close();
            tcpSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}


