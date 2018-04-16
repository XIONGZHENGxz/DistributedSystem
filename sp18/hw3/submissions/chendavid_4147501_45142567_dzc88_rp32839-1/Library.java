import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Library {
	
	HashMap<String, Integer> inventory;
	HashMap<Integer, Record> records;
	HashMap<String, List<Record>> studentList;
	int recordId;
	
	public Library(HashMap<String, Integer> _inventory) {
		records = new HashMap<Integer, Record>();
		studentList = new HashMap<String, List<Record>>();
		inventory = _inventory;
		recordId = 1;
	}
	
	public String borrowBook(String studentName, String bookName) {
		Record newRecord;
		StringBuilder sb = new StringBuilder();
		
		synchronized (inventory) {
			if (!inventory.containsKey(bookName)) {
				sb.append("Request Failed - We do not have this book");
				return sb.toString();
			}
			else if (inventory.get(bookName) < 1) {
				sb.append("Request Failed - Book not available");
				return sb.toString();
			}
			else {
				newRecord = new Record.RecordBuilder().id(recordId++).student(studentName).book(bookName).createRecord();
				int numOfBooks = inventory.get(newRecord.getBook());
				inventory.put(newRecord.getBook(), numOfBooks - 1); // update inventory of book
			}
		}
		
		sb.append(String.format("Your request has been approved, %d %s %s", newRecord.getId(), newRecord.getStudent(), newRecord.getBook()));
		
		synchronized(records) {
			records.put(newRecord.getId(), newRecord); // map record id to record
		}
		
		synchronized(studentList) {
			studentList.putIfAbsent(newRecord.getStudent(), new ArrayList<Record>()); // creates new list if new student
			studentList.get(newRecord.getStudent()).add(newRecord); // adds record to student list
		}
		
		return sb.toString();
	}
	
	public String returnRecordId(int _recordId) {
		Record record;
		StringBuilder sb = new StringBuilder();
		
		synchronized (records) {
			if (!records.containsKey(_recordId)) {
				sb.append(String.format("%d not found, no such record borrow record", _recordId));
				return sb.toString();
			}
			else {
				record = records.get(_recordId);
				sb.append(String.format("%d is returned", _recordId));
				records.remove(_recordId); // remove record from borrow records
			}
		}
		
		synchronized (inventory) {
			int numOfBooks = inventory.get(record.getBook()); 
			inventory.put(record.getBook(), numOfBooks + 1); // update inventory of book
		}
		
		synchronized (studentList) {
			studentList.get(record.getStudent()).remove(record); // remove record from student list
			if (studentList.get(record.getStudent()).size() == 0) { // if student has no records, remove student from list
				studentList.remove(record.getStudent());
			}
		}
		
		return sb.toString();
	}
	
	public String list(String studentName) {
		StringBuilder sb = new StringBuilder();
		synchronized(studentList) {
			if (!studentList.containsKey(studentName)) {
				sb.append(String.format("No record found for %s", studentName));
			}
			else {
				Iterator<Record> iterator = studentList.get(studentName).iterator();
				while (iterator.hasNext()) {
					sb.append(iterator.next().toString()).append('\n');
				}
			}
		}
		return sb.toString();
	}
	
	public String inventory() {
		StringBuilder sb = new StringBuilder();
		synchronized(inventory) {
			if (inventory.isEmpty()) {
				
			}
			else {
				inventory.forEach((k, v) -> sb.append(String.format("%s %d", k, v)).append('\n'));
			}
		}
		return sb.toString();
	}
	
	public String exit() throws IOException {
		StringBuilder sb = new StringBuilder();
		final FileWriter fw = new FileWriter(new File("inventory.txt"));
		synchronized(inventory) {
			inventory.forEach((k, v) -> {
				sb.append(String.format("%s %d", k, v)).append('\n');
			});
		}
		fw.write(sb.toString().trim());
		fw.close();
		return "";
	}
}
