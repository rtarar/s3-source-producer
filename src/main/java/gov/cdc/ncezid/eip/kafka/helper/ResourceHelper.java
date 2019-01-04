package gov.cdc.ncezid.eip.kafka.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ResourceHelper {

	private static final Logger logger = LoggerFactory.getLogger(ResourceHelper.class);

	private static final String APPLICATION_PROPERTIES_PATH = "/application.properties";
	private static final String CONFIG_PROPERTIES_PATH = "/config.properties";
	private static final String CONFIG_SERVICES_PATH = "/config-services.properties";
	private static Properties properties = null;

	public static final String CONST_ENV_VAR_PIPELINE_NAME = "PIPELINE_NAME";
	public static final String CONST_ENV_VAR_GROUP_NAME = "GROUP_NAME";
	public static final String CONST_ENV_VAR_INCOMING_TOPIC_NAME = "INCOMING_TOPIC_NAME";
	public static final String CONST_ENV_VAR_OUTGOING_TOPIC_NAME = "OUTGOING_TOPIC_NAME";
	public static final String CONST_ENV_VAR_ERROR_TOPIC_NAME = "ERROR_TOPIC_NAME";
	public static final String CONST_ENV_VAR_KAFKA_BROKERS = "KAFKA_BROKERS";
	public static final String CONST_ENV_VAR_S3_BUCKET_NAME = "S3_BUCKET_NAME";
	public static final String CONST_ENV_VAR_SCHEMA_REGISTRY_URL = "SCHEMA_REGISTRY_URL";
	public static final String CONST_ENV_VAR_S3_ACCESS_KEY = "S3_ACCESS_KEY";
	public static final String CONST_ENV_VAR_S3_SECRET = "S3_SECRET";
	public static final String CONST_ENV_VAR_STORAGE_URL = "STORAGE_URL";
	public static final String CONST_ENV_VAR_INDEXING_URL = "INDEXING_URL";
	public static final String  CONST_ENV_VAR_PRODUCER_CLIENT_ID = "CLIENT_ID_CONFIG";
	public static final String  CONST_ENV_VAR_POLL_INTERVAL_MILLIS = "POLL_INTERVAL_MILLIS";
	public static final String  CONST_ENV_VAR_SQS_URL="SQS_URL"; 
	
	private ResourceHelper() {
		throw new IllegalAccessError("Helper class");
	}

	public static Properties getProperties() throws IOException {
		if (properties == null) {
			properties = new Properties();
			InputStream is = ResourceHelper.class.getResourceAsStream(APPLICATION_PROPERTIES_PATH);
			if (is != null)
				properties.load(is);
			is = ResourceHelper.class.getResourceAsStream(CONFIG_PROPERTIES_PATH);
			if (is != null)
				properties.load(is);
			is = ResourceHelper.class.getResourceAsStream(CONFIG_SERVICES_PATH);
			if (is != null)
				properties.load(is);
		}
		return properties;
	}

	public static Properties getProperties(String path) throws IOException {
		Properties properties = new Properties();
		properties.load(ResourceHelper.class.getResourceAsStream(path));
		return properties;
	}

	public static Map<String, String> getPropertyMap(String path) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			Properties properties = getProperties(path);
			for (Object key : properties.keySet()) {
				map.put((String) key, properties.getProperty((String) key));
			}
		} catch (IOException e) {
			logger.error("Error in reading properties",e);
		}
		return map;
	}

	public static String getProperty(String propertyName) throws IOException {
		return getProperties().getProperty(propertyName);
	}

	public static String getSysEnvProperty(String propertyName, boolean required) throws Exception {
		String value = System.getProperty(propertyName);
		if (value == null || value.isEmpty())
			value = System.getenv().get(propertyName);
			
		logger.debug("Env Variable: " + propertyName + " = " + value);
		if (required && StringUtils.isEmpty(value)) {
			throw new Exception("The environment variable `" + propertyName + "` is empty.");
		}
		return value;
	}


}
