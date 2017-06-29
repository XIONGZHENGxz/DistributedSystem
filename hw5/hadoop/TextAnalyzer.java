import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import java.io.IOException;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {
	// Replace "?" with your own output key / value types
	// The four template data types are:
	//     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
	public static class TextMapper extends Mapper<LongWritable, Text, Text, Tuple> {
		public static Text contextWord=new Text();
		public static Text queryWord = new Text();
		public static IntWritable count = new IntWritable();

		public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException
			{
				// Implementation of you mapper function
				StringTokenizer st=new StringTokenizer(value.toString().toLowerCase().replaceAll("[^a-zA-Z0-9]"," "));
				Map<String,Integer> map=new HashMap<>();
				while(st.hasMoreTokens()){
					String currWord=st.nextToken();
					map.put(currWord,map.getOrDefault(currWord,0)+1);
				}

				for(String key1:map.keySet()) {
					contextWord.set(key1);
					for (String key2 : map.keySet()) {
						if (key2.equals(key1)) {
							if(map.get(key1)>1) {
								queryWord.set(key2);
								count.set(map.get(key2)-1);
								context.write(contextWord,new Tuple(queryWord,count));
							} else continue;
						} else{
							queryWord.set(key2);
							count.set(map.get(key2));
							context.write(contextWord,new Tuple(queryWord,count));
						}
					}
				}
			}
	}

	// Replace "?" with your own key / value types
	// NOTE: combiner's output key / value types have to be the same as those of mapper
	public static class TextCombiner extends Reducer<Text, Tuple, Text, Tuple> {
		public void reduce(Text key, Iterable<Tuple> tuples, Context context)
			throws IOException, InterruptedException
			{
				// Implementation of you combiner function
				Map<String,Integer> map=new HashMap<>();
				for(Tuple t:tuples){
					String val=t.getValue().toString();
					int count=t.getCount().get();
					map.put(val,map.getOrDefault(val,0)+count);
				}

				for(String s:map.keySet()) {
					Tuple t=new Tuple(new Text(s),new IntWritable(map.get(s)));
					context.write(key,t);
				}
			}
	}

	// Replace "?" with your own input key / value types, i.e., the output
	// key / value types of your mapper function
	public static class TextReducer extends Reducer<Text, Tuple, Text, Text> {
		private final static Text emptyText = new Text("");
		private final static Text queryWordText = new Text();

		public void reduce(Text key, Iterable<Tuple> queryTuples, Context context)
			throws IOException, InterruptedException
			{
				// Implementation of you reducer function
				Map<String,Integer> map=new HashMap<>();
				for(Tuple t:queryTuples){
					String val=t.getValue().toString();
					int c=t.getCount().get();
					map.put(val,map.getOrDefault(val,0)+c);
				}
				// Write out the results; you may change the following example
				// code to fit with your reducer function.
				//   Write out the current context key
				context.write(key, emptyText);
				for(String queryWord: map.keySet()){
					String count = map.get(queryWord).toString() + ">";
					queryWordText.set("<" + queryWord + ",");
					context.write(queryWordText, new Text(count));
				}
				context.write(emptyText, emptyText);
			}
	}

	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();

		// Create job
		Job job = Job.getInstance(conf, "xz5665"); // Replace with your EIDs
		job.setJarByClass(TextAnalyzer.class);

		// Setup MapReduce job
		job.setMapperClass(TextMapper.class);
		//   Uncomment the following line if you want to use Combiner class
		job.setCombinerClass(TextCombiner.class);
		job.setReducerClass(TextReducer.class);

		// Specify key / value types (Don't change them for the purpose of this assignment)
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		//   If your mapper and combiner's  output types are different from Text.class,
		//   then uncomment the following lines to specify the data types.
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Tuple.class);

		// Input
		FileInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(TextInputFormat.class);

		// Output
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setOutputFormatClass(TextOutputFormat.class);

		// Execute job and return status
		return job.waitForCompletion(true) ? 0 : 1;
	}

	// Do not modify the main method
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
		System.exit(res);
	}

	// You may define sub-classes here. Example:
	// public static class MyClass {
	//
	// }
}



