package udacity.storm.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

public class MyLikesSpout extends BaseRichSpout {
  SpoutOutputCollector _collector;
  Random _rand;


  @Override
  public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
    _collector = collector;
    _rand = new Random();
  }

  @Override
  public void nextTuple() {
    Utils.sleep(100);
    String[] pairs = new String[]{
      "Lewis # Udacity",
      "Taylor # Cinematography",
      "Justine # Dogs",
      "Liz # Soccer",
      "Kim # Art"
      };
    String pair = pairs[_rand.nextInt(pairs.length)];
    String name = pair.split("#")[0].trim();
    String favorite = pair.split("#")[1].trim();
    //** TO DO: update emit and declareOutputFields to
    //** emit "name" and "favorite" instead of "pair"
    _collector.emit(new Values(pair));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("pair"));
  }

}
