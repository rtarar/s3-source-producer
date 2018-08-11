package gov.cdc.ncezid.eip.kafka;

import java.io.BufferedReader;

import java.io.InputStreamReader;
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
import org.apache.log4j.Logger;

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

public class S3Producer extends TimerTask{
	
	private static final Logger logger = Logger.getLogger(S3Producer.class);

	protected final String outgoingTopicName;
	protected final String errorTopicName;
	protected final String kafkaBrokers;
	protected final String s3AccessKey;
	protected final String s3Secret;
	protected final String s3BucketName;
	protected final String s3SourcePrefix;
	protected final String s3ProcessedPrefix;
	protected final AmazonS3 s3client;
	KafkaProducer<String,String> producer;
	protected  Properties props;

	
    
	
	public S3Producer() {
		throw new IllegalArgumentException("This constructor should not be used.");
	}
	
	public S3Producer(String kafkaBrokers, String outgoingTopicName, String errorTopicName,String s3accessKey , String s3Secret, String s3BucketName , String s3Sourceprefix, String s3ProcessedPrefix) {
		
		this.kafkaBrokers = kafkaBrokers;
        this.outgoingTopicName = outgoingTopicName;
		this.errorTopicName = errorTopicName;
		
		this.s3AccessKey = s3accessKey;
		this.s3Secret = s3Secret;
		this.s3BucketName = s3BucketName;
		this.s3ProcessedPrefix = s3ProcessedPrefix;
		this.s3SourcePrefix = s3Sourceprefix;
		 
		AWSCredentials credentials = new BasicAWSCredentials(
	      		  s3AccessKey, 
	      		  s3Secret
	      		); 
	      
	    this.s3client = AmazonS3ClientBuilder
	      		  .standard()
	      		  .withCredentials(new AWSStaticCredentialsProvider(credentials))
	      		  .withRegion(Regions.US_EAST_1)
	      		  .build();
		
		
		Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaBrokers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        //props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "mguid123");
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.RETRIES_CONFIG, "3");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "1");
        this.props = props;
        
        this.producer = new KafkaProducer<String,String>(props);
		
	}

	public void run() {
		logger.debug("Starting..run.");
		try {
			//get messages from S3
			getDataFromS3();
		} catch (WakeupException e) {
			// ignore for shutdown
		} finally {
			
		}
		
	}
	

	private void getDataFromS3() {
      ListObjectsRequest lor = new ListObjectsRequest()
              .withBucketName(s3BucketName)
              .withPrefix(s3SourcePrefix)
              .withDelimiter("/");
      
      ObjectListing objectListing = s3client.listObjects(lor);
      int numrecords = objectListing.getObjectSummaries().size()-1; //dont count the folder
      
      if(numrecords>0) {
      	System.out.println((numrecords) +" Files found in S3 Bucket : "+s3BucketName+" and folder "+s3SourcePrefix+" to process.");
	        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
	        	
	        	logger.info(os.getKey());
	        	//System.out.println(os.getKey()+":"+os.getSize()+":"+os.getLastModified()+":"+os.getETag());
	        	S3Object s3Object = s3client.getObject(new GetObjectRequest(s3BucketName,os.getKey()));
	        	BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
	            String s3Data = reader.lines().collect(Collectors.joining("\n")); 
	          
	            if(os.getKey().contains("json")) {
		           // System.out.println(s3Data);
	              final long time = System.currentTimeMillis();
	              final CountDownLatch countDownLatch = new CountDownLatch(numrecords);
	            	
		            try {
		            	  final ProducerRecord<String, String> record = new ProducerRecord<String, String>(outgoingTopicName,os.getKey(),s3Data);
		            	  
		            	    
		            	  producer.send(record , new org.apache.kafka.clients.producer.Callback() {
							
							public void onCompletion(RecordMetadata metadata, Exception exception) {
								
								long elapsedTime = System.currentTimeMillis() - time;
				                if (metadata != null) {
				                    System.out.printf("sent record(key=%s value=%s) " +
				                                    "meta(partition=%d, offset=%d) time=%d\n",
				                            record.key(), record.value(), metadata.partition(),
				                            metadata.offset(), elapsedTime);
				                    //success move the s3 file from incoming to processed
				                    s3FileMove(record.key(),true);
				                } else {
				                    exception.printStackTrace();
				                }
				                countDownLatch.countDown();
							}
						}); 
		                  
		            	//  sendMessage(s3Data, outgoingTopicName);
		            	//  s3FileMove(s3client,s3Object,false);
		            }catch(Exception e) {
		            	e.printStackTrace();
		            }finally {
		            	//producer.close();
		            }
	        	}
	        }
      }else {
      	System.out.println("No Files found in S3 Bucket : "+s3BucketName+" and folder "+s3SourcePrefix+" to process , will check back later.");
      }
	}

	private   void s3FileMove(String key , boolean delete) {
    	String oldkey = key;
    	String destinationKey = oldkey.replace(s3SourcePrefix,s3ProcessedPrefix);
    	//System.out.println(destinationKey);
    	CopyObjectRequest cpr = new CopyObjectRequest(s3BucketName, oldkey, s3BucketName, destinationKey);
    	s3client.copyObject(cpr);
    	if(delete) {
	    	DeleteObjectsRequest dor = new DeleteObjectsRequest(s3BucketName)
	    								.withKeys(oldkey);
	    	s3client.deleteObjects(dor);
    	}
    }
	
	
	
}
