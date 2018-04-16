import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class BookServer {

    LinkedList<BookNode> inventory;
    Map<String, ArrayList<BookData>> borrow;
    Map<Integer, String> records;
    Map<Integer, String> recordIdToOwner;
    int recordId;

    public BookServer() {
        inventory = new LinkedList<BookNode>();
        borrow = new HashMap<>();
        records = new HashMap<>();
        recordIdToOwner = new HashMap<>();
        recordId = 0;
    }

    public void addToInventory(BookNode bookNode) {
        inventory.addLast(bookNode);
    }

    public synchronized int borrows(String title, String name) {
        for (BookNode n : inventory) {
            if (n.title.equals(title)) {
                if (n.amount <= 0) return 0;
                recordId++;
                records.put(recordId, title);
                recordIdToOwner.put(recordId, name);
                if (!borrow.containsKey(name)) {
                    ArrayList<BookData> l = new ArrayList<>();
                    l.add(new BookData(title, recordId));
                    borrow.put(name, l);
                } else {
                    ArrayList<BookData> l = borrow.get(name);
                    l.add(new BookData(title, recordId));
                }
                n.amount -= 1;
                return recordId;
            }
        }
        return -1;
    }

    public synchronized boolean returnBook(int recordId) {
        if (records.containsKey(recordId)) {
            String title = records.get(recordId);
            records.remove(recordId);
            for (BookNode n : inventory) {
                if (n.title.equals(title)) {
                    n.amount += 1;
                    String owner = recordIdToOwner.get(recordId);
                    ArrayList<BookData> l = borrow.get(owner);
                    for (int i = 0; i < l.size(); i++) {
                        if (l.get(i).title.equals(title)) {
                            l.remove(i);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public String messageHandler(String received) {
        String[] split = received.split(" ");
        String cmd = split[0];
        if (cmd.equals("borrow")) {
            String name = split[1];
            StringBuilder title = new StringBuilder();
            for (int i = 2; i < split.length - 1; i++)
                title.append(split[i] + " ");
            title.append(split[split.length - 1]);
            System.out.println("TITLE:" + title.toString());
            int id = (borrows(title.toString(), name));
            if (id >= 1) return "Your request has been approved, " + id + " " + name + " " + title;
            else if (id == 0) return "Request Failed - Book not available";
            else if (id == -1) return "Request Failed - We do not have this book";

        } else if (cmd.equals("list")) {
            String name = split[1];
            String ret = getNameList(name);
            if(ret.equals("")) return "No record found for " + name;
            return ret;
        } else if (cmd.equals("return")) {
            int id = Integer.parseInt(split[1]);
            if (returnBook(id)) {
                return id + " is returned";
            } else return id + " not found, no such borrow record";

        } else if (cmd.equals("inventory")) {
            return getInventory();

        } else if (cmd.equals("exit")) {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new File("inventory.txt"));
                writer.append(getInventory());
                writer.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                writer.close();
            }
            return "EXIT";
        }
        return "ERROR";
    }

    public synchronized String getNameList(String name) {
        StringBuilder sb = new StringBuilder();
        if(!borrow.containsKey(name)) return "";
        boolean first = true;
        ArrayList<BookData> l = borrow.get(name);
        for (BookData b : l) {
            if(!first) sb.append('\n');
            first = false;
            sb.append(b.record + " " + b.title);
        }
        return sb.toString();

    }

    public synchronized String getInventory() {
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (BookNode n : inventory) {
            if (!first) s.append("\n");
            first = false;
            s.append(n.toString());
        }
        return s.toString();
    }

    public static void main(String[] args) {
        int tcpPort;
        int udpPort;

        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        BookServer bookServer = new BookServer();
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        // parse the inventory file
        Scanner libBuilder = null;
        try {
            libBuilder = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (libBuilder.hasNextLine()) {
            String line = libBuilder.nextLine();
            StringBuilder title = new StringBuilder();
            String[] split = line.split("\"");
            title.append("\"");
            title.append(split[1]);
            title.append("\"");
            int amount = Integer.parseInt(split[split.length - 1].substring(1));
            BookNode n = new BookNode(title.toString(), amount);
            bookServer.addToInventory(n);
        }


        TCPServerSocket tcpServerSocket = new TCPServerSocket(bookServer);
        Thread tcp = new Thread(tcpServerSocket);
        tcp.start();
        // create two threads one for udp one for tcp
        UDPServerSocket udpServerSocket = new UDPServerSocket(bookServer);
        Thread udp = new Thread(udpServerSocket);
        udp.start();
    }

    private static class BookNode {
        String title;
        int amount;

        public BookNode(String title, int amount) {
            this.title = title;
            this.amount = amount;
        }

        public String toString() {
            return title + " " + amount;
        }

        public String getTitle() {
            return title;
        }

        public int getAmount() {
            return amount;
        }
    }

    private class BookData {
        String title;
        int record;

        public BookData(String title, int record) {
            this.title = title;
            this.record = record;
        }
    }
}