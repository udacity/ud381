package udacity.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.task.ShellBolt;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import udacity.storm.spout.RandomSentenceSpout;

import backtype.storm.utils.Utils;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This topology demonstrates how to count distinct words from
 * a stream of words.
 * 
 * This is an example for Udacity Real Time Analytics Course - ud381
 *
 */
public class WordCountTopology {

  /**
   * Constructor - does nothing
   */
  private WordCountTopology() { }

  /**
   * A spout that emits a random word
   */
  static class WordSpout extends BaseRichSpout {

    // Random number generator
    private Random rnd;  

    // To output tuples from spout to the next stage
    private SpoutOutputCollector collector;

    // For storing the list of words to be fed into the topology
    private String[] wordList;

    @Override
    public void open(
        Map                     map, 
        TopologyContext         topologyContext,
        SpoutOutputCollector    spoutOutputCollector) 
    {

      // initialize the random number generator
      rnd = new Random(31);

      // save the output collector for emitting tuples
      collector = spoutOutputCollector;

      // initialize a set of words
      wordList = new String[]{"Jack", "Mary", "Jill", "McDonald"};
    }

    @Override
    public void nextTuple() 
    {
      // sleep a second before emitting any word
      Utils.sleep(1000);

      // generate a random number based on the wordList length 
      int nextInt = rnd.nextInt(wordList.length);

      // emit the word chosen by the random number from wordList
      collector.emit(new Values(wordList[nextInt]));
    }

    @Override
    public void declareOutputFields(
        OutputFieldsDeclarer outputFieldsDeclarer)
    {
      // tell storm the schema of the output tuple for this spout
      // tuple consists of a single column called 'word' 
      outputFieldsDeclarer.declare(new Fields("word"));
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
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) 
    {
      // nothing to add - since it is the final bolt
    }
  }

  public static void main(String[] args) throws Exception {

    // create the topology
    TopologyBuilder builder = new TopologyBuilder();

    // attach the word spout to the topology - parallelism of 5
    builder.setSpout("word-spout", new WordSpout(), 5);

    // attach the count bolt using fields grouping - parallelism of 15
    builder.setBolt("count-bolt", new CountBolt(), 15).fieldsGrouping("word-spout", new Fields("word"));

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
      cluster.submitTopology("word-count", conf, builder.createTopology());

      // let the topology run for 10 seconds. note topologies never terminate!
      Thread.sleep(10000);

      // we are done, so shutdown the local cluster
      cluster.shutdown();
    }
  }
}
