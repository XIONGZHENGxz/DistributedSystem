/*
 * rsr873
 * vks377
 */

import java.util.*;
import java.io.*;

public class Library {
    private Map<Integer, String> recordToBook;
    private Map<String, Integer> inventory;
    private Map<String, List<Integer>> studentLists;

    private int recordID;

    public Library(String fileName) throws IOException {
        recordToBook = new HashMap<Integer, String>();
        inventory = new LinkedHashMap<String, Integer>();

        Scanner in = new Scanner(new File(fileName));
        while(in.hasNextLine()) {
            String[] line = in.nextLine().split(" ");
            String bookName = "";
            for(int i = 0; i < line.length - 2; i++)
                bookName += line[i] + " ";
            bookName += line[line.length - 2];
            int quantity = Integer.parseInt(line[line.length - 1]);
            inventory.put(bookName, quantity);
        }

        studentLists = new HashMap<String, List<Integer>>();
        recordID = 0;
    }

    public synchronized int borrow(String studentName, String bookName) {
        if(!inventory.containsKey(bookName))
            return -1;
        if(inventory.get(bookName) == 0)
            return 0;
        else {
            inventory.put(bookName, inventory.get(bookName) - 1);
            recordID++;
            recordToBook.put(recordID, bookName);
            if(studentLists.containsKey(studentName)) {
                List<Integer> list = studentLists.get(studentName);
                list.add(recordID);
                studentLists.put(studentName, list);
            }
            else {
                List<Integer> list = new ArrayList<Integer>();
                list.add(recordID);
                studentLists.put(studentName, list);
            }
        }
        return recordID;
    }

    public synchronized String returnBook(int recID) {
        if(recordToBook.containsKey(recID)) {
            String bookName = recordToBook.get(recID);
            inventory.put(bookName, inventory.get(bookName) + 1);
            recordToBook.remove(recID);
            for(String studentName: studentLists.keySet()) {
                if(studentLists.get(studentName).contains(recID))
                    studentLists.get(studentName).remove((Integer)recID);
            }
            return bookName;
        }
        return "";
    }

    public synchronized String booksForStudent(String studentName) {
        String books = "";
        int lines = 0;
        if(studentLists.containsKey(studentName)) {
            List<Integer> recIDs = studentLists.get(studentName);
            for(int recID: recIDs) {
                books += recID + " " + recordToBook.get(recID) + "\n";
                lines++;
            }
            books = String.valueOf(lines) + "\n" + books.trim();
        }
        return books;
    }

    public synchronized String getInventory() {
        String inv = "";
        int lines = 0;
        for(String bookName: inventory.keySet()) {
            inv += bookName + " " + inventory.get(bookName) + "\n";
            lines++;
        }
        inv = String.valueOf(lines) + "\n" + inv.trim();
        return inv;
    }

    public synchronized void updateInventory() {
        try {
            PrintWriter writer = new PrintWriter("inventory.txt");
            for (String bookName : inventory.keySet())
                writer.println(bookName + " " + inventory.get(bookName));
            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
    }
}
