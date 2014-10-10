package udacity.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.Map;
import java.util.Arrays;

/**
 * A bolt that parses the tweet into words
 */
public class ParseTweetBolt extends BaseRichBolt
{
  // To output tuples from this bolt to the count bolt
  OutputCollector collector;

  private String[] skipWords = {"rt", "to", "me","la","on","that","que",
    "followers","watch","know","not","have","like","I'm","new","good","do",
    "more","es","te","followers","Followers","las","you","and","de","my","is",
    "en","una","in","for","this","go","en","all","no","don't","up","are",
    "http","http:","https","https:","http://","https://","with","just","your",
    "para","want","your","you're","really","video","it's","when","they","their","much",
    "would","what","them","todo","FOLLOW","retweet","RETWEET","even","right","like",
    "bien","Like","will","Will","pero","Pero","can't","were","Can't","Were","TWITTER",
    "make","take","This","from","about","como","esta","follows","followed"};

  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {
    // save the output collector for emitting tuples
    collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple)
  {
    // get the 1st column 'tweet' from tuple
    String tweet = tuple.getString(0);

    // provide the delimiters for splitting the tweet
    String delims = "[ .,?!]+";

    // now split the tweet into tokens
    String[] tokens = tweet.split(delims);

    // for each token/word, emit it
    for (String token: tokens) {
      //emit only words greater than length 3 and not stopword list
      if(token.length() > 3 && !Arrays.asList(skipWords).contains(token)){
        if(token.startsWith("#")){
          collector.emit(new Values(token));
        }
      }
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer)
  {
    // tell storm the schema of the output tuple for this spout
    // tuple consists of a single column called 'tweet-word'
    declarer.declare(new Fields("tweet-word"));
  }

}
