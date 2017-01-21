import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;

public class WordCount2{
	public static class TokenizerMapper extends Mapper<Object,Text,Text,IntWritable>{
		static enum CountersEnum {INPUT_WORDS}
		private final static IntWritable one=new IntWritable(1);
		private  Text word=new Text();
		private boolean caseSensative;
		private Set<String> patternsToSkip = new HashSet<>();
		private Configuration conf;
		private BufferedReader br;
		@Override 
		public void setup(Context context) throws IOException, InterruptedException{
			conf=context.getConfiguration();
			caseSensative=conf.getBoolean("wordcount.case.sensative",true);
			if(conf.getBoolean("wordcount.skip.patterns",true)){
				URI[] patternsURI=Job.getInstance(conf).getCacheFiles();
				for(URI uri:patternsURI){
					Path patternPath=new Path(uri.getPath());
					parseSkipFiles(patternPath.getName().toString());
				}
			}
		}


		private void parseSkipFiles(String fileName){
			try{
				br=new BufferedReader(new FileReader(fileName));
				String pattern=null;
				while((pattern=br.readLine())!=null){
					patternsToSkip.add(pattern);
				}
			} catch (IOException e){
				e.printStackTrace();
			}
		}

		@Override 
		public void map(Object key,Text value,Context context) throws IOException, InterruptedException{
			String line=caseSensative?value.toString():value.toString().toLowerCase();
			for(String s:patternsToSkip){
				line.replaceAll(s,"");
			}
			StringTokenizer st=new StringTokenizer(line.toString());
			while(st.hasMoreTokens()){
				word.set(st.nextToken());
				context.write(word,one);
				Counter counter=context.getCounter(CountersEnum.class.getName(),CountersEnum.INPUT_WORDS.toString());
				counter.increment(1);
			}
		}
	}



	public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
		private IntWritable result=new IntWritable();

		@Override 
		public void reduce(Text key,Iterable<IntWritable> values,Context context) throws IOException, InterruptedException{
			int sum=0;
			for(IntWritable iw:values){
				sum+=iw.get();
			}
			result.set(sum);
			context.write(key,result);
		}
	}


	public static void main(String...args) throws Exception{
		Configuration conf = new Configuration();
    	GenericOptionsParser optionParser = new GenericOptionsParser(conf, args);
    	String[] remainingArgs = optionParser.getRemainingArgs();
    	if (!(remainingArgs.length != 2 || remainingArgs.length != 4)) {
      		System.err.println("Usage: wordcount <in> <out> [-skip skipPatternFile]");
      		System.exit(2);
    	}
    	Job job = Job.getInstance(conf, "word count");
    	job.setJarByClass(WordCount2.class);
   	 	job.setMapperClass(TokenizerMapper.class);
    	job.setCombinerClass(IntSumReducer.class);
    	job.setReducerClass(IntSumReducer.class);
    	job.setOutputKeyClass(Text.class);
    	job.setOutputValueClass(IntWritable.class);

    	List<String> otherArgs = new ArrayList<String>();
    	for (int i=0; i < remainingArgs.length; ++i) {
      		if ("-skip".equals(remainingArgs[i])) {
        		job.addCacheFile(new Path(remainingArgs[++i]).toUri());
        		job.getConfiguration().setBoolean("wordcount.skip.patterns", true);
      		} else {
        	otherArgs.add(remainingArgs[i]);
      		}
    	}
    	FileInputFormat.addInputPath(job, new Path(otherArgs.get(0)));
    	FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));
    	System.exit(job.waitForCompletion(true) ? 0 : 1);
  
  }
}
			
			
				
