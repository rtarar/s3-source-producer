set PIPELINE_NAME=EIPPLUS
set GROUP_NAME=S3PRODUCER01
set OUTGOING_TOPIC_NAME=eip-s3-hl7-new3
set ERROR_TOPIC_NAME=eip-error
set KAFKA_BROKERS=sdl-dev01.biotech.cdc.gov:9092
set POLL_INTERVAL_MILLIS=2000
set S3_BUCKET_NAME=eip-plus-messages-dev
set S3_ACCESS_KEY=AKIAIHX6U5QGJDZGPVIA
set S3_SECRET=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
set SQS_URL=https://sqs.us-east-1.amazonaws.com/626636711996/eip-legacy-dev
set SCHEMA_REGISTRY_URL=http://sdl-dev01.biotech.cdc.gov:8081

java -jar target\s3-source-producer-0.0.1-SNAPSHOT-jar-with-dependencies.jar