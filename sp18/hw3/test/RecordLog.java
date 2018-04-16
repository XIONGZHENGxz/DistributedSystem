import java.util.ArrayList;

public class RecordLog {

    private int length;
    private ArrayList<Record> list;

    public RecordLog(){
        this.list = new ArrayList<Record>();
        this.length = 0;
    }

    public synchronized void add(String user, String book, int id){
        Record a = new Record(user, book, id);
        list.add(a);
        length++;
    }


    public synchronized boolean returnBook(int id){
        for (Record a : list) {
            if (a.getId() == id) {
                return a.returnBook();
            }
        }
        return false;
    }

    public synchronized int getLength(){
        return length;
    }

    public synchronized ArrayList<Record> userOrders(String user){
        ArrayList<Record> records = new ArrayList<Record>();
        for (Record a : list) {
            if(a.getUser().equals(user)&& !a.getReturned()){
                records.add(a);
            }
        }
        return records;
    }

    public String getBook(int id){
        return list.get(id - 1).getBook();
    }

}