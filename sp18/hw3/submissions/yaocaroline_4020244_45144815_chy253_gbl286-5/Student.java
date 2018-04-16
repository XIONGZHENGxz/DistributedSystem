//package hwk3;

import java.util.ArrayList;

public class Student {
	String name;
	ArrayList<Book> books;
	ArrayList<Integer> records; // list of record nums
	
	public Student (String na) {
		this.name = na;
		books = new ArrayList<Book>();
		records = new ArrayList<Integer>();
	}
	
	public void remove(int record) {
		books.remove(records.indexOf(record));
		records.remove(records.indexOf(record));
	}
	
	public void add(Book bk, int record) {
		records.add(record);
		books.add(bk);
	}
	
	
}
