
package udacity.storm;

import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.task.OutputCollector;
import java.util.Map;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.task.TopologyContext;
import java.util.Arrays;
public class SplitSentenceBolt extends BaseRichBolt {
	
// To output tuples from this bolt to the next stage bolts, if any
	private OutputCollector collector;

	public void SplitSentenceBolt(){

	}

	@Override
	public void prepare(
		Map                     map,
		TopologyContext         topologyContext,
		OutputCollector         outputCollector)
	{

      // save the collector for emitting tuples
		collector = outputCollector;


	}

	@Override
	public void execute(Tuple tuple)
	{
      //**************************************************


      //Syntax to get the word from the 1st column of incoming tuple
		String sentence = tuple.getString(0);

		//split sentence into words
		if (sentence != null){
			String delims = "[ .,?!]+";
			Arrays.asList(sentence.split(delims)).parallelStream().forEach(word -> collector.emit(new Values(word)));
		}

		


	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
	{
      

		outputFieldsDeclarer.declare(new Fields("words"));

	}

}