import java.util.ArrayList;

public class RecordList {
	
	class Record {
			private String student, book;
			private int recordId;

			public Record(String student, String book, int recordId) {
				this.student = student;
				this.book = book;	
				this.recordId = recordId;
			}

			public String toString() {
				return (student + " " + book);
			}
	}

	private ArrayList<Record> checkouts;
	private int recordCount;

	public RecordList() {
		checkouts = new ArrayList<Record>();
		recordCount = 1;
	}

	public synchronized int borrow(String student, String book) {
		int recordId = recordCount;
		checkouts.add(new Record(student, book, recordId));
		recordCount++;
		
		return recordId;
	}
	
	public synchronized String ret(int recordId) {
		for (int i = 0; i < checkouts.size(); i++) {
			if (checkouts.get(i).recordId == recordId) {
				String book = checkouts.get(i).book;
				checkouts.remove(i);
				return book;
			}
		}

		return "DNE";
	}

	public synchronized boolean canReturn(int recordId) {
		if (recordId <= checkouts.size()) {
			return false;
		}

		return true;
	}

	public synchronized String list(String student) {
		String list = "";

		for (int i = 0; i < checkouts.size(); i++) {
			if (checkouts.get(i).student.equals(student)) {
				list += checkouts.get(i).recordId + " " + checkouts.get(i).book + "\n";
			}
		}

		if (list.length() > 0) {
			return list;
		}

		return ("No record found for " + student);
	}
}
