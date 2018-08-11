package gov.cdc.ncezid.eip.kafka;

import java.util.Timer;

public class ThreadedApp {

	public static void main(String[] args) {
		
		S3Producer producer = new S3Producer( "localhost:9092", "eip-s3-incoming", "errorTopic","AKIAIHX6U5QGJDZGPVIA" , "Oq5mV5tmwo6KMg9AUKKbWRd0JO6yQSX4rNBFfu3g", "eip-plus-messages-dev" , "outgoing/", "processed/");
		
		Timer timer = new Timer();
		timer.schedule(producer,0, 5000l);
		
		
	}

}
