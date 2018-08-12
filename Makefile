docker-build:
	make -i docker-stop 2>/dev/null
	(docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)) || true 
	mvn clean package
	docker build \
	  -t s3-source-producer \
	  --rm \
	  --build-arg PIPELINE_NAME=EIPPLUS \
	  --build-arg GROUP_NAME=S3PRODUCER \
	  --build-arg OUTGOING_TOPIC_NAME=eip-s3-incoming \
	  --build-arg ERROR_TOPIC_NAME=eip-error \
	  --build-arg KAFKA_BROKERS=localhost:9092 \
	  --build-arg INDEXING_URL=http://localhost:8084 \
	  --build-arg STORAGE_URL=http://localhost:8083 \
	  --build-arg POLL_INTERVAL_MILLIS=2000 \
	  --build-arg S3_BUCKET_NAME=eip-plus-messages-dev \
	  --build-arg S3_ACCESS_KEY=AKIAIHX6U5QGJDZGPVIA \
	  --build-arg S3_SECRET=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 \
	  --build-arg S3_INCOMING_PREFIX=outgoing/ \
	  --build-arg S3_PROCESSED_PREFIX=processed/ \
	  .

docker-start:
	 docker run -d \
		 -p 8083:8083 \
		 --network=s3sourceproducer_default  \
		 --name=s3sourceproducer_main \
		 s3-source-producer

docker-stop:
	docker stop s3sourceproducer_main || true
	docker rm s3sourceproducer_main || true


		 