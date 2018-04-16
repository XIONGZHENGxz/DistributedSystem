//EID: pya74, brw922

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class TCPThread implements Runnable {
  Socket socket;
  BookInventory inventory;
  
  public TCPThread(Socket socket, BookInventory inventory) {
    this.socket = socket;
    this.inventory = inventory; 
  }
  
  @Override
  public void run() {
    String output = "";
    
    Scanner sc = null;
    try {
      sc = new Scanner(socket.getInputStream());
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    try {
      PrintWriter pout = new PrintWriter(socket.getOutputStream());
    
    
      while (true) {
        String input;
        while((input = sc.nextLine()) == null) {};
        
        //System.out.println(input);
        input = input.trim();
        String[] parsedInput = input.split(" ");
        String command = parsedInput[0];
        
        if (command.equals("borrow")) {
          String studentName = parsedInput[1];
          String bookName = input.substring(input.indexOf(" \"")+ 1);
          //System.out.println(bookName);
          output = bookName; 
          
          int bookID = inventory.borrowBook(studentName, bookName);
          //System.out.println(bookID);
          if (bookID == -1){
            //TODO: check this string - conflicting on the document
            output = "Request Failed - Book not available";
          }
          else{
            output = "Your request has been approved, " + bookID + " " + studentName + " " + bookName;
          }
          
        } else if (command.equals("return")) {
          int recordID = Integer.valueOf(parsedInput[1]);
          if (inventory.returnBook(recordID)){
            output = (recordID + " is returned");
          } else {
            output = (recordID + " not found, no such borrow record");
          }
          
        } else if (command.equals("list")) {
          String studentName = parsedInput[1];
//          LinkedHashMap<Integer, String> books = inventory.getRecord(studentName);
          output = inventory.getRecordStr(studentName);
          if (output == null){
            output = "No record found for " + studentName;
          }
          
        } else if (command.equals("inventory")) {
//          LinkedHashMap<String, Integer> bookInventory = inventory.getInventory();
//          output = InventoryString(bookInventory);
          output = inventory.getInventoryStr();
          
        } else if (command.equals("exit")) {
          //print out inventory and exit
          FileWriter outputWrite;
          try {
            outputWrite = new FileWriter("inventory.txt");
            PrintWriter outputPrint = new PrintWriter(outputWrite);
            String inventoryStr = inventory.getInventoryStr();
            String[] multipleStr = inventoryStr.split("  \"");
            for(int i = 0; i<multipleStr.length; i++) {
              String outStr = "";
              if (i != 0) {
                outStr += "\"";
              }
              outStr += multipleStr[i];
              if(i == multipleStr.length - 1){
                outputPrint.print(outStr);
              }
              else{
                outputPrint.println(outStr);
              }
            }
            outputPrint.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
          
        } else if (command.equals("setmode")) {
          break;
        }
        
        //send message back to client
        pout.println(output);
        pout.flush();
      }
    
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
