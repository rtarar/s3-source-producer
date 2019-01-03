package gov.cdc.ncezid.eip.kafka;


import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cdc.ncezid.eip.kafka.helper.ResourceHelper;

public class EIPS3ProducerApp {

	private static final Logger logger = LoggerFactory.getLogger(EIPS3ProducerApp.class);
	
	/**
	 * Set the following ENV vars , or use the startproducer.sh with canned vars
	 * 
	 * This is important as these vars will be injected in openshift configs.
	 * 
	 * PIPELINE_NAME 
	 * GROUP_NAME
	 * INCOMING_TOPIC_NAME
	 * OUTGOING_TOPIC_NAME
	 * ERROR_TOPIC_NAME
	 * KAFKA_BROKERS
	 * S3_BUCKET_NAME
	 * SCHEMA_REGISTRY_URL
	 * S3_ACCESS_KEY
	 * S3_SECRET
	 * S3_INCOMING_PREFIX
	 * S3_PROCESSED_PREFIX
	 * STORAGE_URL
	 * INDEXING_URL
	 * CLIENT_ID_CONFIG
	 * 
	 * 
	 * 
	 */
	
	
	public static void main(String[] args) {
		
		try {

		
		//load environment vars	
		String pipeline = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_PIPELINE_NAME,true);
		String outgoingTopicName = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_OUTGOING_TOPIC_NAME,true);
		String errorTopicName = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_ERROR_TOPIC_NAME,true);
		String kafkaBrokers = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_KAFKA_BROKERS,true);
		String s3AccessKey = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_S3_ACCESS_KEY,true);
		String s3Secret = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_S3_SECRET,true);
		String s3BucketName = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_S3_BUCKET_NAME,true);
		String s3SourcePrefix = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_S3_INCOMING_PREFIX,true);
		String s3ProcessedPrefix= ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_S3_PROCESSED_PREFIX,true);
		String sqsUrl= ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_SQS_URL,true);
		String storageUrl = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_STORAGE_URL,false);
		String pollIntervalMillis = ResourceHelper.getSysEnvProperty(ResourceHelper.CONST_ENV_VAR_POLL_INTERVAL_MILLIS,true);

		
		
		//S3Producer producer = new S3Producer( "localhost:9092", "eip-s3-incoming", "errorTopic","AKIAIHX6U5QGJDZGPVIA" , "Oq5mV5tmwo6KMg9AUKKbWRd0JO6yQSX4rNBFfu3g", "eip-plus-messages-dev" , "outgoing/", "processed/");
		logger.info("Starting producer");
		S3Producer producer = new S3Producer( kafkaBrokers, 
											  outgoingTopicName, 
											  errorTopicName,
											  s3AccessKey , 
											  s3Secret, 
											  s3BucketName , 
											  s3SourcePrefix, 
											  s3ProcessedPrefix,
											  pollIntervalMillis,
											  sqsUrl
												);
		
		
		Timer timer = new Timer();
		timer.schedule(producer,0, Long.parseLong(pollIntervalMillis));
		}catch(Exception e) {
			logger.error("Error in starting Producer",e);
			e.printStackTrace();
			
		}
		
	}

}
