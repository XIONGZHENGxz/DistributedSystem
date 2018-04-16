import java.util.concurrent.atomic.AtomicInteger;

public class Book {
    private String name;
    private AtomicInteger quantity;

    public Book(String name, AtomicInteger num) {
        this.name = name;
        this.quantity = num;
    }

    public String getName() {
        return name;
    }

    public AtomicInteger getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return name + " " + quantity;
    }
}
