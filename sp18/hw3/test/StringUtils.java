import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	// String split on spaces unless there are quotes
	// Code from https://stackoverflow.com/questions/7804335/split-string-on-spaces-in-java-except-if-between-quotes-i-e-treat-hello-wor
	public static String[] quoteSplit(String str) {
		ArrayList<String> list = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(str);
		while (m.find())
		    list.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
		
		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}
}
