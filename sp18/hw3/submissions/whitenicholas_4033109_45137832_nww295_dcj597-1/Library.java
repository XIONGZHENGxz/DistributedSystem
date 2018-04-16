import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Library {
    public static LinkedHashMap<String, Integer> list = BookServer.bookItems;
    private static Hashtable<Integer, String> borrowedBooked = new Hashtable<>();
    private static Hashtable<String, ArrayList<Integer>> studentList = new Hashtable<>();
    private static Integer recordId = 0;

    public static synchronized String borrow(String student, String book) {
        String returnString;
        if(list.get(book) > 0) {
            returnString = "Your request has been approved,";
            Integer numBooks = list.get(book);
            list.replace(book, (numBooks-1));
            recordId++;
            borrowedBooked.put(recordId, book);
            ArrayList<Integer> getArray;
            if(studentList.get(student) == null) {
                getArray = new ArrayList<>();
            } else {
                getArray = studentList.get(student);
            }
            getArray.add(recordId);
            studentList.put(student, getArray);
            returnString = returnString+" "+recordId+" "+student+" "+"\""+book+"\"";
        } else if(list.get(book) == 0){
            returnString = "Request Failed - Book not available";
        } else {
            returnString = "Request Failed - We do not have this book";
        }

        return returnString;
    }

    public static synchronized String returnBook(String record) {
        Integer recordId = Integer.parseInt(record);
        String returnString = null;

        if(borrowedBooked.get(recordId) != null) {
            String returnedBook = borrowedBooked.remove(recordId);
            Integer numBooks = list.get(returnedBook);
            list.replace(returnedBook, (numBooks+1));
            returnString = record+" is returned";
            Set<String> keys = studentList.keySet();
            ArrayList<String> removeKeys = new ArrayList<>();
            for(String key: keys) {
                ArrayList<Integer> returnList = studentList.get(key);
                for(int i = 0; i < returnList.size(); i++) {
                    if(returnList.get(i).equals(recordId)) {
                        removeKeys.add(key);
                    }
                }
            }
            for(int i = 0; i < removeKeys.size(); i++) {
                ArrayList<Integer> removeList = studentList.get(removeKeys.get(i));
                for(int j=0; j < removeList.size(); j++) {
                    if (removeList.get(j) == recordId) {
                        removeList.remove(j);
                        if(removeList.isEmpty()) {
                            studentList.remove(removeKeys.get(i));
                        }
                    }
                }
            }

        } else {
            returnString = record+" not found, no such borrow record";
        }

        return returnString;
    }

    public static synchronized String list(String student) {
        Set<String> stringSet = studentList.keySet();
        String returnString = "";

        if(studentList.get(student).equals(null)) {
            returnString = "No record found for "+student;
        } else {
            for (String key : stringSet) {
                if(key.equals(student)) {
                    ArrayList<Integer> studentBooks = studentList.get(student);
                    for(int i = 0; i < studentBooks.size(); i++) {
                        returnString = returnString+studentBooks.get(i).toString()+" \""+borrowedBooked.get(studentBooks.get(i))+"\"\n";
                    }
                }
            }
        }

        return returnString;

    }

    public static synchronized String inventory() {
        String returnString = "";
        Set<String> keys = list.keySet();
        for(String key: keys) {
            returnString = returnString+"\""+key+"\""+" "+list.get(key)+"\n";
        }

        return returnString;
    }

    public static synchronized void printInventory() {
        String write = inventory();
        try {
            FileWriter writer = new FileWriter("inventory.txt");
            PrintWriter printWrite = new PrintWriter(writer);
            printWrite.print(write);
            printWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
