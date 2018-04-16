import java.util.concurrent.*;
import java.io.*;
import java.util.*;
import java.net.*;


public class BookServer {

    private static final int TCP_PORT = 7000;
    private static final int UDP_PORT = 8000;
    private static ExecutorService threadPool;
    static Inventory i;
    private static Map<String, Boolean> isUDP;

    public static void main (String[] args) {

        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        i = new Inventory(fileName);
        isUDP = Collections.synchronizedMap(new HashMap<String, Boolean>());
        threadPool = Executors.newCachedThreadPool();

        threadPool.execute(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(TCP_PORT);
            } catch(IOException e) {
                e.printStackTrace();
                return;
            }
            while(true){
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                threadPool.execute(new tcpClientHandler(clientSocket));
            }
        });

        threadPool.execute(() -> {
            DatagramSocket datagramSocket = null;
            byte[] sizeBuffer = new byte[100];
            String message = null;

            try {
                datagramSocket = new DatagramSocket(UDP_PORT);
            } catch(IOException e) {
                e.printStackTrace();
            }
            DatagramPacket receive = null;
            while(true) {
                try {
                    byte[] databuffer = new byte[10000];
                    receive = new DatagramPacket(databuffer, databuffer.length);
                    datagramSocket.receive(receive);
                    message = new String(receive.getData(), 0, receive.getLength());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                threadPool.execute(new udpClientHandler(datagramSocket, message, receive.getAddress(), receive.getPort()));
            }
        });
    }

    static class tcpClientHandler implements Runnable{
        Socket clientSocket = null;

        public tcpClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                BufferedReader inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter outStream = new PrintWriter(clientSocket.getOutputStream());

                String[] message = inStream.readLine().split(" ", 2);
                String reply = null;
                if(isUDP.get(message[0]) == null || isUDP.get(message[0])) {
                    reply = "ERROR: Wrong protocol";
                } else {
                    reply = doCommand(message[1]);
                }

                if(reply.equals("exit")){
                    isUDP.put(message[0], null);
                    reply = "";
                } else if(reply.equals("tcp")){
                    isUDP.put(message[0], false);
                    reply = "";
                } else if(reply.equals("udp")){
                    isUDP.put(message[0], true);
                    reply = "";
                }

                outStream.println(reply);
                outStream.flush();
                inStream.close();
                outStream.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static class udpClientHandler implements Runnable{
        DatagramSocket socket;
        String msg;
        InetAddress addr;
        int port;

        public udpClientHandler(DatagramSocket socket, String message, InetAddress address, int port) {
            this.socket = socket;
            msg = message;
            addr = address;
            this.port = port;
        }

        public void run() {
            String[] message = msg.split(" ", 2);
            String reply = null;
            if(isUDP.get(message[0]) != null && !isUDP.get(message[0])) {
                reply = "ERROR: Wrong protocol";
            } else {
                reply = doCommand(message[1]);
            }
            if (reply != null) {
                if(reply.equals("exit")){
                    isUDP.put(message[0], null);
                    reply = "";
                }
                if(reply.equals("tcp")){
                    isUDP.put(message[0], false);
                    reply = "";
                }
                if(reply.equals("udp")){
                    isUDP.put(message[0], true);
                    reply = "";
                }
            }
            try {
                DatagramPacket returnPacket = new DatagramPacket(
                        reply.getBytes(),
                        reply.length(),
                        addr,
                        port);
                socket.send(returnPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized static String doCommand(String message){
        String [] messageTokens = message.split(" ");
        switch(messageTokens[0].trim()){
            case "setmode":
                if(messageTokens[1].trim().equals("T"))
                    return "tcp";
                else
                    return "udp";
            case "borrow":
                int transNum  = i.addTransaction(messageTokens[1].trim(), message.substring(message.indexOf('"')).trim());
                if(transNum >= 0)
                    return "Your request has been approved, " + transNum + " " + messageTokens[1].trim() + " "  + message.substring(message.indexOf('"')).trim();
                else if(transNum == -1)
                    return "Request Failed - Book not available";
                else 
                    return "Request Failed - We do not have this book";
            case "return":
               boolean b = i.returnBook(Integer.parseInt(messageTokens[1].trim()));
               if(b)
                   return messageTokens[1].trim() + " is returned";
               else
                   return messageTokens[1].trim() + " not found, no such borrow record";
            case "list":
                return i.studentList(messageTokens[1].trim());
            case "inventory":
                return i.toString();
            case "exit":
                try{
                    PrintWriter out = new PrintWriter(new FileWriter("inventory.txt"));
                    out.print(i.toString());
                    out.close();
                return "exit";
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return "exit";
            default:
                return "NOT A VALID COMMAND";

        }

    }
}

