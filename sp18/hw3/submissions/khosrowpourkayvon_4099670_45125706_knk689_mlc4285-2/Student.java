import java.util.ArrayList;
import java.util.List;

public class Student {

    private String name;
    private List<Record> records;

    public Student(String name) {
        this.name = name;
        this.records = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Record> getRecords() {
        return records;
    }
    @Override
    public String toString() {
        return name;
    }

    public String detailedString() {
        return "STUDENT=("+name + " " + records+")";
    }

}
