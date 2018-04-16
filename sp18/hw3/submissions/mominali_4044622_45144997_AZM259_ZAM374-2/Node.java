/*
 *  Node.java
 *  EE 360P Homework 3
 *
 *  Created by Ali Ziyaan Momin and Zain Modi on 03/02/2018.
 *  EIDs: AZM259 and ZAM374
 *
 */

public class Node {

    private String studentName;
    private int recordID;
    private String bookName;

    Node(String studentName, int recordID, String bookName){
        this.studentName = studentName;
        this.recordID = recordID;
        this.bookName = bookName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getRecordID() {
        return recordID;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }
}
