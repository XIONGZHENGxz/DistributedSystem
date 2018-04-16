import java.util.*;
import java.io.*;

public class Inventory{
    class Transaction{
        int transactionNumber;
        String bookName;
        boolean hasBeenReturned;
        public Transaction(String name, int transactionNumber){
            bookName = name;
            this.transactionNumber = transactionNumber;
            hasBeenReturned = false;
        }
        public int getTransactionNumber(){
            return transactionNumber;
        }
        public String getBookName(){
            return bookName;
        }
        public boolean getReturned(){
            return hasBeenReturned;
        }
        public void setReturned(boolean b){
            hasBeenReturned = b;
        }
    }

    private volatile Map <String, Integer> bookList;//bookName,numOfAvailableBooks
    private volatile Map <String, ArrayList<Transaction>> studentRecord;
    private volatile Object numTransactionsLock;
    private volatile int numberOfTransactions;

    public Inventory(String fileName){
        bookList = Collections.synchronizedMap(new HashMap <String, Integer>());
        studentRecord = Collections.synchronizedMap(new HashMap <String, ArrayList <Transaction>>());
        parseInput(fileName);
        numberOfTransactions = 0;
        numTransactionsLock = new Object();
    }

    public void parseInput(String fileName){
        try (BufferedReader in = new BufferedReader(new FileReader(fileName))){
            for(String line = in.readLine(); line != null; line = in.readLine()){
                int i = line.lastIndexOf("\"");
                String name = line.substring(0, i + 1).trim();
                int numOfBooks =Integer.parseInt(line.substring(i + 1).trim());
                bookList.put(name, numOfBooks);
            }
        } catch (IOException e) {
            System.err.println("Input file not found");
            System.exit(1);
        }
    }

    public synchronized int getNumberOfCopies(String bookName){
        if(bookList.containsKey(bookName)){
            return bookList.get(bookName);
        }else{
            return -1;
        }
    }

    public synchronized boolean getCopyAndDecrement(String bookName){
        if(!bookList.containsKey(bookName) || (bookList.get(bookName) <= 0)){
            return false;
        }

        int numCopies = bookList.get(bookName);
        bookList.put(bookName, numCopies - 1);
        return true;
    }

    public synchronized boolean returnCopy(String bookName){
        if(!bookList.containsKey(bookName)) {
            return false;
        }

        int numCopies = bookList.get(bookName);
        bookList.put(bookName, numCopies + 1);
        return true;
    }


    public String toString(){
        StringBuilder s = new StringBuilder();
        Map<String, Integer> copy = new HashMap<>(bookList);
        for(Map.Entry<String, Integer> e : copy.entrySet()){
            s.append(e.getKey() + " " + e.getValue());
            s.append('\n');
        }
        return s.toString().trim();
    }

    public int getNewTransactionNumber(){
        synchronized(numTransactionsLock) {
            numberOfTransactions++;
            return numberOfTransactions;
        }
    }

    public synchronized int addTransaction(String studentName, String bookName){
        if(bookList.containsKey(bookName) && (bookList.get(bookName) > 0)){
            int newNumber = getNewTransactionNumber();
            if(studentRecord.containsKey(studentName)){
                studentRecord.get(studentName).add(new Transaction(bookName, newNumber));
            }
            else{
                studentRecord.put(studentName, new ArrayList<Transaction>());
                studentRecord.get(studentName).add(new Transaction(bookName, newNumber));
            }
            getCopyAndDecrement(bookName);
            return newNumber;
        }
        else if(bookList.containsKey(bookName)){
            return -1;
        }
        else{
            return -2;
        }

    }

    public synchronized boolean returnBook(int toReturn){
        String currentOwner = null;
        for(Map.Entry<String,ArrayList<Transaction>> e: studentRecord.entrySet()){
            for(Transaction t : e.getValue()){
                if(toReturn == t.getTransactionNumber())
                    currentOwner = e.getKey();
            }
        }
        if(currentOwner!=null){
            String book = null;
            Transaction currentTransaction = null;
            for(Transaction t : studentRecord.get(currentOwner)){
                if(t.getTransactionNumber() == toReturn){
                    book = t.getBookName();
                    currentTransaction = t;
                }
            }
            if(book != null && currentTransaction != null && !currentTransaction.getReturned()){
                returnCopy(book);
                currentTransaction.setReturned(true);
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }

    public String studentList(String studentName){
        if(studentRecord.containsKey(studentName)){
            StringBuilder s = new StringBuilder();
            List<Transaction> copy = new ArrayList<>(studentRecord.get(studentName));
            for(Transaction t : copy){
                s.append(t.getTransactionNumber()+ " " + t.getBookName());
                s.append("\n");
            }
            return s.toString().trim();
        }
        else{
            return "No record found for " + studentName;
        }
    }


}
