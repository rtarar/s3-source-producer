package gov.cdc.ncezid.eip.kafka;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.clouddirectory.model.DeleteObjectRequest;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesRequest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;



/**
 * Hello world!
 *
 */
public class App 
{
	 private final static String TOPIC = "eip-s3-incoming";
     private final static String BOOTSTRAP_SERVERS = "localhost:9092";
     private final static Logger logger = Logger.getLogger(App.class);
     private final static String S3_EIP_BUCKET_NAME = "eip-plus-messages-dev";
     private final static String S3_EIP_BUCKET_PREFIX_INCOMING = "outgoing/";
     private final static String S3_EIP_BUCKET_PREFIX_PROCESSED = "processed/";
     private final static String S3_ACCESS_KEY = "AKIAIHX6U5QGJDZGPVIA";
     private final static String S3_SECRET_KEY = "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";
     
     
     
    public static void main( String[] args )
    {
        
        AWSCredentials credentials = new BasicAWSCredentials(
        		  S3_ACCESS_KEY, 
        		  S3_SECRET_KEY
        		); 
        
        AmazonS3 s3client = AmazonS3ClientBuilder
        		  .standard()
        		  .withCredentials(new AWSStaticCredentialsProvider(credentials))
        		  .withRegion(Regions.US_EAST_1)
        		  .build();
               
        ListObjectsRequest lor = new ListObjectsRequest()
                .withBucketName(S3_EIP_BUCKET_NAME)
                .withPrefix(S3_EIP_BUCKET_PREFIX_INCOMING)
                .withDelimiter("/");
        
        ObjectListing objectListing = s3client.listObjects(lor);
        if(objectListing.getObjectSummaries().size()>1) {
        	System.out.println((objectListing.getObjectSummaries().size()-1) +" Files found in S3 Bucket : "+S3_EIP_BUCKET_NAME+" and folder "+S3_EIP_BUCKET_PREFIX_INCOMING+" to process.");
	        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
	        	logger.info(os.getKey());
	        	//System.out.println(os.getKey()+":"+os.getSize()+":"+os.getLastModified()+":"+os.getETag());
	        	S3Object s3Object = s3client.getObject(new GetObjectRequest(S3_EIP_BUCKET_NAME,os.getKey()));
	        	BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
	            String s3Data = reader.lines().collect(Collectors.joining("\n")); 
	          
	            if(os.getKey().contains("json")) {
		           // System.out.println(s3Data);
	            	//its been read lets change the prefix for the object 
	          
		            try {
		            	  runProducer(os.getKey(),s3Data);
		            	  s3FileMove(s3client,s3Object,false);
		            }catch(Exception e) {
		            	e.printStackTrace();
		            }
	        	}
	        }
        }else {
        	System.out.println("No Files found in S3 Bucket : "+S3_EIP_BUCKET_NAME+" and folder "+S3_EIP_BUCKET_PREFIX_INCOMING+" to process , will check back later.");
        }
        
    }
    
    public static void s3FileMove(AmazonS3 s3client, S3Object s3Object , boolean delete) {
    	String oldkey = s3Object.getKey();
    	String destinationKey = oldkey.replace(S3_EIP_BUCKET_PREFIX_INCOMING,S3_EIP_BUCKET_PREFIX_PROCESSED);
    	//System.out.println(destinationKey);
    	CopyObjectRequest cpr = new CopyObjectRequest(s3Object.getBucketName(), s3Object.getKey(), S3_EIP_BUCKET_NAME, destinationKey);
    	s3client.copyObject(cpr);
    	if(delete) {
	    	DeleteObjectsRequest dor = new DeleteObjectsRequest(S3_EIP_BUCKET_NAME)
	    								.withKeys(oldkey);
	    	s3client.deleteObjects(dor);
    	}
    }
    
    private static KafkaProducer<String, String> createProducer() {

        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        //props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "mguid123");
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.RETRIES_CONFIG, "3");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "1");
        return new KafkaProducer<String, String>(props);

    }
    
    static void runProducer(final String key,final String data) throws Exception {

        final KafkaProducer<String, String> producer = createProducer();

        long time = System.currentTimeMillis();
        
       

        try {
        		//producer.initTransactions();
                final ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC, key, data);

                RecordMetadata metadata = producer.send(record).get();
               // producer.commitTransaction();
                long elapsedTime = System.currentTimeMillis() - time;

                System.out.printf("sent record(key=%s valuesize=%s) " + 
                                "meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value().length(), metadata.partition(),
                        metadata.offset(), elapsedTime);
            

        }catch(Exception e ) {
        	e.printStackTrace();
        	//producer.abortTransaction();
        	
        }finally {

            producer.flush();

            producer.close();

        }
 
    
    }
    
    
    
}

