package hw3;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Library {
    LinkedHashMap<String, Integer> inventory;
    HashMap<String, ArrayList<Book>> studentToBooks;
    HashMap<Integer, String> recordIDToStudent;

    public Library() {
        inventory = new LinkedHashMap<>();
        studentToBooks = new HashMap<>();
        recordIDToStudent = new HashMap<>();
    }

    public void add(String book, int num) {
        inventory.put(book, num);
    }

    public synchronized String getInventory() {
        String inventory_str = "";
        if (inventory.size() > 0) {
            for (Object o : inventory.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                inventory_str += (pair.getKey() + " " + pair.getValue() + "_%_");
            }
            return inventory_str;
        }
        return null;
    }

    public synchronized String borrow(String studentName, String bookName) {
        if (inventory.containsKey(bookName) && inventory.get(bookName) != 0) {
            Book b = new Book(bookName);
            studentToBooks.putIfAbsent(studentName, new ArrayList<>());
            studentToBooks.get(studentName).add(b);
            recordIDToStudent.put(b.recordID, studentName);
            inventory.put(bookName, inventory.get(bookName) - 1);
            return "Your request has been approved, " + b.recordID + " " + studentName + " " + b.name;
        } else if (inventory.containsKey(bookName)){
            return "Request Failed - Book not available";
        } else {
            return "Request Failed - We do not have this book";
        }
    }

    public synchronized boolean returnBook(int recordID) {
        if (recordIDToStudent.containsKey(recordID)) {
            String student = recordIDToStudent.get(recordID);

            Book b = null;
            for (int i = 0; i < studentToBooks.get(student).size(); i++) {
                if (studentToBooks.get(student).get(i).recordID == recordID) {
                    b = studentToBooks.get(student).get(i);
                    break;
                }
            }
            if (b != null) {
                studentToBooks.remove(b);
                inventory.put(b.name, inventory.get(b.name) + 1);
                recordIDToStudent.remove(recordID);
            }
            return true;
        }
        return false;
    }

    public synchronized String list(String studentName) {
        ArrayList<Book> books = studentToBooks.get(studentName);
        String list = "";
        if (books != null)  {
            for (Book b : books) {
                list += (b.recordID + " " + b.name + "_%_");
            }
            return list;
        } else {
            return "No record found for " + studentName;
        }
    }

    public synchronized void updateInventory() {
        try {
            PrintWriter out = new PrintWriter("inventory.txt");
            for (String book : inventory.keySet()) {
                out.println(book + " " + inventory.get(book));
                out.flush();
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
    }
}
