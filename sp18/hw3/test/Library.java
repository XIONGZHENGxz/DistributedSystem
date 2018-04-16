import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Library {
	private BookRecord[] inventory;
	private HashMap<Integer, BorrowRecord> borrows;
	private int numberOfBorrows;

	private Library() {
		borrows = new HashMap<>();
		numberOfBorrows = 0;
	}
	
	private BookRecord get(String book) {
		for(BookRecord br : inventory) {
			if(br.getBook().equals(book)) {
				return br;
			}
		}
		return null;
	}
	
	private boolean containsKey(String book) {
		return get(book) != null;
	}

	public boolean hasBook(String book) {
		return containsKey(book);
	}

	public synchronized boolean isBookAvailable(String book) {
		if (hasBook(book)) {
			return get(book).getAvailable() > 0;
		}
		return false;
	}

	public synchronized int borrowBook(String student, String book) {
		if (!hasBook(book)) {
			throw new IllegalArgumentException(book + " not found");
		}
		
		if(get(book).borrowBook()) {
			borrows.put(++numberOfBorrows, new BorrowRecord(book, student));
			return numberOfBorrows;
		}
		else {
			return -1;
		}
	}
	
	public synchronized boolean returnBook(int recordID) {
		if(borrows.containsKey(recordID)) {
			BorrowRecord br = borrows.remove(recordID);
			return get(br.getBook()).returnBook();
		}
		return false;
	}
	
	public synchronized List<BookRecord> getInventory() {
		ArrayList<BookRecord> records = new ArrayList<>();
		for(BookRecord record : inventory) {
			records.add(record.clone());
		}
		return records;
	}
	
	public synchronized Map<Integer, BorrowRecord> getBorrowedBooks(String student) {
		HashMap<Integer, BorrowRecord> records = new HashMap<>();
		for(Entry<Integer, BorrowRecord> record : borrows.entrySet()) {
			if(record.getValue().getStudent().equals(student)) {
				records.put(record.getKey(), record.getValue());
			}
		}
		return records;
	}
	
	public static Library fromFile(String path) throws IOException {
		Library library = new Library();
		List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(path));
		library.inventory = new BookRecord[lines.size()];
		for(int x = 0; x < lines.size(); x++) {
			String[] parts = lines.get(x).split("\" ");
			String book = parts[0].substring(1);
			library.inventory[x] = new BookRecord(book, Integer.parseInt(parts[1]));
		}
		return library;
	}
}
