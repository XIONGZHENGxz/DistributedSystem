import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by phucd on 3/2/2018.
 */
public class Library {
	class Book {
		public String name;
		public int quantity;

		public Book(String s, int i) {
			name = s;
			quantity = i;
		}
	}

	class Record {
		public String student;
		public String book;
		public int ID;

		public Record(String st, String b, int i) {
			student = st;
			book = b;
			ID = i;
		}
	}

	public ArrayList<Record> record;
	public LinkedHashSet<Book> inventory;
	public int record_id;

	public Library(String fileName) {
		record = new ArrayList<>();
		inventory = new LinkedHashSet<>();
		record_id = 0;
		String name;
		int quantity;
		try {
			FileReader input = new FileReader(fileName);
			BufferedReader bufRead = new BufferedReader(input);
			String myLine = null;
			while ((myLine = bufRead.readLine()) != null) {
				String[] splited = myLine.split(" ");
				quantity = Integer.parseInt(splited[splited.length - 1]);
				String bookName = splited[0];
				if (splited.length > 2) {
					int i = 1;
					while (i < splited.length - 1) {
						bookName = bookName + " " + splited[i];
						i++;
					}
				}
				Book newBook = new Book(bookName, quantity);
				inventory.add(newBook);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*Return 0 if run out of book -1 if there is no book
	 return id > 0 and add the record of borrow into record list if the borrow is sucessful*/
	public synchronized int borrow(String student, String name) {
		for (Book b : inventory) {
			if (b.name.equals(name)) {
				if (b.quantity == 0) {
					return 0;
				} else {
					b.quantity--;
					record_id++;
					record.add(new Record(student, name, record_id));
					return record_id;
				}
			}
		}
		return -1;
	}

	/*Return 0 if there is no borrow with that id else return id > 0*/
	public synchronized int ret(int id) {
		for (Record r : record) {
			if (r.ID == id) {
				record.remove(r);
				String name = r.book;
				for (Book b : inventory) {
					if (b.name.equals(name)) {
						b.quantity++;
					}
				}
				return id;
			}
		}
		return 0;
	}

	/*List all record associated with the student name*/
	public synchronized String list(String name) {
		String result = "";
		boolean found = false;
		for (Record r : record) {
			if (r.student.equals(name)) {
				found = true;
				result = result + r.ID + " " + r.book + "*";
			}
		}
		if (!found) {
			result = "No record found for " + name;
		}
		return result;
	}

	/*List the inventory*/
	public synchronized String listInventory() {
		String result = "";
		int count = 0;
		for (Book b : inventory) {
			result = result + b.name + " " + b.quantity + "*";
		}
		return result;
	}
}
