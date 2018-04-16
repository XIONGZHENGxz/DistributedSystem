
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    /* Code from http://forums.codeguru.com/showthread.php?486177-Split-string-with-double-quotes
     * This Pattern will match on either quoted text or text between space, including
     * whitespace, and accounting for beginning and end of line.
     */
    //private final Pattern csvPattern = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?:,|$)");
    private final Pattern csvPattern = Pattern.compile("\"([^\"]*)\"|(?<= |^)([^ ]*)(?: |$)");  
    private ArrayList<String> allMatches = null;        
    private Matcher matcher = null;
    private String match = null;

    public Parser() {                
        allMatches = new ArrayList<String>();
        matcher = null;
        match = null;
    }

    public String[] parse(String csvLine) {
        matcher = csvPattern.matcher(csvLine);
        allMatches.clear();
        String match;
        while (matcher.find()) {
            match = matcher.group(1);
            if (match!=null) {
                    allMatches.add(match);
            }
            else {
                    allMatches.add(matcher.group(2));
            }
        }
        List<String> results = new ArrayList<String>();
        for(int i = 0 ; i < allMatches.size(); i++ ) {
            if(!allMatches.get(i).equals("")) {
                results.add(allMatches.get(i).trim());
            }
        }
        if (results.size() > 0) {
                return results.toArray(new String[results.size()]);
        }
        else {
                return new String[0];
        }                       
    }   
}
