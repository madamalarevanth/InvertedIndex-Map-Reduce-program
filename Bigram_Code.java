import java.io.IOException;
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

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

public class WordCount {
  public static class TokenizerMapper extends Mapper<Object, Text, Text, Text> {
    private Text word = new Text();
    private Text docId = new Text();
    private HashSet<String> selectedBigrams = new HashSet<String>();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      selectedBigrams.add("bruce willis");
      selectedBigrams.add("computer science");
      selectedBigrams.add("information retrieval");
      selectedBigrams.add("los angeles");
      selectedBigrams.add("power politics");

      String arr[] = value.toString().split("\t",2);
      docId.set(arr[0]);
      String words = arr[1].toLowerCase().replaceAll("[^a-z]+", " ");
      StringTokenizer itr = new StringTokenizer(words);

      String first = itr.nextToken();
      while (itr.hasMoreTokens()) {
        String second = itr.nextToken();
        word.set(first + " " + second);
        //System.out.println(word);
        if (selectedBigrams.contains(word.toString())) {
          context.write(word, docId);
        }
        first = second;
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text, Text, Text, Text> {
    HashMap<String, Integer> counter = new HashMap<String, Integer>();
    private Text result = new Text();

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      counter.clear();
      for (Text val : values) {
        String docId = val.toString();
        if (counter.containsKey(docId)) {
          counter.put(docId, counter.get(docId) + 1);
        } else {
          counter.put(docId, 1);
        }
      }
      StringBuilder count = new StringBuilder();
      for (Map.Entry<String, Integer> elem : counter.entrySet()) {
        count.append(elem.getKey() + ":" + elem.getValue() + " ");
      }

      result.set(count.toString());
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "unigram-index");

    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setReducerClass(IntSumReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}// WordCount
