import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Pattern;

public class Diff {
	public static void main(String...args) throws Exception {
		BufferedReader br0 = new BufferedReader(new FileReader(args[0]));
		BufferedReader br1 = new BufferedReader(new FileReader(args[1]));
		String line0 = "";
		String text0 = "";
		String line1 = "";
		String text1 = "";

		while((line1 = br1.readLine()) != null) {	
			text1 = text1 + line1;	
		}
		while((line0 = br0.readLine()) != null) {
			text0 = text0 + line0;
		}

		boolean b = Pattern.matches(text1, text0);
		if(!b) {
			File file = new File("diff.txt");
			file.createNewFile();
		}
	}
}


			
			
