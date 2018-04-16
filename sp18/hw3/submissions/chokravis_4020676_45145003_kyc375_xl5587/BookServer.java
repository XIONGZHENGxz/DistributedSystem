import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer{
    protected final static boolean DEBUG = false;
    protected static AtomicInteger clients = new AtomicInteger(0);
    protected static BookStorage storage;
    protected static ServerSocket serverSock;
    private int udpPort;
    private int tcpPort;
    private int byteLength;
    protected static boolean isOpen;
    protected static boolean tcpOn = false;
    private static Thread TCP = null;
    //private static HashMap<String, Client> userMap = new HashMap<>();

    public static void main (String[] args) throws Exception{
        parseInput(args);
        BookServer UDP = new BookServer();
        UDP.setupUPD();
    }

    protected static Map<String, Integer> inven(){
        return storage.inventory();
    }

    private static void parseInput(String[] args){
        if (args.length != 1) {
            System.out.println("ERROR: No Argument");
            System.exit(-1);
        }

        Map<String, Integer> inventory = new HashMap<>();
        String fileName = args[0];
        String book = "";

        try{
            Scanner sc = new Scanner(new FileReader(fileName));
            while(sc.hasNext()){
                if(sc.hasNextInt()){
                    int num = sc.nextInt();
                    book = book.substring(0, book.length() - 1);
                    inventory.put(book, num);
                    book = "";
                }else {
                    book = book.concat(sc.next());
                    book = book + " ";
                }
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }

        storage = new BookStorage(inventory);
    }

    public BookServer(){
        udpPort = 8000;
        tcpPort = 7000;
        byteLength = 1024;
        isOpen = true;
    }

    private void setupUPD() throws Exception{
        String send = "UServer";
        byte[] buf = new byte[byteLength];
        DatagramSocket socket = new DatagramSocket(udpPort);
        serverSock = new ServerSocket(tcpPort);
        TCP = new Thread(new TCPServer());
        TCP.start();

        while(isOpen){
            DatagramPacket dpget = new DatagramPacket(buf, byteLength);
            socket.receive(dpget);
            String receive = new String(dpget.getData(), 0, dpget.getLength());
            int port = dpget.getPort();

            if(DEBUG){System.out.println("Server receive: " + receive + " from " +
                    dpget.getAddress().getHostAddress() + ": " + dpget.getPort());}

            send = processCommand(receive);
            if(!send.equals("")){
                if(DEBUG){System.out.println("Server send: " + send);}

                DatagramPacket dpsend = new DatagramPacket(send.getBytes(), send.length(),
                        dpget.getAddress(), dpget.getPort());
                socket.send(dpsend);
                dpget.setLength(byteLength);
            }
        }
    }

    protected static String processCommand(String command){
        String message = "";
        String[] parse = command.split(" ");

        if(parse[0].equals("hello")){
            if(DEBUG){System.out.println("Initial connection established");}
            clients.incrementAndGet();
        }

        else if(parse[0].equals("setmode")) {
            if(parse[1].equals("U"))
                return "U";

            else if(parse[1].equals("T")){

                //while(!tcpOn){}
                return "T";
            }
        }
        else if(parse[0].equals("borrow")){
            synchronized (storage){
                String name = parse[1];
                String book = "";
                for(int i = 2; i < parse.length; i++){
                    book = book + parse[i] + " ";
                }
                book = book.substring(0, book.length()-1);
                int result = storage.borrow(name, book);
                if(result != -1 && result != -2){
                    message = "You request has been approved " + result + " " + name + " " + book;
                }else if(result == -1){
                    message = "Request Failed - Book not available";
                }else{
                    message = "Request Failed - We do not have this book";
                }
            }
            return message;
        }

        else if(parse[0].equals("return")){
            synchronized (storage){
                int id = Integer.parseInt(parse[1]);
                boolean result = storage.return_1(id);
                if(result){
                    message = id + " is returned";
                }else{
                    message = id + " not found, no such borrow record";
                }
            }
            return message;
        }

        else if(parse[0].equals("list")){
            synchronized (storage){
                String name = parse[1];
                Map<Integer, String> result = storage.list(name);
                if(storage.list(name) == null){
                    message = "No record found for " + name + "\n";
                }else{
                    Set<Integer> get = result.keySet();
                    for(Integer o : get){
                        String val = result.get(o);
                        message = message + o + " " + val +"\n";
                    }
                }
                int i = message.lastIndexOf("\n");
                message = message.substring(0, i);
                return message;
            }
        }

        else if(parse[0].equals("inventory")){
            synchronized (storage) {
                Map<String, Integer> quantity = storage.inventory();
                Set<String> result = quantity.keySet();
                for (String good : result) {
                    int val = quantity.get(good);
                    message = message + good + " " + val + "\n";
                }
                int i = message.lastIndexOf("\n");
                message = message.substring(0, i);
                return message;
            }
        }
        else if(parse[0].equals("exit")) {
            clients.decrementAndGet();
            if(clients.intValue() == 0){
                printInvent();
            }
            return "exit";
        }
        else{
            if(DEBUG){System.out.println("ERROR");}
            return "";
        }
        return "";
    }

    protected static void printInvent(){
        synchronized (storage) {
            String message = "";
            PrintStream inven1;
            FileOutputStream iout;
            File invfile = new File("./src", "inventory.txt");
            try {
                iout = new FileOutputStream(invfile);
                inven1 = new PrintStream(iout);
                Map<String, Integer> store = storage.inventory();
                Set<String> result = store.keySet();
                for (String good : result) {
                    int val = store.get(good);
                    message = message + good + " " + val + "\n";
                }
                int i = message.lastIndexOf("\n");
                message = message.substring(0, i);
                inven1.println(message);
                System.setOut(inven1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
