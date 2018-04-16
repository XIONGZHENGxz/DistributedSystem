import java.util.Map.Entry;
import java.util.concurrent.*;

public class Book {
	String title;
	int totalCopies;
	ConcurrentHashMap<Integer, String> records;  
	public Book(String title, int totalCopies){
		this.title = title;
		this.totalCopies = totalCopies;
		records = new ConcurrentHashMap<Integer, String>(); 
	}
	public synchronized int checkoutBook(String name, int transaction){
		int transactionId = -1;
		if(booksAvailable()>0){
			records.put(transaction, name);
			transactionId = transaction;
			System.out.println("AFTER BORROW COPIES #: " + totalCopies);
		}
		/*
		 * if a book is available, 
		 * get new recordId and assign to records the recordId and student name.
		 * set transactionId to that new record id 
		 */
		return transactionId;
	}
	
	public synchronized String getKeyFromValue(ConcurrentHashMap<Integer, String> hm, String name) {
    	Integer txID= null; 
        for(Entry<Integer, String> entry: hm.entrySet()){
            if(name.equals(entry.getValue())){
                txID = entry.getKey();
                break;
            }
        }
        String recordId = txID.toString();
        System.out.println("RETURNED: "+ recordId);
        return recordId;
    }
	public synchronized boolean returnBook(String name){
		if(records.containsValue(name)){
			String recordId = getKeyFromValue(records, name);
			records.remove(Integer.valueOf(recordId));
			System.out.println("RECORDS SIZE: " + records.size());
			System.out.println("AFTER RETURN COPIES #: " + totalCopies);
			return true;
		}		
		/*
		 * search records map for the student and remove the book
		 * if successful return true
		 */
		return false;
	}
	public int booksAvailable(){
		System.out.println("NUMBER OF BOOKS AVAILABLE: " +  (totalCopies-records.size()));
		return totalCopies-records.size();
	}
	public int booksCheckedout(){
		return records.size();
	}
}
