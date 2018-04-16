import java.util.*;
import java.net.*;


public class MultithreadedServer {
    LinkedHashMap<String, Integer> list = BookServer.bookItems;

    public static String processResponse(String input) {
        String response;
        input = input.trim();
        String[] splitInput = input.split(" ");
        String reply;

        if(splitInput[0].equals("borrow")) {
            String[] bookSplit = input.split("\"");
            reply = Library.borrow(splitInput[1], bookSplit[1]);
        }
        else if(splitInput[0].equals("return")) {
            reply = Library.returnBook(splitInput[1]);
        }
        else if(splitInput[0].equals("list")) {
            reply = Library.list(splitInput[1]);
        }
        else if(splitInput[0].equals("inventory")) {
            reply = Library.inventory();
        }
        else {
            reply = "ERROR: No such command";
        }

        response = reply;
        return response;
    }
}
