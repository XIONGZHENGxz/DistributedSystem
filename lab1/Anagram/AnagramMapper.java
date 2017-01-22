import java.util.Arrays;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;
public class AnagramMapper extends Mapper<Object,Text,Text,Text>{
	private Text sortedWord=new Text();
	private Text originalWord=new Text();

	@Override 
	public void map(Object key,Text word,Context context) throws IOException,InterruptedException{
		String value=word.toString();
		char[] chs=value.toCharArray();
		Arrays.sort(chs);
		sortedWord.set(new String(chs));
		originalWord.set(value);
		context.write(sortedWord,originalWord);
	}
}

