import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LibraryClient {

	private final static Logger LOGGER = Logger.getLogger(BookClient.class.getName());

	private Library library;

	public LibraryClient(Library library) {
		this.library = library;
	}

	public String execute(String command) {		
		String[] tokens = command.split(" ");
		
		switch (tokens[0]) {
		case "borrow":
			String name = tokens[2].replaceAll("\"", "");
			for(int x = 3; x < tokens.length-1; x++) {
				name += " " + x;
			}
			if(tokens.length > 3)
				name += " " + tokens[tokens.length-1].replaceAll("\"", "");
			return borrow(tokens[1], name);
		case "return":
			return returnBook(tokens[1]);
		case "inventory":
			return inventory();
		case "list":
			return list(tokens[1]);
		case "exit":
			return exit();
		default:
			return "0\n";
		}
	}

	public String borrow(String student, String book) {
		if(!library.hasBook(book)) {
			return "1\nRequest Failed - We do not have this book";
		}
		int id = library.borrowBook(student, book);
		if(id > -1) {
			return String.format("1%nYour request has been approved, %d %s \"%s\"", id, student, book);
		}
		else {
			return "1\nRequest Failed - Book not available";
		}
	}

	public String returnBook(String recordId) {
		if(library.returnBook(Integer.parseInt(recordId))) {
			return "1\n" + recordId + " is returned";
		}
		else {
			return "1\n" + recordId + " not found, no such borrow record";
		}
	}

	public String list(String student) {
		Map<Integer, BorrowRecord> brs = library.getBorrowedBooks(student);
		String ret = brs.size() + "\n";
		for (Entry<Integer, BorrowRecord> br : brs.entrySet()) {
			ret += String.format("%d \"%s\"%n", br.getKey(), br.getValue().getBook());
		}
		return ret;
	}

	public String inventory() {
		List<BookRecord> brs = library.getInventory();
		String ret = brs.size() + "\n";
		for (BookRecord br : brs) {
			ret += String.format("\"%s\" %d%n", br.getBook(), br.getAvailable());
		}
		return ret;
	}
	
	public String exit() {
		try {
			PrintWriter out = new PrintWriter(new File("inventory.txt"));
			for (BookRecord br : library.getInventory()) {
				out.printf("\"%s\" %d%n", br.getBook(), br.getAvailable());
			}
			out.close();
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, "Cannot write inventory to disk");
		}
		return "0\n";
	}
}
