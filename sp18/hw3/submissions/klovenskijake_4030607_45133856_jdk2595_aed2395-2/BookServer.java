import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.LinkedList;

public class BookServer {
    static LinkedList<Book> inventory;
    static LinkedList<Transaction> transactionList;

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
        inventory = new LinkedList<Book>();
        try {
            Scanner sc = new Scanner(new FileReader(args[0]));
            while(sc.hasNextLine()) {
                String readIn = sc.nextLine();
                int split = readIn.lastIndexOf("\"") + 1;
                String title = readIn.substring(0,split);
                int stock = Integer.parseInt(readIn.substring(split + 1));
                Book nextBook = new Book(title,stock);
                inventory.add(nextBook);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        transactionList = new LinkedList<Transaction>();
        // TODO: handle request from clients
        ExecutorService es = Executors.newCachedThreadPool();
        Librarian tcpLibrarian = new Librarian(tcpPort,es);
        Librarian udpLibrarian = new Librarian(udpPort,es);
        es.submit(tcpLibrarian);
        es.submit(udpLibrarian);
    }


    //If the book is in our inventory at all.
    synchronized private static boolean searchInventory(String book){
        for(Book b: inventory){
            if(b.getBookName().equals(book)){
                return true;
            }
        }
        return false;
    }

    //if the book is one that is carried.
    synchronized private static Book searchBook(String book) {
        for (Book b : inventory) {
            if (b.getBookName().equals(book)) {
                return b;
            }
        }
        return null;
    }
    //Lets a student check out a book returns the id if successful and returns a -1 if failed.

    private static class Book {
        //For use with the BookServer.
        private String bookName;
        private int stock;

        //For use within student class for easy listing.
        public Book(String bookName, int num){
            this.bookName = bookName;
            this.stock = num;
        }

        synchronized public boolean checkOut(){
            if(this.stock > 0) {
                this.stock -= 1;
                return true;
            }
            return false;
        }

        synchronized public int ret(){
            this.stock+=1;
            return this.stock;
        }

        public String getBookName() {
            return bookName;
        }

        public int getStock() {
            return stock;
        }
    }

    private static class Transaction {
        int ID;
        String student;
        String bookTitle;
        static int idCounter = 1;

        public Transaction(String student, String bookTitle) {
            this.ID = idCounter++;
            this.student = student;
            this.bookTitle = bookTitle;
        }

        public int getID() {
            return ID;
        }

        public String getStudent() {
            return student;
        }

        public String getBookTitle() {
            return bookTitle;
        }
    }
    private static class Librarian implements Runnable {
        int port;
        ExecutorService es;
        public Librarian(int port, ExecutorService es) {
            this.port = port;
            this.es = es;
        }
        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(this.port);
                while(true) {
                    Socket s = ss.accept();
                    SocketHandler sh = new SocketHandler(s);
                    es.submit(sh);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class SocketHandler implements Runnable {
        Socket s;
        public SocketHandler(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String output, input;
                boolean run = true;
                while(run) {
                    while((input = in.readLine()) != null) {
                        String[] tokens = input.split(" ");
                        if(tokens[0].equals("borrow")) {
                            // borrow <student> <book title>
                            String bookTitle = tokens[2];
                            if(tokens.length > 3) {
                                for(int i = 3; i < tokens.length; i++) {
                                    bookTitle+= " " + tokens[i];
                                }
                            }
                            if(!searchInventory(bookTitle)) {
                                out.println("Request Failed - We do not have this book");
                                break;
                            }
                            Book book = searchBook(bookTitle);

                            if(!book.checkOut()) {
                                out.println("Request Failed - Book not available");
                                break;
                            }
                            Transaction trans = new Transaction(tokens[1], bookTitle);
                            transactionList.add(trans);
                            out.println("Your request has been approved, " +
                                    trans.getID() + " " + trans.getStudent() + " " + trans.getBookTitle());
                        }

                        else if(tokens[0].equals("return")) {
                            // return <bookID>
                            int transID = Integer.parseInt(tokens[1]);
                            Transaction trans = null;
                            for(int i = 0; i < transactionList.size();i++) {
                                if(transactionList.get(i).getID() == transID) {
                                    trans = transactionList.remove(i);
                                    break;
                                }
                            }
                            if(trans == null) {
                                out.println(transID + " not found, no such borrow record");
                            }
                            Book book = searchBook(trans.getBookTitle());
                            book.ret();
                            out.println(transID + " is returned");
                        }
                        else if(tokens[0].equals("list")) {
                            // list <Student name>
                            LinkedList<Transaction> studentTransactions = new LinkedList<Transaction>();
                            for(int i = 0; i < transactionList.size(); i++) {
                                Transaction t = transactionList.get(i);
                                if(t.getStudent().equals(tokens[1])) {
                                    studentTransactions.add(t);
                                }
                            }
                            if(studentTransactions.size() > 0) {
                                out.println(studentTransactions.size());
                                for (int i = 0; i < studentTransactions.size(); i++) {
                                    Transaction t = studentTransactions.get(i);
                                    out.println(t.getID() + " " + t.getBookTitle());
                                }
                            }
                            else {
                                out.println("No record found for " + tokens[1]);
                            }
                        }
                        else if(tokens[0].equals("inventory")) {
                            // inventory
                            out.println(inventory.size());
                            for(int i = 0; i < inventory.size(); i++) {
                                Book b = inventory.get(i);
                                out.println(b.getBookName() + " " + b.getStock());
                            }
                        }
                        else if(tokens[0].equals("exit")) {
                            s.close();
                            run = false;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}