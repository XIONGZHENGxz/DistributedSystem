//EID: pya74, brw922

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookInventory {
  private LinkedHashMap<String, Integer> inventory = new LinkedHashMap<>(); // book title, book count
  private HashMap<String, LinkedHashMap<Integer, String>> records = new LinkedHashMap<>(); // student name, books checked out by student
  static int recordID = 0;
  
  public BookInventory(ArrayList<String> titles, ArrayList<Integer> count) {
    for (int i = 0; i < titles.size(); i++) {
      inventory.put(titles.get(i), count.get(i));
    }   
  }
  
  public synchronized int borrowBook(String studentName, String bookName) {
    if (inventory.get(bookName) > 0) {
      inventory.put(bookName, inventory.get(bookName) - 1);
      recordID++;
      
      if (!records.containsKey(studentName)) { // if records don't contain student, add student records
        LinkedHashMap<Integer, String> newRecord = new LinkedHashMap<>();
        newRecord.put(recordID, bookName);
        records.put(studentName, newRecord);
      } else {
        HashMap<Integer, String> studentRecord = records.get(studentName);
        studentRecord.put(recordID, bookName);
      }
      
      return recordID;
    }
    
    return -1;
  }
  
  public synchronized boolean returnBook(int recordID) {
    for (String studentName : records.keySet()) { // search through each student
      HashMap<Integer, String> studentRecord = records.get(studentName);
      if (studentRecord.containsKey(recordID)) {
        String bookName = studentRecord.get(recordID);
        studentRecord.remove(recordID); // remove from student record
        inventory.put(bookName, inventory.get(bookName) + 1); // increment book count
        
        return true;
      }
    }
    
    return false;
  }
  
  public synchronized String getRecordStr(String studentName) {
    if(records.get(studentName) == null){
      return null;
    }
    String listStr = "";
    boolean first = true;
    Iterator<Map.Entry<Integer, String>> it = records.get(studentName).entrySet().iterator();
    while (it.hasNext()){
      Map.Entry<Integer, String> pair = (Map.Entry<Integer, String>)it.next();
      if(!first){
        listStr += "  ";
      }else {
        first = false;
      }
      listStr += pair.getKey() + " " + pair.getValue();
    }
    return listStr;
  }
  
  public synchronized String getInventoryStr() {
    String inventoryStr = "";
    Iterator<Map.Entry<String, Integer>> it = inventory.entrySet().iterator();
    while (it.hasNext()){
      Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
      inventoryStr += pair.getKey() + " " + pair.getValue();
      //adding double spaces for parsing purposes
      if(it.hasNext()){
        inventoryStr += "  ";
      }
    }
    return inventoryStr;
  }
  
//  public LinkedHashMap<Integer, String> getRecord(String studentName) {
//    return records.get(studentName);
//  }
//  
//  public LinkedHashMap<String, Integer> getInventory() {
//    return inventory;
//  }
  
}