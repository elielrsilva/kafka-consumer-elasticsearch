# Kafka + Elasticsearch

This is a small project that I build while I was studying Apache Kafka. In this README.MD, I have listed some basic concepts used to build this example. This project makes use of Twitter Stream API as a producer, Apache Kafka an event store and Elasticsearch as a consumer.


## Dependencies

[Apache kafka versão 2.4.0](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.4.0/kafka_2.11-2.4.0.tgz)

[Elasticsearch 7.5.2](https://www.elastic.co/pt/downloads/past-releases/elasticsearch-7-5-2)

[Kibana 7.5.2](https://www.elastic.co/pt/downloads/past-releases/kibana-7-5-2)

[Twitter Stream API](https://developer.twitter.com/en/docs/tutorials/consuming-streaming-data)

## What is Apache (very resumed)

Apache Kafka is a distributed stream processing platform. In other words, this platform allows you to get data
from a source (a Rest API, a sensor, ...), to process it and send it to a consumer system. Apache Kafka is known as a distributed stream processing platform because of the capabilities described below:

-  It allows publish and subscribe to streams of records, similar to a message queue or enterprise messaging system.

- Store streams of records in a fault-tolerant durable way.

- Process streams of records as they occur.

To make easier to understand how does Apache Kafka works, I will resume it in this way:

1) A message is **produced** in a data source and it is sent through a **Kafka Producer**. In this project, the Data Source is the Twitter Stream API and the [TwitterProducer.java](src\main\java\com\github\elielodeveloper\producer\TwitterProducer.java)

2) This message will be stored logically in a **Kafka Topic**.

3) So finally, a **Kafka consumer** consumes the message.

![source - https://medium.com/@gabrielqueiroz/o-que-%C3%A9-esse-tal-de-apache-kafka-a8f447cac028](https://miro.medium.com/max/2970/1*q2jYvDNJMS72HgWsOG1f8g.png)

I strongly recommend to read these 2 other links to have a better understanding of Apache Kafka's theory:

- https://medium.com/@gabrielqueiroz/o-que-%C3%A9-esse-tal-de-apache-kafka-a8f447cac028
- https://kafka.apache.org/intro

## How to run the project?