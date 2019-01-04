# S3 Source Producer

This is an S3 Source producer that reads messages from an SQS Queue and extract the mguid. It then uses the mguid to pull file from S3 bucket and pumps them to a KafkaTopic specified.

If the message send to Kafka is successful it removes the message from the SQS queue.

In order to run this you will have to do two things
- Start the Kafka infrastructure
- Run the producer

## Start the Infrastructure
Note:You dont need to start the infasructure if you are connecting to the Dev Kafka Broker on sdl-dev01.biotech.cdc.gov:9092
 
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
	OUTGOING_TOPIC_NAME=eip-s3-hl7
	ERROR_TOPIC_NAME=eip-error
	KAFKA_BROKERS=sdl-dev01.biotech.cdc.gov:9092
	POLL_INTERVAL_MILLIS=10000
	S3_BUCKET_NAME=eip-plus-messages-dev
	S3_ACCESS_KEY=AKIAIHX6U5QGJDZGPVIA
	S3_SECRET=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
	SQS_URL=https://sqs.us-east-1.amazonaws.com/626636711996/eip-legacy-dev

```
I have included a helper bash script to start the producer just run the following to start the producer
```
Note: First time you may have to change the exec permissions on the file below by typing

CHMOD 777 startproducer.sh

./startproducer.sh
