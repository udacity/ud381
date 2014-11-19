package udacity.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

class TopNTweetTopology
{
  public static void main(String[] args) throws Exception
  {
    //Variable TOP_N number of words
    int TOP_N = 5;
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
    // credential
    TweetSpout tweetSpout = new TweetSpout(
            "",
            "",
            "",
            ""
    );

    // attach the tweet spout to the topology - parallelism of 1
    builder.setSpout("tweet-spout", tweetSpout, 1);

    // attach the parse tweet bolt using shuffle grouping
    builder.setBolt("parse-tweet-bolt", new ParseTweetBolt(), 10).shuffleGrouping("tweet-spout");
    builder.setBolt("infoBolt", new InfoBolt(), 10).fieldsGrouping("parse-tweet-bolt", new Fields("county_id"));
    builder.setBolt("top-words", new TopWords(), 10).fieldsGrouping("infoBolt", new Fields("county_id"));
    builder.setBolt("report-bolt", new ReportBolt(), 1).globalGrouping("top-words");

    // attach rolling count bolt using fields grouping - parallelism of 5
    //builder.setBolt("rolling-count-bolt", new RollingCountBolt(1000, 10), 1).fieldsGrouping("parse-tweet-bolt", new Fields("tweet-word"));

    //from incubator-storm/.../storm/starter/RollingTopWords.java
    //builder.setBolt("intermediate-ranker", new IntermediateRankingsBolt(TOP_N, 10), 2).fieldsGrouping("rolling-count-bolt", new Fields("obj"));
    //builder.setBolt("total-ranker", new TotalRankingsBolt(TOP_N, 2)).globalGrouping("intermediate-ranker");

    /*
     * total-ranker bolt output is broadcast (allGrouping) to all the top-tweets bolt instances so
     * that every one of them have access to the top hashtags
     * tweet-spout tweet stream will be distributed randomly to the top-tweets bolt instances
     */
    //builder.setBolt("top-tweets", new TweetsWithTopHashtagsBolt(), 4)
    //    .allGrouping("total-ranker")
    //    .shuffleGrouping("tweet-spout");

    // attach the report bolt using global grouping - parallelism of 1
    //builder.setBolt("report-bolt", new ReportBolt(), 1).globalGrouping("top-tweets");

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
      conf.setMaxTaskParallelism(4);

      // create the local cluster instance
      LocalCluster cluster = new LocalCluster();

      // submit the topology to the local cluster
      cluster.submitTopology("tweet-word-count", conf, builder.createTopology());

      // let the topology run for 300 seconds. note topologies never terminate!
      Utils.sleep(300000000);

      // now kill the topology
      cluster.killTopology("tweet-word-count");

      // we are done, so shutdown the local cluster
      cluster.shutdown();
    }
  }
}
