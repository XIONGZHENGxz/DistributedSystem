package hw3;

//import java.util.concurrent.ConcurrentHashMap;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import java.io.FileReader;

public class Library {
    private int record_id = 1;
    PrintWriter file;

    private class Book {
        public String name;
        public int count;

        public Book(String name, int count){
            this.name = name;
            this.count = count;
        }
    }

    private class Record{
        public int id;
        public String book_name;
        public String person;

        Record(int id, String book_name, String person_name){
            this.id = id;
            this.book_name = book_name;
            this.person = person_name;
        }
    }

    //private ConcurrentHashMap<String, Integer> books;
    private ArrayList<Book> books;
    private ArrayList<Record> check_out = new ArrayList<>();


    public Library(String fileName) {
        books = new ArrayList<>();
        String name;
        int count;

        try {
            Scanner sc = new Scanner(new FileReader(fileName)).useDelimiter("\n");

            String s;
            String u[];
            while (sc.hasNext()) {
                s = sc.next();
                u = s.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                name = u[0];
                count = Integer.parseInt(u[1]);

                books.add(new Book(name, count));
            }
        } catch (Exception e){
            System.out.println(e);
        }

    }

    public synchronized void writeFile(){
        StringBuilder s = new StringBuilder();
        for(Book b : books){
            s.append(b.name + " " + b.count + "\n");
        }

        try {
            file = new PrintWriter(new FileWriter("inventory.txt"));
            file.println(s.toString());
        } catch (Exception e){}


    }
    public synchronized int getBookCount(String key) {
        for(Book b : books){
            if(b.name.equals(key)){
                return b.count;
            }
        }
        return 0;
    }

    public synchronized String listInventory(){
        StringBuilder s = new StringBuilder();
        for(Book b : books){
            s.append(b.name + " " + b.count + "\n");
        }
        writeFile();
        return s.toString();
    }

    public synchronized String listByName(String name){
        StringBuilder s = new StringBuilder();
        for(Record record : check_out){
            if(record.person.equals(name)){
                s.append(record.id + " " + record.book_name);
            }
        }
        return s.toString();
    }

    public synchronized String checkoutBook(String person, String key) {

        for(Book b : books){
            if(b.name.equals(key)){
                if(b.count > 0){
                    b.count--;
                    Record record = new Record(record_id++, key, person);
                    check_out.add(record);
                    writeFile();
                    return "You request has been approved, " + record.id + " " + person + " " + key;
                }
                else{
                    writeFile();
                    return "Request Failed - Book not available";
                }
            }
        }
        writeFile();
        return "Request Failed - We do not have this book";
    }


    public synchronized String returnBook(int id){
        Record next;

        ListIterator<Record> iter = check_out.listIterator();
        while (iter.hasNext()) {
            if((next = iter.next()).id == id){
                for(Book b : books){
                    if(b.name.equals(next.book_name)){
                        b.count += 1;
                        break;
                    }
                }
                iter.remove();
                writeFile();
                return id + " is returned";
            }
        }
        writeFile();
        return id + " not found, no such borrow record";
    }


}
