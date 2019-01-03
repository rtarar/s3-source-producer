package gov.cdc.ncezid.eip.kafka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;



import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.jayway.jsonpath.JsonPath;

import gov.cdc.ncezid.eip.kafka.exception.WorkerException;
import gov.cdc.ncezid.eip.kafka.helper.ResourceHelper;

public class S3Producer extends TimerTask{
	
	private static final Logger logger = LoggerFactory.getLogger(S3Producer.class);

	protected final String outgoingTopicName;
	protected final String errorTopicName;
	protected final String kafkaBrokers;
	protected final String s3AccessKey;
	protected final String s3Secret;
	protected final String s3BucketName;
	protected final String s3SourcePrefix;
	protected final String s3ProcessedPrefix;
	protected final AmazonS3 s3client;
	protected final AmazonSQS sqsClient;
	protected final String sqsUrl;
	protected final String pollIntervalMillis;
	private KafkaProducer<String,String> producer;
	protected  Properties props;

	
    
	
	public S3Producer() {
		throw new IllegalArgumentException("This constructor should not be used.");
	}
	
	public S3Producer(String kafkaBrokers, String outgoingTopicName, String errorTopicName,String s3accessKey , String s3Secret, String s3BucketName , String s3Sourceprefix, String s3ProcessedPrefix, String pollIntervalMillis, String sqsUrl)  throws Exception{
		
		this.kafkaBrokers = kafkaBrokers;
        this.outgoingTopicName = outgoingTopicName;
		this.errorTopicName = errorTopicName;
		
		this.s3AccessKey = s3accessKey;
		this.s3Secret = s3Secret;
		this.s3BucketName = s3BucketName;
		this.s3ProcessedPrefix = s3ProcessedPrefix;
		this.s3SourcePrefix = s3Sourceprefix;
		this.pollIntervalMillis = pollIntervalMillis;
		this.sqsUrl = sqsUrl;
		 
		AWSCredentials credentials = new BasicAWSCredentials(
	      		  s3AccessKey, 
	      		  s3Secret
	      		); 
	      
	    this.s3client = AmazonS3ClientBuilder
	      		  .standard()
	      		  .withCredentials(new AWSStaticCredentialsProvider(credentials))
	      		  .withRegion(Regions.US_EAST_1)
	      		  .build();
	    
	    this.sqsClient = AmazonSQSClientBuilder
	    					.standard()
	    					.withCredentials(new AWSStaticCredentialsProvider(credentials))
	    					.withRegion(Regions.US_EAST_1)
	    					.build();
	    					
	    
	    
		try {
		    String clientID = ResourceHelper.getProperty(ProducerConfig.CLIENT_ID_CONFIG);
		    String acks = ResourceHelper.getProperty(ProducerConfig.ACKS_CONFIG);
		    String keySer = ResourceHelper.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
		    String valSer = ResourceHelper.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
		    String retries = ResourceHelper.getProperty(ProducerConfig.RETRIES_CONFIG);
		    String linger = ResourceHelper.getProperty(ProducerConfig.LINGER_MS_CONFIG);
			
			Properties props = new Properties();
	        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaBrokers);
	        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientID);
	        //props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "mguid123");
	        props.put(ProducerConfig.ACKS_CONFIG, acks);
	        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySer);
	        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valSer);
	        props.put(ProducerConfig.RETRIES_CONFIG, retries);
	        props.put(ProducerConfig.LINGER_MS_CONFIG, linger);
            this.props = props;
		}catch(IOException ioe) {
			//abort cant start without this
			throw new WorkerException("Could not load or find Producer config entries"+ioe.getMessage());
		}
		
		Thread.currentThread().setContextClassLoader(null);//make sure that classloader loads all Kafka Libs
		
		this.producer = new KafkaProducer<String,String>(props);
	}

	public void run() {
		logger.debug("Producer Waking up ..");
		System.out.println("Producer Waking up ...");
		try {
			//get messages from S3
			//getDataFromS3();
			
			//check SQS for messages
			getSQSMessages();
		} catch (WakeupException e) {
			// ignore for shutdown
			logger.error("Error in producing records.",e);
		} finally {
			
		}
		
	}
	
	
	private void getSQSMessages() {
		List<Message> messages = sqsClient.receiveMessage(sqsUrl).getMessages();
		
		// delete messages from the queue
        for (Message m : messages) {
        	logger.debug("Got message .."+m.getMessageId());
        	produceData(m);
        }
	}
	
	private String getKeyMguid(String body){
		String value = readStringJSON(body, "$..Records[0].s3.object.key");
		String key = value.substring(value.indexOf("/")+1, value.length());
		return key;
	}
	
	
	private void produceData(Message m) {
		String key = getKeyMguid(m.getBody());
		S3Object s3Object = s3client.getObject(new GetObjectRequest(s3BucketName,key));
    	BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
        String s3Data = reader.lines().collect(Collectors.joining("\n")); 
        final  String handle = m.getReceiptHandle();
      
          final long time = System.currentTimeMillis();
            try {
            	  final ProducerRecord<String, String> record = new ProducerRecord<String, String>(outgoingTopicName,key,s3Data);
            	  producer.send(record , new org.apache.kafka.clients.producer.Callback() {
				  public void onCompletion(RecordMetadata metadata, Exception exception) {
						
				  long elapsedTime = System.currentTimeMillis() - time;
		          if (metadata != null) {
		               		String message = String.format("sent record(key=%s valuelength=%s) " +
		                                    "meta(partition=%d, offset=%d) time=%d\n",
		                            record.key(), record.value().length(), metadata.partition(),
		                            metadata.offset(), elapsedTime);
		                	logger.info(message);
		                	//delete message from queue
		                	DeleteMessageResult r = sqsClient.deleteMessage(sqsUrl, handle);
		                	logger.info(r.getSdkResponseMetadata().getRequestId());
		                } else {
		                    exception.printStackTrace();
		                }
					}
				}); 
                  
            }catch(Exception e) {
            	e.printStackTrace();
            }finally {
            	//producer.close();
            }
      	}


	
	
	private String readStringJSON(String obj , String path) {
		String value = "";
		if(obj!=null) {
		
			Object objVal = JsonPath.read(obj, path);
			if (objVal instanceof net.minidev.json.JSONArray) {
				net.minidev.json.JSONArray objValArr = (net.minidev.json.JSONArray)objVal;
				for(int i=0;i<objValArr.size();i++) {
					value += objValArr.get(i).toString(); 
				}
			}
			
		} 
		return value;
	}

	

	
	
}
