import java.io.FileNotFoundException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class BookServer {
    static final int tcpPort = 7000;
    static final int udpPort = 8000;
    private static ConcurrentMap<String, AtomicInteger> inventory;
    private static ConcurrentMap<Integer, Record> records;
    private static ConcurrentMap<String, ConcurrentLinkedQueue<Record>> student_logs;
    private static List<String> book_list;
    private static AtomicInteger record_number;
    private static ReentrantLock lock;

    private static void write_to_file() {
        lock.lock();
        File file = new File("inventory.txt");
        FileWriter fr = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fr = new FileWriter(file);
            fr.write(String.join("\n", book_list.stream().
                    map(b -> b + " " + inventory.get(b).get()).collect(Collectors.toList())) + "\n");
        } catch (Exception e) {}
        finally {
            try {
                fr.close();
            } catch (Exception e) {}
            lock.unlock();
        }
    }

    private static String processCommand(String command) {
        lock.lock();
        // TODO: interpret what the command does
        try {
            Scanner sc = new Scanner(command);
            String type = sc.next();
            if (type.equals("borrow")) {
                String student = sc.next();
                String book_name = sc.nextLine().trim();
                AtomicInteger count = inventory.getOrDefault(book_name, null);
                if (count == null) {
                    return "Request Failed - We do not have this book";
                }
                if (count.getAndUpdate(x -> Math.max(0, x - 1)) == 0) {
                    return "Request Failed - Book not available";
                }
                int num = record_number.getAndIncrement();
                Record current = new Record(num, student, book_name);
                records.put(num, current);
                student_logs.compute(student, (k, v) -> {
                    if (v == null) {
                        ConcurrentLinkedQueue<Record> list = new ConcurrentLinkedQueue<>();
                        list.add(current);
                        return list;
                    } else {
                        v.add(current);
                        return v;
                    }
                });
                // the doc has a typo here but the expected output has it correct
                return String.format("Your request has been approved, %d %s %s", num, student, book_name);
            } else if (type.equals("return")) {
                int num = sc.nextInt();
                Record invalid = new Record(-1);
                Record r = records.replace(num, invalid);
                if (r == null || r.val == -1) {
                    return num + " not found, no such borrow record";
                }
                records.remove(num);
                AtomicInteger count = inventory.get(r.book);
                count.incrementAndGet();
                student_logs.get(r.student).remove(r);
                return num + " is returned";
            } else if (type.equals("list")) {
                String student = sc.next();
                if (!student_logs.containsKey(student)) {
                    return "No record found for " + student;
                }
                List<String> l = student_logs.get(student).stream()
                        .map(r -> r.val + " " + r.book)
                        .collect(Collectors.toList());
                if (l.size() == 0) {
                    return "No record found for " + student;
                }
                return l.size() + "\n" + String.join("\n", l);
            } else if (type.equals("inventory")) {
                List<String> l = book_list.stream().
                        map(b -> b + " " + inventory.get(b).get()).collect(Collectors.toList());
                return l.size() + "\n" + String.join("\n", l);
            } else {
                System.out.println("ERROR: No such command");
            }
            return "";
        }
        finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];

        inventory = new ConcurrentHashMap<>();
        records = new ConcurrentHashMap<>();
        student_logs = new ConcurrentHashMap<>();
        book_list = new ArrayList<>();
        record_number = new AtomicInteger(1);
        lock = new ReentrantLock(true);
        // parse the inventory file
        try {
            Scanner scan = new Scanner(new FileReader(fileName));
            while (scan.hasNextLine()) {
                String cmd = scan.nextLine();
                String[] tokens = cmd.split("(?<=\") ");
                inventory.putIfAbsent(tokens[0], new AtomicInteger(Integer.parseInt(tokens[1])));
                book_list.add(tokens[0]);
            }
        } catch (FileNotFoundException e) {}
        // TODO: handle request from clients (probably need to handle TCP and UDP concurrently)
        try {
            // Start UDP listener
            DatagramSocket datasocket = new DatagramSocket(udpPort);
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                datasocket.receive(packet);
                String init = new String(packet.getData(), 0, packet.getLength());
                // This should always happen
                if (init.equals("init")) {
                    UDPServerThread t = new UDPServerThread();
                    t.start();
                    String port_num = "" + t.socket.getLocalPort();
                    byte[] init_response = port_num.getBytes();
                    DatagramPacket init_packet = new DatagramPacket(init_response, init_response.length, packet.getAddress(), packet.getPort());
                    datasocket.send(init_packet);
                }
            }
        } catch (Exception t) {
        }
    }

    static class Record {
        int val;
        String student;
        String book;
        public Record(int v) {
            val = v;
        }

        public Record(int v, String s, String b) {
            val = v;
            student = s;
            book = b;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Record)) {
                return false;
            }

            Record r = (Record) o;
            return val == r.val && student.equals(r.student) && book.equals(r.book);
        }
    }

    static class TCPServerThread extends Thread {
        ServerSocket listener;
        public TCPServerThread () {
            try {
                listener = new ServerSocket(0); // automatically allocates a port
            } catch (Exception e) {}
        }

        public void run() {
            try {
                Socket client = listener.accept();
                Scanner sc = new Scanner(client.getInputStream());
                PrintWriter pout = new PrintWriter(client.getOutputStream());
                boolean loop = true;
                while (loop) {
                    String command = sc.nextLine();
                    String msg;
                    if (command.startsWith("exit")) {
                        write_to_file();
                        msg = "";
                        loop = false;
                    } else if (command.startsWith("switching")) {
                        UDPServerThread t = new UDPServerThread();
                        t.start();
                        msg = "" + t.socket.getLocalPort();
                        loop = false;
                    } else {
                        msg = processCommand(command);
                    }
                    pout.println(msg);
                    pout.flush();
                }
                sc.close();
                pout.close();
                client.close();
            } catch (IOException e) {}
        }
    }

    static class UDPServerThread extends Thread {
        DatagramSocket socket;
        public UDPServerThread () {
            try {
                socket = new DatagramSocket();
            } catch (Exception e) {}
        }

        public void run() {
            try {
                boolean loop = true;
                while (loop) {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String command = new String(packet.getData(), 0, packet.getLength());
                    byte[] message;
                    if (command.startsWith("exit")) {
                        write_to_file();
                        message = "".getBytes();
                        loop = false;
                    }
                    else if (command.startsWith("switching")) {
                        TCPServerThread t = new TCPServerThread();
                        t.start();
                        message = ("" + t.listener.getLocalPort()).getBytes();
                        loop = false;
                    } else {
                        String res = processCommand(command);
                        message = res.getBytes();
                    }
                    DatagramPacket return_packet = new DatagramPacket(message, message.length, packet.getAddress(), packet.getPort());
                    socket.send(return_packet);
                }
            } catch (Exception e) {}
            socket.close();
        }
    }
}
