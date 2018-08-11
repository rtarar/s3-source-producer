# S3 Source Producer

This is an S3 Source producer that reads messages from an S3 bucket and pumps them to a KafkaTopic.
If the message send to Kafka is successful it also "moves" the file from the "incoming" s3 filder to processed folder.



Use this with the following infrastructure command using the docket-compose.yml in root of this project

The docker compose file for thsi project containes a single node Kafka Cluster.
- Latest Kafka Confluent Image
- Latest Zookeeper Confluent Image
- Latest Confluent Schema Registry Image
- Latest Confluent Schema Rest Proxy Image


```
docker compose up

```

If you want a UI on to see Kafka Topics etc , use the following command 

```
docker run --rm -it -p 8000:8000 \
               -e "KAFKA_REST_PROXY_URL=http://localhost:8082" \
               -e "PROXY=true" \
               landoop/kafka-topics-ui
```