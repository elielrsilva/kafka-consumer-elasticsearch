package com.github.elielodeveloper.producer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterProducer {
	
	Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());
	
	private String consumerKey="";
	private String consumerSecret= "";
	private String token = "";
	private String secret = "";
	
	public TwitterProducer(){}
	
	public static void main(String[] args){
		new TwitterProducer().run();
	}
	public void run(){

		logger.info("Steup");
		
		//Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);
		
		//create a twitter client
		Client client = createTwitterClient(msgQueue);
		client.connect();
		
		// create a kafka producer
		KafkaProducer<String, String> producer = createKafkaProducer();
		
		// on a different thread, or multiple different threads....
		while (!client.isDone()) {
			String msg = null;
		try {
			msg = msgQueue.poll(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Client Timeout exception");
			client.stop();
		}
		  if(msg != null)
			logger.info(msg);
		  	producer.send(new ProducerRecord<>("twitter_tweets_1", null, msg), new Callback() {

				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if(exception != null) {
						logger.error("error while seding msg to kafka broker", exception);
					}					
				}
		  	});
		  
		}
		logger.info("End of application");
	}
	
	public Client createTwitterClient(BlockingQueue<String> msgQueue){
		
		/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
		// Optional: set up some followings and track terms
		List<String> terms = Lists.newArrayList("#norliv");
		hosebirdEndpoint.trackTerms(terms);

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);
	
		ClientBuilder builder = new ClientBuilder()
				  .name("Hosebird-Client-01")                              // optional: mainly for the logs
				  .hosts(hosebirdHosts)
				  .authentication(hosebirdAuth)
				  .endpoint(hosebirdEndpoint)
				  .processor(new StringDelimitedProcessor(msgQueue)); // optional: use this if you want to process client events

				Client hosebirdClient = builder.build();
				// Attempts to establish a connection.
				return hosebirdClient;
	}
	
	public KafkaProducer<String, String> createKafkaProducer(){
		String bootstrapServers = "127.0.0.1:9092";
		
		//Create Producer properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		// Create the Producer
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
		return producer;
	}

}
