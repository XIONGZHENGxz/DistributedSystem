import java.util.ArrayList;
import java.io.*;

public class Inventory {
	
	class Book {
		private String name;
		private int amount;

		public Book(String name, int amount) {
			this.name = name;
			this.amount = amount;			
		}

		public String toString() {
			return (name + " " + amount);
		}
	}

	private ArrayList<Book> inventory;

	public Inventory() {
		inventory = new ArrayList<Book>();
	}		

	public void add(String name, int amount) {
		inventory.add(new Book(name, amount));
	}

	public synchronized int search(String book) {
		
		for (int i = 0; i < inventory.size(); i++) {
			//System.out.println(inventory.get(i).name + " " + book);
			if (inventory.get(i).name.equals(book)) {
				return i;
			}
		}

		return -1;
	}

	public synchronized int borrow(String book) {

		//this.print();

		int index = this.search(book);

		if (index != -1) {
			if(inventory.get(index).amount == 0) {
				//all books lent out
				return -1;
			}

			inventory.get(index).amount--;
			//successful borrow
			return 0;
		}

		//book does not exist in inventory
		return -2;
	}

	public synchronized void ret(String book) {
		int index = this.search(book);

		inventory.get(index).amount++;
	}

	public synchronized String list() {
		String list = "";

		for (int i = 0; i < inventory.size(); i++) {
			list += inventory.get(i).toString() + "\n";
			//System.out.println("[debug]\n" + list);
		}

		return list;
	}

	public synchronized void exit() {
		try {
			PrintWriter writer = new PrintWriter("inventory.txt", "UTF-8");
			writer.print(this.list());
			writer.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public synchronized void print() {
		System.out.println(this.list());	
	}
}
