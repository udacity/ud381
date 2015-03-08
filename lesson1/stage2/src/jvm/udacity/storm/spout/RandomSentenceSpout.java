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
import org.adrianwalker.multilinestring.Multiline;

public class RandomSentenceSpout extends BaseRichSpout {
  SpoutOutputCollector _collector;
  Random _rand;

  /**
Help, I need somebody
Help, not just anybody
Help, you know I need someone, help

When I was younger (So much younger than) so much younger than today
(I never needed) I never needed anybody's help in any way
(Now) But now these days are gone (These days are gone), I'm not so self assured
(I know I've found) Now I find I've changed my mind and opened up the doors

Help me if you can, I'm feeling down
And I do appreciate you being 'round
Help me get my feet back on the ground
Won't you please, please help me

(Now) And now my life has changed in oh so many ways
(My independence) My independence seems to vanish in the haze
(But) But every now (Every now and then) and then I feel so insecure
(I know that I) I know that I just need you like I've never done before

Help me if you can, I'm feeling down
And I do appreciate you being 'round
Help me get my feet back on the ground
Won't you please, please help me

When I was younger so much younger than today
I never needed anybody's help in any way
(But) But now these days are gone (These days are gone), I'm not so self assured
(I know I've found) Now I find I've changed my mind and opened up the doors

Help me if you can, I'm feeling down
And I do appreciate you being round
Help me, get my feet back on the ground
Won't you please, please help me, help me, help me, ooh


  */
  @Multiline private static String _beat_help_song;

  private String[] _parsed_song;

  @Override
  public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
    _collector = collector;
    _rand = new Random();
    _parsed_song = _beat_help_song.split("[\\r\\n]+");
  }

  @Override
  public void nextTuple() {
    Utils.sleep(100);

    String sentence =  _parsed_song[_rand.nextInt(_parsed_song.length)];
    _collector.emit(new Values(sentence));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("sentence"));
  }

}
