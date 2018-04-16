import java.util.HashMap;

public class Student {
    String name;
    private HashMap<Integer, String> books = new HashMap<Integer, String>();

    public Student(String name) {
        this.name = name;
    }

    public HashMap<Integer, String> getBooks() {
        return books;
    }

    public void addBook(Integer recID, String book) {
        books.put(recID, book);
    }

    public void removeBook(Integer recID) {
        books.remove(recID);
    }
}