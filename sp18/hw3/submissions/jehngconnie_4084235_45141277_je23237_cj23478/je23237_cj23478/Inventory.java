import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Inventory {
	AtomicInteger recordCounter = new AtomicInteger(1);
 	ConcurrentHashMap<String, Book> inventory = new ConcurrentHashMap<String, Book>();
 	ArrayList<String> orderedList = new ArrayList<String>();
    public Inventory(String file){
    	Parser p = new Parser(file);
    	p.parse(inventory, orderedList);
    }
    
    /* inputs: student name, book title
     * all copies lent out - respond: Request Failed - Book not available
     * library does not have book - respond: Request Failed - We do not have this book
     * ow - respond: You request has been approved, <record-id> <student-name> <book-name>
     * auto generate request-id
     * update library inventory
     */
    public String borrow(String name, String title){
    	String reply = "";
	    if(!inventory.containsKey(title)){
	    	reply = "Request Failed - We do not have this book";
	    }
	    else{
	    	Book b = inventory.get(title);
	    	if(b.booksAvailable()==0)
	    		reply = "Request Failed - Book not available";
	    	else{
	    		int recordId = recordCounter.getAndIncrement();
	    		if(b.checkoutBook(name, recordId)<0)
	    			reply = "Request Failed - Book not available";
	    		else
	    			reply = "Your request has been approved, " + recordId + " " + name + " " + title;
	    	}
	    }
	    return reply;
    }
    /* return book of record id
     * if no book - reply: <record-id> not found, no such borrow record
     * ow - reply: <record-id> is returned
     * update inventory
     */
    public String returnBook(String recordId){
    	String reply = "";
    	String title = "";
    	int idNum = Integer.valueOf(recordId.trim());
    	System.out.println("Trying to RETURN: " + idNum);
    	Book b = findCheckoutRecord(idNum); 
    	if(b != null && b.title != null)
    		title = b.title;
    	if(b == null || !inventory.containsKey(title))
    		reply = recordId + " not found, no such borrow record";
    	else{
    		String name = b.records.get(Integer.valueOf(recordId));
    		System.out.println("HELLO"+name);
    		if(name != null){
    			b.returnBook(name);
    			reply = recordId + " is returned";
    		}
    	}
    	return reply;
    }
	/*
	 * get book object and iterate the records list 
	 * if records list contains record id then return book
	 * ow no record found
	 */
    protected Book findCheckoutRecord(int recordId){
    	Iterator<String> it = inventory.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next().toString();
    		Book b = inventory.get(key);
    		if(b.records.containsKey(recordId)){
    			return b;
    		}
    	}
		return null;
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
        //System.out.println("FROM RECORDS HASHMAP: "+ recordId);
        return recordId;
     }

    /* list all books borrowed by the student
     * if no record - reply: No record found for <student-name>
     * ow print: <record-id>, <book-name> per line
     */
    public String list(String name){
    	String list = "";
    	Iterator<String> it = inventory.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		System.out.println("KEY: " + key);
    		Book b = inventory.get(key);
    		System.out.println("HERE BOOK: " + b.records.get(1));

    		if(b.records.containsValue(name)){
    			String recordId = getKeyFromValue(b.records, name); 
    			//if(recordId==null) return reply;
    			list+=(recordId + " " + name + "\n");
    		}
    		else{
    			/* no student found */
    			System.out.println("MIKE NOT FOUND");
    		}
    	}
    	if(list.equals("")) list = "No record found for " + name;
    	/* HOW TO MAKE LIST A STRING WITH ONE record per line */
    	/*almost same as findCheckoutRecord, but now if student match is in the record,
    	 * then add the book to the list
    	 */  
    	System.out.println("THE RECORD LIST: " + list);
    	return list;
    }
    /* list all available books in the library.
     * print <book-name>, <quantity> per line
     */
    public synchronized String bookInventory(){
    	String availableList = "";
    	Iterator<String> iterator = orderedList.iterator();
    	while (iterator.hasNext()) {
    	   String key = iterator.next();
    	   Book b = inventory.get(key);
    	   String numCopies = Integer.toString(b.booksAvailable());
    	   availableList+=(key + " " + numCopies + "\n");
    	}
    	/* what about books in original inventory file with ZERO copies left? */
    	/* return list */
    	return availableList;
    }
    
}
