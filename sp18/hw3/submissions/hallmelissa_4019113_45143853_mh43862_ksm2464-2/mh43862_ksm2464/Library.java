import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class Library {

  private HashMap<String, Integer> inventory;
  private ArrayList<String> titles;
  private HashMap<String, HashMap<Integer, String>> students;
  private AtomicInteger id;

  public Library(HashMap<String, Integer> inventory, ArrayList<String> titles){
    this.inventory = inventory;
    this.titles = titles;
    this.students = new HashMap<String, HashMap<Integer, String>>();
    this.id = new AtomicInteger(1);
  }

  public synchronized String borrow(String student_name, String book_name){
    Integer count = inventory.get(book_name);
    if(count == null){
      return "Request Failed - We do not have this book\n";
    }
    if(count == 0){
      return "Request Failed - Book not available\n";
    }

    inventory.put(book_name, inventory.get(book_name)-1);

    if(!students.containsKey(student_name)){
      students.put(student_name, new HashMap<Integer, String>());
    }
    students.get(student_name).put(id.get(), book_name);

    return "Your request has been approved, "+id.getAndIncrement()+" "+student_name+" "+book_name+"\n";

  }

  public synchronized String to_return(Integer record_id){

    String ret = "";

    Set<String> it = students.keySet();
    String book = "";
    for(String s:it){
      Set<Integer> rec_it = students.get(s).keySet();
      for(Integer i:rec_it){
        if(i==record_id){
          book = students.get(s).get(i);
          students.get(s).remove(i);
          inventory.put(book, inventory.get(book)+1);
          break;
        }
      }
      if(!book.equals("")){
        break;
      }
    }

    if(book.equals("")){
      ret = record_id+" not found, no such borrow record\n";
    }
    else{
      ret = record_id+" is returned\n";
    }

    return ret;
  }

  public synchronized String list(String student_name){
    
    String ret = "";

    if(students.get(student_name) == null || students.get(student_name).size()==0){
      return "No record found for "+student_name+"\n";
    }

    Set<Integer> it = students.get(student_name).keySet();
    Integer[] it_arr = it.toArray(new Integer[it.size()]);
    Arrays.sort(it_arr);

    for(Integer i:it_arr){
      ret+=i+" "+students.get(student_name).get(i)+"\n";
    }

    return ret;
  }

  public synchronized String inventory_count(){

    String ret = "";

    for(int i=0; i<titles.size(); i+=1){
      ret += titles.get(i)+" "+inventory.get(titles.get(i))+"\n";
    }

    return ret;
  }

  public synchronized String exit(){

    String ret = "exit\n";

    return ret+inventory_count();
  }
}


