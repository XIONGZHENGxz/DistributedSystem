/*
 * rsr873
 * vks377
 */

import java.io.*;
import java.util.*;
import java.net.*;

public class TCPThread extends Thread {
    Socket theClient;
    Library library;

    public TCPThread(Socket s, Library l) {
        theClient = s;
        library = l;
    }

    public void run() {
        Scanner sc = null;
        try {
            sc = new Scanner(theClient.getInputStream());
            PrintWriter pout = new PrintWriter(theClient.getOutputStream());
            //String command = sc.nextLine();
            while(sc.hasNextLine()) {
                String command = sc.nextLine();
                System.out.println("received: " + command);
                String[] cmdArray = command.split(" ");
                String tag = cmdArray[0];
                if (tag.equals("borrow")) {
                    String studentName = cmdArray[1];
                    String bookName = "";
                    for(int i = 2; i < cmdArray.length - 1; i++)
                        bookName += cmdArray[i] + " ";
                    bookName += cmdArray[cmdArray.length - 1];
                    int recordID = library.borrow(studentName, bookName);
                    if (recordID > 0)
                        pout.println("Your request has been approved, " + recordID + " " + studentName + " " + bookName);
                    else if (recordID == 0)
                        pout.println("Request Failed - Book not available");
                    else
                        pout.println("Request Failed - We do not have this book");
                    pout.flush();
                } else if (tag.equals("return")) {
                    int recordID = Integer.parseInt(cmdArray[1]);
                    String bookName = library.returnBook(recordID);
                    if (!bookName.equals(""))
                        pout.println(recordID + " is returned");
                    else
                        pout.println(recordID + " not found, no such borrow record");
                    pout.flush();
                } else if (tag.equals("list")) {
                    String studentName = cmdArray[1];
                    String books = library.booksForStudent(studentName);
                    if (!books.equals(""))
                        pout.println(books);
                    else
                        pout.println(1 + "\n" + "No record found for " + studentName);
                    pout.flush();
                } else if (tag.equals("inventory")) {
                    String inv = library.getInventory();
                    pout.println(inv);
                    pout.flush();
                } else if (tag.equals("exit")) {
                    library.updateInventory();
                }
                //command = sc.nextLine();
            }
        } catch(IOException e) {
            System.err.println(e);
        } finally {
            sc.close();
        }
    }
}
