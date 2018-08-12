# S3 Source Producer

This is an S3 Source producer that reads messages from an S3 bucket and pumps them to a KafkaTopic.
If the message send to Kafka is successful it also "moves" the file from the "incoming" s3 folder to processed folder.

In order to run this you will have to do two things
- Start the Kafka infrastructure
- Run the producer

## Start the Infrastructure

Use this with the following infrastructure command using the docker-compose.yml in root of this project
The docker compose file for this project contains a single node Kafka Cluster.
- Latest Kafka Confluent Image
- Latest Zookeeper Confluent Image
- Latest Confluent Schema Registry Image
- Latest Confluent Schema Rest Proxy Image

```
docker compose up

```

## Start the Producer
The producer is configured to run as a Java App. It is compiled with all dependecies in a fat jar (Ref: POM)
In order for the producer to run it needs configurations. Below are the list of configurations at the moment.
```
	PIPELINE_NAME=EIPPLUS
	GROUP_NAME=S3PRODUCER
	OUTGOING_TOPIC_NAME=eip-s3-incoming
	ERROR_TOPIC_NAME=eip-error
	KAFKA_BROKERS=localhost:9092
	INDEXING_URL=http://localhost:8084
	STORAGE_URL=http://localhost:8083
	POLL_INTERVAL_MILLIS=10000
	S3_BUCKET_NAME=eip-plus-messages-dev
	S3_ACCESS_KEY=AKIAIHX6U5QGJDZGPVIA
	S3_SECRET=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
	S3_INCOMING_PREFIX=outgoing/
	S3_PROCESSED_PREFIX=processed/

```
I have included a helper bash script to start the producer just run the following to start the producer
```
Note: First time you may have to change the exec permissions on the file below by typing

CHMOD 777 startproducer.sh

./startproducer.sh

```





If you want a UI on to see Kafka Topics etc , use the following command 

```
docker run --rm -it -p 8000:8000 \
               -e "KAFKA_REST_PROXY_URL=http://localhost:8082" \
               -e "PROXY=true" \
               landoop/kafka-topics-ui
```

To create a topic called  "eip-s3-incoming" in your cluster run the following command in your Kafka Container.
In order to do this you will have to do the following
 - Find your container running Kafka by running "docker ps"
 - Bash in to your container using the following commands
 - Run kafka Topic Command as below (Kafka commands is usually installed in usr/bin directory)  
 	 
```
 //list all containers
 docker ps
 
 //To "bash into tour kafka container" The name could be container id from docker ps command
 docker exec -it <container name> /bin/bash
 
 //create a kafka topic
 kafka-topics --zookeeper localhost:2181 --create --topic eip-s3-incoming --partitions 1 --replication-factor 1
 
 ```