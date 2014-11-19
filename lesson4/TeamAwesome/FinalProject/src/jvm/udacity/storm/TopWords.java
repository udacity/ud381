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

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

import udacity.storm.spout.RandomSentenceSpout;
import udacity.storm.tools.SentimentAnalyzer;


public class TopWords extends BaseRichBolt
{
	private OutputCollector collector;
	private Map<String, Integer> countNouns;
	private Map<String, Integer> countVerbs;
	private Map<String, Integer> countDO;
	private Map<String, Integer> SentimentDistribution;
	Integer val;
	Integer maxNoun = 0;
	Integer maxVerb = 0;
	Integer maxDO = 0;
	String mNoun = "";
	String mVerb = "";
	String mDO = "";
	double alpha;

	@Override 
	public void prepare(
		Map map,
		TopologyContext topologyContext,
		OutputCollector outputCollector
	)
	{
		collector = outputCollector;
		countNouns = new HashMap<String, Integer>();
		countVerbs = new HashMap<String, Integer>();
		countDO = new HashMap<String, Integer>();
		SentimentDistribution = new HashMap<String, Integer>();
		alpha = 0.2;
	}

	public double getAvg(String county)
	{
		double sum = 0;
		double total_count = 0.001;
		for(int i = -1; i< 2; i++)
		{
			if(SentimentDistribution.containsKey(county+" "+String.valueOf(i)))
			{
				sum = sum + (SentimentDistribution.get(county+" "+String.valueOf(i))*(i+1.0)/3.0);
				total_count = total_count + SentimentDistribution.get(county+" "+String.valueOf(i));
			}
		}
		return sum/total_count;
	}
	
	public void execute(Tuple tuple)
	{
		
		String tweet = tuple.getStringByField("original-tweet");
		String tweetWord = tuple.getStringByField("original-tweet");
		String noun = tuple.getStringByField("noun");
		String verb = tuple.getStringByField("verb");
		String dO = tuple.getStringByField("object");
		String county = (String) tuple.getStringByField("county_id");
		String url = tuple.getStringByField("url");
		String matchedEmoticon = tuple.getStringByField("matchedEmoticon");
		int matchedEmoticonScore = tuple.getIntegerByField("matchedEmoticonScore");
		int sentiment = tuple.getIntegerByField("sentiment");
		String sentimentKey = county + " " + String.valueOf(sentiment);
		double reportSentiment = 0.5;
		//SentimentAnalyzer.findSentiment(tweet);
		
		
		System.out.println("\t\tTopWords\tDEBUG Read Values: " + noun + ", " + verb + ", " + dO);
		
		
		if (SentimentDistribution.get(sentimentKey) == null){
			SentimentDistribution.put(sentimentKey,0);
		}
		SentimentDistribution.put(sentimentKey, SentimentDistribution.get(sentimentKey) + 1);
		
		if(matchedEmoticonScore == 0){
			reportSentiment = Math.max(Math.min(1.0, getAvg(county)*3), 0.0);
		}
		else
		{
			reportSentiment = (matchedEmoticonScore-1)/4.0;
		}
		
		System.out.println("\t\tTopWords\tDEBUG Read Values: " + noun + ", " + verb + ", " + dO + ", reportSentiment: " + String.valueOf(reportSentiment) + ", URL : " + url);
		
		
		/*
		if (countNouns.get(noun) == null){
			countNouns.put(noun,1);
			mNoun = noun;
		}else{
			val = countNouns.get(noun) + 1;
			if (val > maxNoun && noun.length() > 0) {
				maxNoun = val;
				mNoun = noun;
			}
			countNouns.put(noun,val);

		}
		if (countVerbs.get(verb) == null){
			countVerbs.put(verb,1);
			mVerb = verb;
		}else{
			val = countVerbs.get(verb)+1;
			if (val > maxVerb && verb.length() > 0){
				maxVerb = val;
				mVerb = verb;
			}
			countVerbs.put(verb,val);
		}
		if (countDO.get(dO) == null){
			countDO.put(dO, 1);
			mDO = dO;
		}else{
			val = countDO.get(dO)+1;
			if (val > maxDO && dO.length() > 0){
				maxDO = val;
				mDO = dO;
			}
			countDO.put(dO,val);
		}
		 */
		System.out.println("\t\tTopWords\tDEBUG EMIT Tweet " + tweetWord + ", matcedEmoticon: " + matchedEmoticon + ", sentimentKey: " + sentimentKey + ", reportSentiment: " + reportSentiment);
		collector.emit(new Values(tweet, tweetWord, mNoun, mVerb, mDO, county, url, matchedEmoticonScore, matchedEmoticon, reportSentiment));

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
	{
		
		
		outputFieldsDeclarer.declare(
				new Fields("tweet", "tweetWord", "noun", "verb" , "do", "county_id", "url", "matchedEmoticonScore", "matchedEmoticon", "sentiment"));
	}

}
