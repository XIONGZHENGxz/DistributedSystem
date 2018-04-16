import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class Book {

    private String title;
    private int count;
    private List<Record> records;

    public Book(String title, int count) {
        this.title = title;
        this.count = count;
        this.records = new ArrayList<>();
    }

    public int getCount() {
        return count;
    }

    public void checkoutBook(Record r) {
        if (count == 0)
            throw new NoSuchElementException("Attempted checkout of book: " + title);
        records.add(r);
        count--;
    }

    public void returnBook(int returnID) {

        Record desiredRecord = null;
        for (Record r : records) {
            if (r.id() == returnID) {
                desiredRecord = r;
            }
        }

        if (desiredRecord == null) {
            throw new NoSuchElementException(String.format("No record of id %d found in book %s",returnID,title));
        } else {
            count++;
            records.remove(desiredRecord);
        }

    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public String toString() {
        return title + " " + Integer.toString(count);
    }

    public String detailedString() {
        return "BOOK=("+toString() + " " + records+")";
    }

}
