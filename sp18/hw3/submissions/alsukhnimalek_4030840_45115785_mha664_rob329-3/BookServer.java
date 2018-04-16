import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class BookServer {
    static Semaphore inventorySema = new Semaphore(1);
    static ArrayList<String> books = new ArrayList<>();
    static HashMap<String, Integer> inventory = new HashMap<>();
    static HashMap<String, ArrayList<String>>  personToBooks = new HashMap<>();
    static HashMap<Integer, String> idToBook = new HashMap<>();
    static HashMap<Integer, String> idToPerson = new HashMap<>();
    static HashMap<String, ArrayList<Integer>> personToId = new HashMap<>();
    static ArrayList<String> leaveMeAlone = new ArrayList<>();
    static int recordId = 1;

    public static void main (String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }

        String fileName = args[0];
        int tcpPort = 7000;
        int udpPort = 8000;

        // parse the inventory file
        try {
            Scanner sc = new Scanner(new FileReader(fileName));
            while(sc.hasNextLine()) {
                String inventoryLine = sc.nextLine();
                char[] tokens = inventoryLine.toCharArray();
                String bookName = "";
                String amount = "";
                int i = 0;
                bookName += tokens[i];
                for (i = 1; i < tokens.length; i++) {
                    if (tokens[i] == '"') {
                        break;
                    }
                    bookName += tokens[i];
                }
                bookName += tokens[i];

                for (i = i + 2; i < tokens.length; i++){
                    amount += tokens[i];
                }

                books.add(bookName);
                inventory.put(bookName, Integer.parseInt(amount));
            }

            Thread udpThread = new Thread(new UdpServer(udpPort));
            Thread tcpThread = new Thread(new TcpServer(tcpPort));

            udpThread.start();
            tcpThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * will borrow a book and update all relevant data structures
     * @param tokens
     * @return a response for borrow command
     */
    private static String borrow(String[] tokens){
        String response = "";
        String bookName = "";
        bookName += tokens[2];
        for(int i = 3; i < tokens.length - 1; i++){
            bookName += " ";
            bookName += tokens[i];
        }

        if (!inventory.containsKey(bookName)) {
            response = "Request Failed - We do not have this book";
        } else if(inventory.get(bookName) == 0) {
            response = "Request Failed - Book not available";
        } else {
            response = "Your request has been approved, ";
            response += recordId;
            response += " ";
            response += tokens[1];
            response += " ";
            response += bookName;

            if(!personToBooks.containsKey(tokens[1])){
                ArrayList<String> currBooks = new ArrayList<>();
                currBooks.add(bookName);
                personToBooks.put(tokens[1], currBooks);

                ArrayList<Integer> currIds = new ArrayList<>();
                currIds.add(recordId);
                personToId.put(tokens[1], currIds);
            } else {
                ArrayList<String> currBooks = personToBooks.get(tokens[1]);
                currBooks.add(bookName);
                personToBooks.put(tokens[1], currBooks);

                ArrayList<Integer> currIds = personToId.get(tokens[1]);
                currIds.add(recordId);
                personToId.put(tokens[1], currIds);
            }

            idToBook.put(recordId, bookName);
            idToPerson.put(recordId, tokens[1]);
            recordId++;

            int i = inventory.get(bookName);
            i--;
            inventory.put(bookName, i);
        }

        return response;
    }

    /**
     * will return a book and update all relevant data structures
     * @param tokens
     * @return a response for return command
     */
    private static String returnBook(String[] tokens){
        String response = "";
        int id = Integer.parseInt(tokens[1]);

        String bookName = idToBook.get(id);
        idToBook.remove(id);

        String personName = idToPerson.get(id);

        ArrayList<String> books = personToBooks.get(personName);
        books.remove(bookName);
        personToBooks.put(personName, books);

        ArrayList<Integer> ids = personToId.get(personName);
        ids.remove(ids.indexOf(id));
        personToId.put(personName, ids);

        int i = inventory.get(bookName);
        i++;
        inventory.put(bookName, i);

        response += id;
        response += " is returned";

        return response;
    }

    /**
     * will return the inventory list and update all relevant data structures
     * @return a response for inventory command
     */
    private static String inventory(){
        String response = "";

        // adding first book separately to avoid having \n at end
        if(books.size() > 0){
            for(String book : books){
                response += book;
                response += " ";
                response += inventory.get(book);
                break;
            }
        }

        boolean ignore = true;

        for(String book : books){
            if(!ignore) {
                response += System.lineSeparator();
                response += book;
                response += " ";
                response += inventory.get(book);
            }
            ignore = false;
        }

        return response;
    }

    /**
     * will return the list for a person and update all relevant data structures
     * @param tokens
     * @return a response for list command
     */
    private static String list(String[] tokens){
        String response = "";

        ArrayList<Integer> ids = personToId.get(tokens[1]);

        if(ids != null){
            for(Integer id : ids){
                response += id;
                response += " ";
                response += idToBook.get(id);
            }
        }

        boolean ignore = true;

        for(Integer id : ids){
            if(!ignore) {
                response += '\n';
                response += id;
                response += " ";
                response += idToBook.get(id);
            }
            ignore = false;
        }

        return response;
    }

    /**
     * will exit server for certain clientId and update all relevant data structures
     * @param tokens
     */
    private static void exit(String[] tokens){

        String clientId = tokens[1];
        leaveMeAlone.add(clientId);

        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter("inventory.txt", false); //true tells to append data.
            out = new BufferedWriter(fstream);
            out.write(inventory());
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class UdpServer implements Runnable{
        int port;

        private UdpServer(int portNum){
            this.port = portNum;
        }

        @Override
        public void run() {
            try {
                DatagramSocket dataSocket = new DatagramSocket(port);
                byte[] buff = new byte[1024];
                DatagramPacket recievePacket = new DatagramPacket(buff, buff.length);

                while(true) {
                    dataSocket.receive(recievePacket);
                    String cmd = new String(recievePacket.getData(), 0, recievePacket.getLength());
                    String[] tokens = cmd.split(" ");

                    DatagramPacket returnPacket = null;
                    String response = "";

                    inventorySema.acquire();
                    if(leaveMeAlone.contains(tokens[tokens.length - 1])){
                        continue;
                    }

                    if (tokens[0].equals("borrow")) {

                        response = borrow(tokens);
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);

                    } else if (tokens[0].equals("return")) {

                        response = returnBook(tokens);
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);

                    } else if (tokens[0].equals("inventory")) {

                        response = inventory();
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);

                    } else if (tokens[0].equals("list")) {

                        response = list(tokens);
                        returnPacket = new DatagramPacket(response.getBytes(), response.length(), recievePacket.getAddress(), recievePacket.getPort());
                        dataSocket.send(returnPacket);

                    } else if (tokens[0].equals("exit")) {

                        exit(tokens);
                    } else {
                        System.out.println("ERROR: No such command");
                    }
                    inventorySema.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class TcpServer implements Runnable{
        int port;

        private TcpServer(int portNum){
            this.port = portNum;
        }

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream());

                    String cmd = input.readLine();

                    while(cmd != null) {
                        String response = "";
                        String[] tokens = cmd.split(" ");

                        inventorySema.acquire();
                        if (leaveMeAlone.contains(tokens[tokens.length - 1])) {
                            break;
                        }
                        if (tokens[0].equals("borrow")) {

                            response = borrow(tokens);
                            outputStream.println(response);
                            outputStream.flush();

                        } else if (tokens[0].equals("return")) {

                            response = returnBook(tokens);
                            outputStream.println(response);
                            outputStream.flush();

                        } else if (tokens[0].equals("inventory")) {

                            response = inventory();
                            outputStream.println(response);
                            outputStream.flush();

                        } else if (tokens[0].equals("list")) {

                            response = list(tokens);
                            outputStream.println(response);
                            outputStream.flush();

                        } else if (tokens[0].equals("exit")) {

                            exit(tokens);
                            inventorySema.release();
                            break;
                        } else {
                            System.out.println("ERROR: No such command");
                        }
                        inventorySema.release();
                        cmd = input.readLine();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}