import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Library {

    final Map<String, Integer> inventory;
    private final List<List<String>> recordTable;

    Library(String fileName) throws FileNotFoundException {
        inventory = loadInventory(fileName);
        recordTable = Collections.synchronizedList(new ArrayList<>());
    }

    int borrow(String studentName, String bookName) {
        int numBooksAvailable;
        synchronized (inventory) {
            numBooksAvailable = inventory.getOrDefault(bookName, -1);
            switch (numBooksAvailable) {
                default:
                    inventory.put(bookName, numBooksAvailable - 1);
                    List<String> info = Collections.synchronizedList(new ArrayList<>());
                    info.add(bookName);
                    info.add(studentName);
                    recordTable.add(info);
                    return recordTable.size();
                case -1:
                    return -1;
                case 0:
                    return -2;
            }
        }
    }

    boolean returnBook(int recordID) {
        try {
            String bookName = recordTable.get(recordID-1).get(0);
            synchronized (inventory) {
                inventory.put(bookName, inventory.get(bookName) + 1);
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    SortedMap<Integer, String> getBorrowRecord(String studentName) {
        int recordID = 1;
        SortedMap<Integer, String> borrowRecord = new TreeMap<>();
        synchronized (recordTable) {
            for (List<String> info : recordTable) {
                if (info.get(1).equals(studentName)) {
                    borrowRecord.put(recordID, info.get(0));
                }
                recordID++;
            }
        }
        return borrowRecord;
    }

    // Regex from https://stackoverflow.com/questions/9577930/regular-expression-to-select-all-whitespace-that-isnt-in-quotes
    private Map<String,Integer> loadInventory(String fileName) throws FileNotFoundException {
        Map<String, Integer> inventory = Collections.synchronizedMap(new LinkedHashMap<>());
        Scanner scanner = new Scanner(new File(fileName));
        String line, input[];
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            input = line.split("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            inventory.put(input[0], Integer.parseInt(input[1]));
        }
        return inventory;
    }

}
