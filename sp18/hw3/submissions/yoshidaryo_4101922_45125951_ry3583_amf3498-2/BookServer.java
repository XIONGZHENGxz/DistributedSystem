import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer {
    private static int recordId = 1;
    private static LinkedHashMap<String, Integer> inventory = new LinkedHashMap<>();
    private static HashMap<Integer, Record> record = new HashMap<>();

    public static void main (String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];

        // parse the inventory file
        try {
            Scanner in = new Scanner(new FileReader(fileName));
            while (in.hasNextLine()) {
                String line = in.nextLine();
                Matcher matcher = Pattern.compile("([^\"]\\S*|\".+\")\\s*").matcher(line);  // quoted strings or non-quoted strings with spaces
                matcher.find();     // find next match
                String title = matcher.group(1);    // no trailing spaces
                matcher.find();
                int num = Integer.parseInt(matcher.group(1));
                inventory.put(title,num);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // initialize UDP and TCP ports
        try {
            ServerSocket TCPServer = new ServerSocket(7000);
            DatagramSocket UDPServer = new DatagramSocket(8000);
            DatagramPacket packet = new DatagramPacket(new byte[1], 1);

            while (true) {
                UDPServer.receive(packet);
                int clientPort = packet.getPort();
                DatagramSocket UDPSocket = new DatagramSocket();
                UDPSocket.send(new DatagramPacket(new byte[1], 1, InetAddress.getByName("localhost"), clientPort));

                Socket TCPSocket = TCPServer.accept();
                new Thread(new ClientHandler(TCPSocket, UDPSocket, clientPort)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized String borrowBook(String name, String book) {
        String response;
        if (inventory.containsKey(book)) {
            if (inventory.get(book) > 0) {
                record.put(recordId, new Record(name, book, recordId));
                inventory.put(book, inventory.get(book) - 1);
                response = "Your request has been approved, " + recordId++ + " " + name + " " + book;
            } else
                response = "Request Failed - Book not available";
        }
        else
            response = "Request Failed - We do not have this book";

        return response;
    }

    static synchronized String returnBook(int id) {
        String response;
        if (record.containsKey(id)) {
            response = id + " is returned";
            Record r = record.get(id);
            inventory.put(r.getBook(), inventory.get(r.getBook()) + 1);
            record.remove(id);
        }
        else
            response = id + " not found, no such borrow record";

        return response;
    }


    static synchronized String[] list(String name, boolean isUDP) throws IOException {
        String[] payload;
        if (!isUDP) {
            int count = 0;
            for (Record record : record.values()) {
                if (record.getName().equals(name)) {
                    count++;
                }
            }

            if (count == 0) {
                payload = new String[2];
                payload[0] = "1";
                payload[1] = "No record found for " + name;
            }
            else {
                payload = new String[count + 1];
                payload[0] = count + "";
                int index = 1;
                for (Record record : record.values()) {
                    if (record.getName().equals(name)) {
                        payload[index] = record.getID() + " " + record.getBook();
                        index++;
                    }
                }
            }

        } else {
            payload = new String[1];
            StringBuilder str = new StringBuilder();
            for (Record record : record.values()) {
                if (record.getName().equals(name))
                    str.append(record.getID()).append(" ").append(record.getBook()).append("\n");
            }
            if (str.toString().isEmpty()) {
                payload[0] = "No record found for " + name;
            }
            else {
                str.deleteCharAt(str.lastIndexOf("\n"));
                payload[0] = str.toString();
            }
        }
        return payload;
    }

    static synchronized String[] inventory(boolean isUDP) throws IOException {
        String[] payload;
        if (!isUDP) {
            payload = new String[inventory.size() + 1];
            payload[0] = inventory.size() + "";
            int index = 1;
            for (String book : inventory.keySet()) {
                payload[index] = book + " " + inventory.get(book);
                index++;
            }
        } else {
            payload = new String[1];
            StringBuilder str = new StringBuilder();
            for (String book : inventory.keySet())
                str.append(book).append(" ").append(inventory.get(book)).append("\n");
            if (!str.toString().isEmpty())
                str.deleteCharAt(str.lastIndexOf("\n"));
            payload[0] = str.toString();
        }
        return payload;
    }

    static synchronized void exit() throws IOException {
        PrintStream out = new PrintStream(new File("inventory.txt"));
        System.setOut(out);
        for (String book : inventory.keySet()) {
            System.out.println(book + " " + inventory.get(book));
        }
        out.close();
    }

}
