export PIPELINE_NAME=EIPPLUS
export GROUP_NAME=S3PRODUCER
export OUTGOING_TOPIC_NAME=eip-s3-hl7
export ERROR_TOPIC_NAME=eip-error
export KAFKA_BROKERS=sdl-dev01.biotech.cdc.gov:9092
export POLL_INTERVAL_MILLIS=2000
export S3_BUCKET_NAME=eip-plus-messages-dev
export S3_ACCESS_KEY=AKIAIHX6U5QGJDZGPVIA
export S3_SECRET=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
export SQS_URL=https://sqs.us-east-1.amazonaws.com/626636711996/eip-legacy-dev
export SCHEMA_REGISTRY_URL=http://sdl-dev01.biotech.cdc.gov:8081

java -jar target/s3-source-producer-*-jar-with-dependencies.jar
