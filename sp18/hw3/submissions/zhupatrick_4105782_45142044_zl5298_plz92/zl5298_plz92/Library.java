import java.util.*;

public class Library {
	
	// Mark: - Properties
	
	private ArrayList<String> bookList = new ArrayList<>();
	// book name -> book count
	private HashMap<String, Integer> library = new HashMap<String, Integer>();
	// requestID -> bookName
	private HashMap<Integer, String> requestRecord = new HashMap<Integer, String>(); 
	// student name -> all his borrowing records
	private HashMap<String, ArrayList<Integer>> studentRecord = new HashMap<String, ArrayList<Integer>>();
	// request ID for next borrowing
	private Integer requestID = 1;
	
	
	// Mark: - Public APIs
	
	public void putBook (String bookName, int bookCount) {
		// if inventory contains such book, add more to it
		if (library.containsKey(bookName)) library.put(bookName, library.get(bookName) + bookCount);
		// if not, create an entry for this new book and update the count
		else {
			bookList.add(bookName);
			library.put(bookName, bookCount);
		}
	}
	
	/**
	 * 
	 * @param student
	 * @param book
	 * @return
	 */
	public synchronized String borrowBook (String studentName, String bookName) {

		// check if inventory contains this book
		if(library.containsKey(bookName)) {
			// check if the inventory has any copy left
			if(library.get(bookName) == 0) return "Request Failed - Book not available";
			else {
				// update request record
				requestRecord.put(requestID, bookName);
				// if student record exist, just add new record
//				if(studentRecord.containsKey(studentName)) studentRecord.get(studentName).add(requestID);
//				// if not create new record
//				else {
//					ArrayList<Integer> recordList = new ArrayList<Integer>();
//					recordList.add(requestID);
//					studentRecord.put(studentName, recordList);
//				}
				
				ArrayList<Integer> list = studentRecord.get(studentName);
    			if(list == null) list = new ArrayList<Integer>();
    			list.add(requestID);
    			studentRecord.put(studentName, list);
				
				// update inventory
				library.put(bookName, library.get(bookName) - 1);
				
				// increment requestID
				int temp = requestID;
				requestID++;
				return "Your request has been approved, " + 
					   temp + " " + studentName + " " + "\"" + bookName + "\"";
			}
		} 
		// cannot find the book
		else return "Request Failed - We do not have this book";
		
	}
	
	/**
	 * 
	 * @param recordID
	 * @return
	 */
	public synchronized String returnBook (int recordID) {
		// check if has such request record
		if(requestRecord.containsKey(recordID)) {
			// get corresponding recorded book
			String bookName = requestRecord.get(recordID);
			// update inventory
			library.put(bookName, library.get(bookName) + 1);
			// remove returned book from the student's borrowed record
			for(String stoodent : studentRecord.keySet()) {
				ArrayList<Integer> update = studentRecord.get(stoodent);
				if(update.contains((Integer)recordID)) {
					update.remove((Integer)recordID);
					if(update.size() == 0) {
						studentRecord.remove(stoodent);
					}
					else {
						studentRecord.put(stoodent, update);
					}
					break;
				}
			}
			// remove returned book from the request record
			requestRecord.remove(recordID);
			return recordID + " is returned";
		}
		else return recordID + " not found, no such borrow record";
	}
	
	/**
	 * 
	 * @param studentName
	 * @return
	 */
	public synchronized String getStudentRecord (String studentName) {
		// check if student record contains target student
		if(studentRecord.containsKey(studentName)) {
			String result = "";
			// get student record
			ArrayList<Integer> studentRecordList = studentRecord.get(studentName);
			
			Integer recordID;
			// get all records
			for (int i = 0; i < studentRecordList.size(); i++) {
				recordID = studentRecordList.get(i);
				result = result + recordID + " " + "\"" + requestRecord.get(recordID) + "\"" + "^";
			}
			return result;
		}
		// student record does not contain target student
		else return "No record found for " + studentName + "\n";
		
	}
	
	/**
	 * 
	 * @return
	 */
	public synchronized String getInventory() {
		String result = "";
		// go through entire inventory
		for(String bookName: bookList) 
			result = result + "\"" + bookName + "\" " + library.get(bookName).toString() + "^";
		
		return result;
	}


}
