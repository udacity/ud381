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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

import udacity.storm.tools.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * A bolt that prints the word and count to redis
 */
public class ReportBolt extends BaseRichBolt
{
  // place holder to keep the connection to redis
  transient RedisConnection<String,String> redis;
  HashMap<String, Integer> URLCounter;
  HashMap<String, Double> sentimentURL;
  Rankings rankableList;
  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {
    // instantiate a redis connection
    RedisClient client = new RedisClient("localhost",6379);
    URLCounter = new HashMap<String, Integer>();
    sentimentURL = new HashMap<String, Double>();
    // initiate the actual connection
    redis = client.connect();
  }

  public Integer getURLCount(String url_)
  {
	  if(URLCounter.containsKey(url_))
	  {
		  return URLCounter.get(url_);
	  }
	  else
	  {
		  return 1;
	  }
  }
  public String getMostFrequentURL()
  {       

	  ValueComparator bvc =  new ValueComparator(URLCounter);
	  TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
	  String result = "";
	  int MAX = 20;
	  int counter = 0;
	  for (String key : sorted_map.keySet())
      {
		  result = result + (key + "," + sorted_map.get(key));
		  counter++;
		  if(counter > 20) break;
      }
      return result;
	  //return "no tweet";
	  
  }
  public void observeURL(String url_)
  {
	  if(URLCounter.containsKey(url_))
	  {
		  URLCounter.put(url_, URLCounter.get(url_) + 1);
	  }
	  else
	  {
		  URLCounter.put(url_,  1);
	  }
  }
  @Override
  public void execute(Tuple tuple)
  {


		
	String tweet = tuple.getStringByField("tweet");
	String tweetWord = tuple.getStringByField("tweetWord");    
    String county_id = tuple.getStringByField("county_id");
    String noun = tuple.getStringByField("noun");
    String verb = tuple.getStringByField("verb");
    String object = tuple.getStringByField("do");
    String matchedEmoticon = tuple.getStringByField("matchedEmoticon");
    String url = tuple.getStringByField("url");
    double matchedEmoticonScore= tuple.getIntegerByField("matchedEmoticonScore")*1.0;
    double sentiment = tuple.getDoubleByField("sentiment");
    System.out.println("\t\t\tDEBUG ReportBolt: " + String.valueOf(matchedEmoticonScore) + ", Tweet Sentiment:" + String.valueOf(sentiment) + "URL: " + getMostFrequentURL());
    
    
    double alpha = 0.01;
    double URLSentiment = sentiment;
    
    if(sentimentURL.containsKey(url))
    	URLSentiment = (1-alpha)*sentimentURL.get(url) + alpha * Math.max(sentiment, matchedEmoticonScore/5);
    sentimentURL.put(url, URLSentiment);
    observeURL(url);
    
    redis.publish("WordCountTopology", county_id + "DELIMITER" + tweet + "DELIMITER" + String.valueOf(sentiment) + "DELIMITER" + getMostFrequentURL());
    
    //redis.publish("WordCountTopology", county_id + "DELIMITER" + noun + " " + verb + " " + object + "DELIMITER" + String.valueOf(sentiment));
    
    
    //String enrichedURL = tuple.getStringByField("word");
    
    //System.out.print(tuple.toString() + "\n");
    // publish the word count to redis using word as the key
    //redis.publish("WordCountTopology", word + ":" + Long.toString(count));
    //redis.publish("WordCountTopology", tweets + "|" + Long.toString(count));
  }

  public void declareOutputFields(OutputFieldsDeclarer declarer)
  {
    // nothing to add - since it is the final bolt
  }
}
