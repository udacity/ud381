package udacity.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;
import udacity.storm.tools.Rankable;
import udacity.storm.tools.Rankings;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * A bolt to join the tweets coming from {@link udacity.storm.TweetSpout} containing
 * top hashtags coming from the {@link udacity.storm.TotalRankingsBolt}
 * Note: The window is determined by the rate at which top hashtags are emitted
 */
public class TweetsWithTopHashtagsBolt extends BaseRichBolt {

  private static final Logger LOG = Logger.getLogger(TweetsWithTopHashtagsBolt.class);

  OutputCollector _collector;
  //Note: Unbounded queue might can cause gc issues if not dequed in time
  Queue<String> tweetsQueue = new LinkedList<String>();

  @Override
  public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
    _collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple) {
    String componentId = tuple.getSourceComponent();

    /*
     * If it is a tweet stream tuple, store it in-memory queue for now.
     * Else if it is a top hashtags tuple, dequeue the tweets from the queue,
     * compute the ones that contain these hashtags and emit them.
     */
    if(componentId.equals("tweet-spout")) {
      String tweet = tuple.getString(0);
      tweetsQueue.add(tweet);
    } else if(componentId.equals("total-ranker")) {

      Rankings rankings = (Rankings) tuple.getValue(0);

      for(String tweetWithTopHashTag: findTweetsWithHashtags(rankings)) {
        _collector.emit(new Values(tweetWithTopHashTag));
        LOG.debug("Tweet with top hashtag = " + tweetWithTopHashTag);
      }
    }
  }

  /*
   *  Compute the tweets containing ranked hashtags
   */
  private Iterable<String> findTweetsWithHashtags(Rankings rankings) {
    List<String> topHashTags = new LinkedList<String>();
    List<String> tweetsWithTopHashtags = new LinkedList<String>();

    for(Rankable r: rankings.getRankings()) {
      topHashTags.add(r.getObject().toString());
    }

    while(!tweetsQueue.isEmpty()) {
      //dequeue a tweet
      String tweet = tweetsQueue.poll();
      Boolean containsHashTag = false;

      for(String hashTag: topHashTags) {
        if(tweet.contains(hashTag)) {
          containsHashTag = true;
        }
      }

      if(containsHashTag) {
        tweetsWithTopHashtags.add(tweet);
      }
    }

    return tweetsWithTopHashtags;
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    outputFieldsDeclarer.declare(new Fields("tweets"));
  }
}
