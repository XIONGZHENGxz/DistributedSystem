public class Request {
	private final int id;
	private final String student;
	private final String book;
	
	public Request(int id, String student, String book) {
		this.id = id;
		this.student = student;
		this.book = book;
	}
	
	public int getID() {
		return id;
	}
	
	public String getStudent() {
		return student;
	}
	
	public String getBook() {
		return book;
	}
}

