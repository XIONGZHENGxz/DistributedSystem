import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Parser {
	static String filename;
	public Parser(String filename){
		this.filename = filename;
	}
	public Parser(){
		
	}
	private static final String FILENAME = "inputFile.txt";
	public static boolean isNullOrEmpty(String s){
		return s == null || s.trim().length() == 0;
	}
	public void parse(ConcurrentHashMap<String, Book> input, ArrayList<String> orderedList){
		//ConcurrentHashMap<String, Book> input = new ConcurrentHashMap<String, Book>();
		String line = null;
		String[] currentLine;
		String title = null;
		try{
			FileReader fileReader = (!isNullOrEmpty(filename)) ? new FileReader(filename) : new FileReader(FILENAME);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null){
				currentLine  = line.split("\\d+");
				int copiesOwned = Integer.parseInt(line.replaceAll("[^0-9]", ""));
				title = currentLine[0].trim();
				Book b = new Book(title, copiesOwned);				
			    input.put(title, b);	
			    orderedList.add(title);
			}
			bufferedReader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}