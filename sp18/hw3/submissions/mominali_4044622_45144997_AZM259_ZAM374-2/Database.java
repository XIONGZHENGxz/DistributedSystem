/*
 *  Database.java
 *  EE 360P Homework 3
 *
 *  Created by Ali Ziyaan Momin and Zain Modi on 03/02/2018.
 *  EIDs: AZM259 and ZAM374
 *
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Database {

    public ConcurrentHashMap<String, Integer> booksList;
    private List<Node> borrowRecord;
    public ArrayList<String> insertionOrder;

    public Database(){
        booksList = new ConcurrentHashMap<>();
        borrowRecord = Collections.synchronizedList(new ArrayList<>());
        insertionOrder = new ArrayList<>();
    }

    public synchronized String getBetweenQuotes(String input){
        String retVal = null;
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(input);
        while (m.find()) {
            retVal = m.group(1);
            break;
        }
        return retVal;
    }

    public synchronized String borrowBook(String studentName, String bookName){
        String ret;
        if(booksList.get(bookName) != null) {
            if (!booksList.get(bookName).equals(0)) {

                Node node;
                int recordID;
                if (borrowRecord.size() != 0) {
                    Node temp = (Node) borrowRecord.get(borrowRecord.size() - 1);
                    recordID = temp.getRecordID() + 1;
                } else {
                    recordID = 1;
                }

                node = new Node(studentName, recordID, bookName);
                borrowRecord.add(node);

                Integer temp = booksList.get(bookName);
                temp -= 1;
                temp = temp < 0 ? 0 : temp;

                booksList.put(bookName, temp);
                ret = ("Your request has been approved, " + Integer.toString(recordID) + " " + studentName + " " + "\"" + bookName + "\"");
                System.out.println(booksList);
            }
            else{
                //tell that bitch the book is gone
                ret = ("Request Failed - Book not available");
            }
        }
        else{
            //tell that bitch the book is gone
            ret = ("Request Failed - Book not available");
        }
        return ret;
    }

    public synchronized String returnBook(int recordID){
        String ret;

        Node ndiq = null;
        boolean found = false;
        for(Object temp: borrowRecord){
            ndiq = (Node) temp;
            if(ndiq.getRecordID() == recordID){
                found = true;
                break;
            }
        }

        if(found){
            Integer count = booksList.get(ndiq.getBookName()) + 1;
            booksList.put(ndiq.getBookName(), count);
            borrowRecord.remove(ndiq);
            ret = (Integer.toString(ndiq.getRecordID()) + " is returned");
        }
        else{
            ret = (Integer.toString(recordID) + " not found");
        }

        return ret;
    }

    public synchronized String listBorrowedBooks(String studentName){
        String ret = "";
        Node ndiq = null;
        for(Object temp: borrowRecord){
            ndiq = (Node) temp;
            if(ndiq.getStudentName().equals(studentName)){
                if(ret.length() == 0){
                    ret = ndiq.getRecordID() + " " + studentName;
                }
                else {
                    ret = ret + "\n" + ndiq.getRecordID() + " " + studentName;
                }
            }
        }

        if(ret.length() == 0){
            ret = "No record found for " + studentName;
        }

        return ret;
    }

    public synchronized String getInventory(){
        StringBuilder ret = new StringBuilder();
        File inventory = new File("inventory.txt");

        try{
            Scanner sc = new Scanner(new FileReader(inventory));

            while(sc.hasNextLine()){
                if(ret.length() == 0){
                    ret.append(sc.nextLine());
                }
                else {
                    ret.append("\n");
                    ret.append(sc.nextLine());
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return ret.toString();
    }

    public synchronized void writeInventory(){
        String ret = "";

        for(String bookName: insertionOrder){
            Integer quantity = booksList.get(bookName);
            if(ret.length() == 0){
                ret = "\"" + bookName + "\"" + " " + Integer.toString(quantity);
            }
            else {
                ret = ret + "\n" + "\"" + bookName + "\"" + " " + Integer.toString(quantity);
            }
        }

        File inventory = new File("inventory.txt");

        try{
            FileWriter fileWriter = new FileWriter(inventory);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.println(ret);

            printWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
