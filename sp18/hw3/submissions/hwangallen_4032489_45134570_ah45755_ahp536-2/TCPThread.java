import java.io.*;
import java.util.*;
import java.net.*;

public class TCPThread extends Thread {
    Socket client;
    Library library;

    public TCPThread(Socket s, Library l) {
        client = s;
        library = l;
    }

    public void run() {
        Scanner scan = null;
        try {
            scan = new Scanner(client.getInputStream());
            PrintWriter pout = new PrintWriter(client.getOutputStream());
            while(scan.hasNextLine()) {
                String input = scan.nextLine();
                System.out.println("received: " + input);
                String[] tokens = input.split(" ");
                String command = tokens[0];
                if (command.equals("borrow")) {
                    String studentName = tokens[1];
                    String bookName = "";
                    for(int i = 2; i < tokens.length - 1; i++)
                        bookName += tokens[i] + " ";
                    bookName += tokens[tokens.length - 1]; //Format displays it as "Book Name" instead of "Book Name"_; String.trim does not seem to work.
                    int recordID = library.borrowBook(studentName, bookName);
                    if (recordID > 0)
                        pout.println("Your request has been approved, " + recordID + " " + studentName + " " + bookName);
                    else if (recordID == 0)
                        pout.println("Request Failed - Book not available");
                    else
                        pout.println("Request Failed - We do not have this book");
                    pout.flush();
                } else if (command.equals("return")) {
                    int recordID = Integer.parseInt(tokens[1]);
                    String bookName = library.returnBook(recordID);
                    if (!bookName.equals(""))
                        pout.println(recordID + " is returned");
                    else
                        pout.println(recordID + " not found, no such borrow record");
                    pout.flush();
                } else if (command.equals("list")) {
                    String studentName = tokens[1];
                    String books = library.listStudentBooks(studentName);
                    if (!books.equals(""))
                        pout.println(books);
                    else
                        pout.println(1 + "\n" + "No record found for " + studentName);
                    pout.flush();
                } else if (command.equals("inventory")) {
                    String inv = library.getInventory();
                    pout.println(inv);
                    pout.flush();
                } else if (command.equals("exit")) {
                    library.updateInventory();
                }

            }
        } catch(IOException e) {
            System.err.println(e);
        } finally {
            scan.close();
        }
    }
}