public class Record {
	private final int id;
	private final String student, book;
	private Record(final int _id, final String _student, final String _book) {
		this.id = _id;
		this.student = _student;
		this.book = _book;
	}
	
	public int getId() {
		return id;
	}
	
	public String getStudent() {
		return student;
	}
	
	public String getBook() {
		return book;
	}
	
	public String toString() {
		return String.format("%d %s", this.getId(), this.getBook());
	}
	
	public static class RecordBuilder {
		private int id;
		private String student, book;
		
		public RecordBuilder() {
			
		}
		
		public RecordBuilder(int _id, String _student, String _book) {
			id = _id;
			student = _student;
			book = _book;
		}
		
		public RecordBuilder id(int _id) {
			id = _id;
			return this;
		}
		
		public RecordBuilder student(String _student) {
			student = _student;
			return this;
		}
		
		public RecordBuilder book(String _book) {
			book = _book;
			return this;
		}
		
		public Record createRecord() {
			return new Record(id, student, book);
		}
	}
}
