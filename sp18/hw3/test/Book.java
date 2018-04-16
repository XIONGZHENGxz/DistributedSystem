package hw3;

public class Book {
    String name;
    int recordID;
    static int idCount = 0;
    public Book(String name) {
        this.name = name;
        recordID = ++idCount;
    }
}
