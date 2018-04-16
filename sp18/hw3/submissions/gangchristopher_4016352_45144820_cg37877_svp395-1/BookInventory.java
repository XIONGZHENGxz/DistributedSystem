import java.util.*;
import java.io.*;

public class BookInventory {
	private ArrayList<String> names;
	private ArrayList<Integer> quantities;

	public BookInventory() {
		this.names = new ArrayList<String>();
		this.quantities = new ArrayList<Integer>();
	}

	void returnBook(String name) {
		int index = names.indexOf(name);
		quantities.set(index, quantities.get(index) + 1);
	}

	int borrowBook(String name) {
		int index = names.indexOf(name);
		if (quantities.get(index) > 0)
		{
			quantities.set(index, quantities.get(index) - 1);
			return quantities.get(index);
		}
		else
			return -1;
	}

	// Write to output file
	void getInventory(String fileName) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

			for (int i = 0; i < names.size(); i++) {
				out.println("\"" + names.get(i) + "\" " + quantities.get(i));
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int getQuantity(String name) {
		if (!names.contains(name))
			return -1;
		return quantities.get(names.indexOf(name));
	}

	void addInventory(String name, int quantity) {
		names.add(name);
		quantities.add(quantity);
	}
}