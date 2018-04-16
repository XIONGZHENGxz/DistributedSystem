import java.util.concurrent.atomic.AtomicInteger;

public class Record {

    private static AtomicInteger uniqueID = new AtomicInteger(1);

    private Book bookCheckedOut;
    private Student student;
    private int recordID;

    public Record(Book bookCheckedOut) {
        this.bookCheckedOut = bookCheckedOut;
        this.recordID = uniqueID.getAndIncrement();
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Student getStudent() {
        return student;
    }

    public int id() {
        return recordID;
    }

    public Book getBookCheckedOut() {
        return bookCheckedOut;
    }

    @Override
    public String toString() {
        return String.format("RECORD=(%s %s %d)", bookCheckedOut, student, recordID);
    }

    public String stringForList() {
        return String.format("%d %s",recordID,bookCheckedOut.getTitle());
    }

}
