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

import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StallWarning;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

/**
 * This is a basic example of a Storm topology.
 */
public class TweetTopology {

  /**
   * A spout that uses Twitter streaming API for continuously
   * getting tweets
   */
  public static class TweetSpout extends BaseRichSpout
  {
    // Twitter API authentication credentials
    String custkey, custsecret;
    String accesstoken, accesssecret;

    // To output tuples from spout to the next stage bolt
    SpoutOutputCollector collector;

    // Twitter4j - twitter stream to get tweets
    TwitterStream twitterStream;

    // Shared queue for getting buffering tweets received
    LinkedBlockingQueue<String> queue = null;

    // Class for listening on the tweet stream - for twitter4j
    private class TweetListener implements StatusListener {

      // Implement the callback function when a tweet arrives
      @Override
      public void onStatus(Status status)
      {
        // add the tweet into the queue buffer
        queue.offer(status.getText());
      }

      @Override
      public void onDeletionNotice(StatusDeletionNotice sdn)
      {
      }

      @Override
      public void onTrackLimitationNotice(int i)
      {
      }

      @Override
      public void onScrubGeo(long l, long l1)
      {
      }

      @Override
      public void onStallWarning(StallWarning warning)
      {
      }

      @Override
      public void onException(Exception e)
      {
        e.printStackTrace();
      }
    };

    /**
     * Constructor for tweet spout that accepts the credentials
     */
    public TweetSpout(
        String                key,
        String                secret,
        String                token,
        String                tokensecret)
    {
      custkey = key;
      custsecret = secret;
      accesstoken = token;
      accesssecret = tokensecret;
    }

    @Override
    public void open(
        Map                     map,
        TopologyContext         topologyContext,
        SpoutOutputCollector    spoutOutputCollector)
    {
      // create the buffer to block tweets
      queue = new LinkedBlockingQueue<String>(1000);

      // save the output collector for emitting tuples
      collector = spoutOutputCollector;


      // build the config with credentials for twitter 4j
      ConfigurationBuilder config =
          new ConfigurationBuilder()
                 .setOAuthConsumerKey(custkey)
                 .setOAuthConsumerSecret(custsecret)
                 .setOAuthAccessToken(accesstoken)
                 .setOAuthAccessTokenSecret(accesssecret);

      // create the twitter stream factory with the config
      TwitterStreamFactory fact =
          new TwitterStreamFactory(config.build());

      // get an instance of twitter stream
      twitterStream = fact.getInstance();

      // provide the handler for twitter stream
      twitterStream.addListener(new TweetListener());

      // start the sampling of tweets
      twitterStream.sample();
    }

    @Override
    public void nextTuple()
    {
      // try to pick a tweet from the buffer
      String ret = queue.poll();

      // if no tweet is available, wait for 50 ms and return
      if (ret==null)
      {
        Utils.sleep(50);
        return;
      }

      // now emit the tweet to next stage bolt
      collector.emit(new Values(ret));
    }

    @Override
    public void close()
    {
      // shutdown the stream - when we are going to exit
      twitterStream.shutdown();
    }

    /**
     * Component specific configuration
     */
    @Override
    public Map<String, Object> getComponentConfiguration()
    {
      // create the component config
      Config ret = new Config();

      // set the parallelism for this spout to be 1
      ret.setMaxTaskParallelism(1);

      return ret;
    }

    @Override
    public void declareOutputFields(
        OutputFieldsDeclarer outputFieldsDeclarer)
    {
      // tell storm the schema of the output tuple for this spout
      // tuple consists of a single column called 'tweet'
      outputFieldsDeclarer.declare(new Fields("tweet"));
    }
  }

  /**
   * A bolt that parses the tweet into words
   */
  public static class ParseTweetBolt extends BaseRichBolt
  {
    // To output tuples from this bolt to the count bolt
    OutputCollector collector;

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
        collector.emit(new Values(token));
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

  /**
   * A bolt that counts the words that it receives
   */
  static class CountBolt extends BaseRichBolt {

    // To output tuples from this bolt to the next stage bolts, if any
    private OutputCollector collector;

    // Map to store the count of the words
    private Map<String, Integer> countMap;

    @Override
    public void prepare(
        Map                     map,
        TopologyContext         topologyContext,
        OutputCollector         outputCollector)
    {

      // save the collector for emitting tuples
      collector = outputCollector;

      // create and initialize the map
      countMap = new HashMap<String, Integer>();
    }

    @Override
    public void execute(Tuple tuple)
    {
      // get the word from the 1st column of incoming tuple
      String word = tuple.getString(0);

      // check if the word is present in the map
      if (countMap.get(word) == null) {

        // not present, add the word with a count of 1
        countMap.put(word, 1);
      } else {

        // already there, hence get the count
        Integer val = countMap.get(word);

        // increment the count and save it to the map
        countMap.put(word, ++val);
      }

      // emit the word and count
      collector.emit(new Values(word, countMap.get(word)));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
    {
      // tell storm the schema of the output tuple for this spout
      // tuple consists of a two columns called 'word' and 'count'

      // declare the first column 'word', second column 'count'
      outputFieldsDeclarer.declare(new Fields("word","count"));
    }
  }

  /**
   * A bolt that prints the word and count to redis
   */
  static class ReportBolt extends BaseRichBolt
  {
    // place holder to keep the connection to redis
    transient RedisConnection<String,String> redis;

    @Override
    public void prepare(
        Map                     map,
        TopologyContext         topologyContext,
        OutputCollector         outputCollector)
    {
      // instantiate a redis connection
      RedisClient client = new RedisClient("localhost",6379);

      // initiate the actual connection
      redis = client.connect();
    }

    @Override
    public void execute(Tuple tuple)
    {
      // access the first column 'word'
      String word = tuple.getStringByField("word");

      // access the second column 'count'
      Integer count = tuple.getIntegerByField("count");

      // publish the word count to redis using word as the key
      redis.publish("WordCountTopology", word + "|" + Long.toString(count));
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
      // nothing to add - since it is the final bolt
    }
  }

  public static void main(String[] args) throws Exception
  {
    // create the topology
    TopologyBuilder builder = new TopologyBuilder();

    /*
     * In order to create the spout, you need to get twitter credentials
     * If you need to use Twitter firehose/Tweet stream for your idea,
     * create a set of credentials by following the instructions at
     *
     * https://dev.twitter.com/discussions/631
     *
     */

    // now create the tweet spout with the credentials
    TweetSpout tweetSpout = new TweetSpout(
        //"[Your customer key]",
        //"[Your secret key]",
        //"[Your access token]",
        //"[Your access secret]"
    );

    // attach the tweet spout to the topology - parallelism of 1
    builder.setSpout("tweet-spout", tweetSpout, 1);

    //*********************************************************************
    // Complete the Topology.
    // Part 1: // attach the parse tweet bolt, parallelism of 10 (what grouping is needed?)
    // Part 2: // attach the count bolt, parallelism of 15 (what grouping is needed?)
    // Part 3: attach the report bolt, parallelism of 1 (what grouping is needed?)
    // Submit and run the topology.


    //*********************************************************************


    // create the default config object
    Config conf = new Config();

    // set the config in debugging mode
    conf.setDebug(true);

    if (args != null && args.length > 0) {

      // run it in a live cluster

      // set the number of workers for running all spout and bolt tasks
      conf.setNumWorkers(3);

      // create the topology and submit with config
      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

    } else {

      // run it in a simulated local cluster

      // set the number of threads to run - similar to setting number of workers in live cluster
      conf.setMaxTaskParallelism(3);

      // create the local cluster instance
      LocalCluster cluster = new LocalCluster();

      // submit the topology to the local cluster
      cluster.submitTopology("tweet-word-count", conf, builder.createTopology());

      // let the topology run for 30 seconds. note topologies never terminate!
      Utils.sleep(30000);

      // now kill the topology
      cluster.killTopology("tweet-word-count");

      // we are done, so shutdown the local cluster
      cluster.shutdown();
    }
  }
}
