package com.github.elielodeveloper.consumers;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;


public class ElasticSearchConsumer {

	@SuppressWarnings("deprecation")
	private static JsonParser jsonParser = new JsonParser(); 
	
	public static RestHighLevelClient createClient() {
		
		RestHighLevelClient client = new RestHighLevelClient(
	        RestClient.builder(
	            new HttpHost("localhost", 9200, "http"),
	            new HttpHost("localhost", 9201, "http")));
		
		return client;
	}
	
	//this method returns a Kafka Consumer
	public static KafkaConsumer<String, String> createConsumer(String topic){
		//settings values
		String bootstrapServers = "127.0.0.1:9092";
		String groupId = "kafka-demo-elasticsearch";
				
		
		//create consumer config
		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		//create consumer
		KafkaConsumer<String, String> consumer =
				new KafkaConsumer <String, String>(properties);
		
		//subscribe consumer to our topic(s)
		consumer.subscribe(Arrays.asList(topic));
		
		return consumer;
				
	}
	
	
	@SuppressWarnings("deprecation")
	private static String extractIdFromTweet(String tweetJson){
		return jsonParser.parse(tweetJson)
				.getAsJsonObject()
				.get("id_str")
				.getAsString();

	}
	
	public static void main(String[] args) throws IOException {
		Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
		RestHighLevelClient client = createClient();
		
		KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets_1");// O tópico em que o Consumer se inscreveu para ler
		
		while(true){
			ConsumerRecords<String, String> records =
				consumer.poll(Duration.ofMillis(100));
			for(ConsumerRecord<String, String> record : records){
				
				if(record.value() != null){
					String id_tweet = extractIdFromTweet(record.value()); // O id que será usado para que o processamento de mensagens seja indepotente
					
					@SuppressWarnings("deprecation")
					IndexRequest indexRequest = new IndexRequest(
							"twitter_teste", "tweets", id_tweet
							).source(record.value(), XContentType.JSON);
					
					IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
					logger.info(indexResponse.getId());	
				}
				
			}
		}
		
		//close the client gracefully
		//client.close();
	}
}
