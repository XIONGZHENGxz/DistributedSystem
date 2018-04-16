import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Library {
	private ArrayList<String> bookOrder = new ArrayList<String>();
	private Map<String, Integer> inventory = new HashMap<String, Integer>(); // book name : # copies available
	private Map<Integer, Record> recordLog = new HashMap<Integer, Record>(); // record-id : book name
	private int recordIdCounter = 1;

	public synchronized void insert(String bookName, int quantity) {
		if (inventory.containsKey(bookName)) {
			int curr_quantity = inventory.get(bookName);
			inventory.put(bookName, curr_quantity + quantity);
		} else {
			inventory.put(bookName, quantity);
			bookOrder.add(bookName);
		}
	}

	/**
	 * borrow <student-name> <book-name> – inputs the name of a student, the name of the book.
		The client sends this command to the server using the current mode of the appropriate protocol.
		If the library has all the copies of this book lent out, the server responds with the message:
		‘Request Failed - Book not available’. If the library does not have the book, the server responds
		with message: ‘Request Failed - We do not have this book’. Otherwise, the borrow request
		succeeds and the server replies with a message: ‘You request has been approved, <record-id>
		<student-name> <book-name>’. Note that, the record-id is unique and automatically generated
		by the server. You can assume that the request-id starts with 1. The server should also update
		the library inventory.
	 * @param studentName
	 * @return
	 */
	public synchronized String borrow(String studentName, String bookName) {
		// Check if library has the book
		if (!inventory.containsKey(bookName))
			return "Request Failed - We do not have this book";
		// Check if copies are available
		int numCopiesAvailable = inventory.get(bookName);
		if (numCopiesAvailable == 0)
			return "Request Failed - Book not available";
		// Generate request-id
		int recordId = recordIdCounter;
		recordIdCounter++;
		// Add request to request log
		recordLog.put(recordId, new Record(recordId, bookName, studentName));
		// Update inventory
		inventory.put(bookName, numCopiesAvailable - 1);
		// Return approval message
		return "Your request has been approved, " + recordId + " " + studentName + " " + bookName; // TODO this is different than the above message, I assume it's a typo...
	}
	
	/**
	 * return <record-id> – return the book associated with the <record-id>. If there is no
		existing borrow record with the id, the response is: ‘<record-id> not found, no such borrow
		record’. Otherwise, the server replies: ‘<record-id> is returned’ and updates the inventory.
	 * @return
	 */
	public synchronized String returnBook(int recordId) {
		// Check if record-id exists
		if (!recordLog.containsKey(recordId))
			return recordId + " not found, no such borrow record";
		// Remove record and get book
		Record record = recordLog.remove(recordId);
		// Update inventory
		if (inventory.containsKey(record.book))
			inventory.put(record.book, inventory.get(record.book) + 1);
		// Return message
		return recordId + " is returned";
	}
	
	/**
	 * list <student-name> – list all books borrowed by the student. If no borrow record is found
		for the student, the system responds with a message: ‘No record found for <student-name>’.
		Otherwise, list all records of the student as <record-id> <book-name>. Note that, you should
		print one line per borrow record.
	 * @return
	 */
	public synchronized String list(String studentName) {
		// Search records for matching student's name
		ArrayList<Record> studentsRecords = new ArrayList<Record>();
		for (Record r : recordLog.values())
			if (r.student.equals(studentName))
				studentsRecords.add(r);
		// Check if no records existed
		if (studentsRecords.isEmpty())
			return "No record found for " + studentName;
		// Build message
		String toReturn = "";
		for (Record r : studentsRecords)
			toReturn += r.id + " " + r.book + "\n";
		toReturn = toReturn.trim(); // remove last newline
		return toReturn;
	}
	
	/**
	 * inventory – lists all available books in the library. For each book, you should show ‘<book-name>
		<quantity>’. Note that, even if there is no copies left, you should print the book with quantity
		0. In addition, you should print one line per book.
	 * @return
	 */
	public synchronized String inventory() {
		String toReturn = "";
		for (String entry : bookOrder)
			toReturn += entry + " " + inventory.get(entry) + "\n";
		toReturn = toReturn.trim(); // remove last newline
		return toReturn;
	}
}



class Record {
	int id;
	String book, student;
	public Record(int id, String book, String student) {
		this.id = id;
		this.book = book;
		this.student = student;
	}
}
