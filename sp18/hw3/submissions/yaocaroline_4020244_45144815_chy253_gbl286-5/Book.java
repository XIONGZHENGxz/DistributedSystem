//package hwk3;

import java.util.ArrayList;

public class Book {
	String bookname;
	int quantity;
	ArrayList<Student> currentOwner;
	ArrayList<Integer> recordNumber;
	
	public Book (String name, int num) {
		bookname = name;
		quantity = num;
//		currentOwner = new ArrayList<Student>();
//		recordNumber = new ArrayList<Integer>();
	}
	
	public String getName() {
		return bookname;
	}
	
	public int getquantity() {
		return quantity;
	}
	
	public void decrquantity() {
		quantity--;
	}
	
	public void incrquantity() {
		quantity++;
	}
	
	public void bookOut(Student name, int record) {
		quantity--;
		name.add(this, record);
	}
	
	public void returnBook(Student name, int record) {
		quantity++;
		name.remove(record);
	}
}
