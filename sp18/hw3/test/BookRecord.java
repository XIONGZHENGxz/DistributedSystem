
public class BookRecord {
	private String book;
	private int count;
	private int borrowed;
	
	public BookRecord(String book, int count) {
		this.book = book;
		this.count = count;
	}
	
	public String getBook() {
		return book;
	}
	
	public int getAvailable() {
		return count - borrowed;
	}
	
	public boolean borrowBook() {
		if(getAvailable() > 0) {
			borrowed++;
			return true;
		}
		return false;
	}
	
	public boolean returnBook() {
		if(borrowed > 0) {
			borrowed--;
			return true;
		}
		return false;
	}
	
	public BookRecord clone() {
		BookRecord br = new BookRecord(book, count);
		br.borrowed = borrowed;
		return br;
	}
}
