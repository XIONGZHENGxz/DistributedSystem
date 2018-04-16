import java.util.*;

public class BookStorage {
    private Map<String, Integer> inventory;
    private ArrayList<Students> students;
    private int count = 0;
    public BookStorage(Map<String, Integer> inventory){
        this.inventory = inventory;
        students = new ArrayList<>();
    }
    public synchronized int borrow(String stdname, String bookname){
        if(!(inventory.containsKey(bookname))){
            return -2;
        }
        int numberleft = inventory.get(bookname);
        if(numberleft == 0){
            return -1;
        }
        for(Students sample : students) {
            if(sample.name.equals(stdname)) {
                count++;
                Map<Integer, String > good = sample.bookID;
                good.put(count, bookname);
                inventory.put(bookname, numberleft-1);
                return count;
            }
        }
        count++;
        Map<Integer, String> good = new HashMap<>();
        good.put(count, bookname);
        Students student = new Students(stdname, good);
        students.add(student);
        inventory.put(bookname, numberleft - 1);
        return count;
    }
    public synchronized boolean return_1(int input){
        for(int i = 0; i < students.size(); i++){
            Students allstu = students.get(i);
            Map<Integer, String> result = allstu.bookID;
            String returnval = result.get(input);
            if(result.get(input) != null){
                int a = inventory.get(returnval);
                inventory.put(returnval, a + 1);
                result.remove(input);
                if(result.isEmpty()){
                    students.remove(i);
                }
                return true;
            }
        }
        return false;
    }
    public synchronized Map<Integer, String> list(String stdname){
        for(int i = 0; i < students.size(); i++) {
            Students temp = students.get(i);
            if (temp.name.equals(stdname)) {
                Map<Integer, String> result = temp.bookID;
                return result;
            }
        }
        return null;
    }
    public synchronized Map<String, Integer> inventory(){
        return this.inventory;
    }
}
