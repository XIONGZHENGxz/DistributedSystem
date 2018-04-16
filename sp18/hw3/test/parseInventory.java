/*
Mohamed Nasreldin man2766
Hamza Ghani hhg263
*/

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class parseInventory {


    private LinkedHashMap<String, Integer> library = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashMap<Integer, String>> students = new LinkedHashMap<>();
    AtomicInteger recordID = new AtomicInteger(1);
    public parseInventory(String fileName){
        FileReader input = null;
        try {
            input = new FileReader(fileName);
        } catch (FileNotFoundException e) {}
        BufferedReader bufRead = new BufferedReader(input);
        String myLine;

        // Initialize library!
        try {
            while ( (myLine = bufRead.readLine()) != null) {
                library.put("\"" + myLine.split("\"")[1] + "\"", Integer.parseInt(myLine.split("\" ")[1]));
            }
        } catch (IOException e) {}
    }


    public synchronized String borrow(String[] input){

        String student = input[0];
        String book = input[1];

        // This means library never had the book
        if (!library.containsKey(book)) {
            return "Request Failed - We do not have this book";
        }

        // Library is out of that book-- count is 0
        if (library.get(book) == 0) {
            return "Request Failed - Book not available";

        }

        // Library has the book to checkout!
        else {

            // Update book count after checking out
            Integer newCount = library.get(book) - 1;
            library.put(book, newCount);

            LinkedHashMap<Integer, String> temp = new LinkedHashMap<>();
            // Update student records and book records
            int IDNum = this.recordID.getAndIncrement();
            if(students.get(student) != null)
                temp = students.get(student);
            temp.put(IDNum, book);
            students.put(student, temp);

            return "Your request has been approved, " + IDNum + " " + student + " " +  book;
        }


    }

    public synchronized String returnBook(int id){

        // Get book/ check if recordID exists
        //if (!records.containsKey(id)) {
        int counter = students.keySet().size();
        String owner = new String();
        for(String s: students.keySet()){
            HashMap tempMap = students.get(s);
            if(tempMap.containsKey(id)){
                owner = s;
                break;
            }
            counter--;
            if(counter == 0)
                return id + " not found, no such borrow record";
        }

        LinkedHashMap<Integer, String> tempMap = students.get(owner);
        String book = tempMap.get(id);

        // Update library
        int newCount = library.get(book) + 1;
        library.put(book, newCount);

        // Update student records
        tempMap = students.get(owner);
        tempMap.remove(id);
        students.put(owner, tempMap);

        return id + " is returned";

    }
    public synchronized String list(String name){

        if (!students.containsKey(name)) {
            return "No record found for " + name;
        }

        HashMap<Integer, String> tempMap = students.get(name);
        String s = new String();
        for (Integer l: tempMap.keySet()) {
            s += l.toString() + " " +tempMap.get(l) + "&";
        }
        s = s.substring(0, s.length()-1);

        return s;

    }

    public synchronized String inventory(){
        String result = new String();
        for (String s: library.keySet()) {
            result += s.toString() + " " +library.get(s) + "&";
        }
        result = result.substring(0, result.length()-1);
        return result;

    }

    public String Command(String command) {
        // calls appropriate command method
        String[] tokens = command.split(" ");
        String order = command.split(" ")[0];
        if(order.equals("borrow")) {
            String studentName = command.split(" ")[1];
            String bookName = "\"" + command.split("\"")[1] + "\"";
            return borrow(new String[]{studentName, bookName});
        }
        else if (order.equals("return"))
            return returnBook(Integer.parseInt(tokens[1]));
        else if (order.equals("list"))
            return list(tokens[1]);
        else if (order.equals("inventory"))
            return inventory();
        return "";
    }
}
