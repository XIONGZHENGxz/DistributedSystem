import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AnagramReducer extends Reducer<Text,Text,Text,Text>{
	private Text result=new Text();

	@Override 
	public void reduce(Text key,Iterable<Text> values,Context context) throws IOException,InterruptedException{
		String temp="";
		for(Text val:values){
			temp+=val.toString();
		}
		result.set(temp);
		context.write(key,result);
	}
}


		
	
