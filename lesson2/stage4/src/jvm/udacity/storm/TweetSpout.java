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

/**
 * A spout that uses Twitter streaming API for continuously
 * getting tweets
 */
public class TweetSpout extends BaseRichSpout
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
