package com.mathours.dal;

import java.io.IOException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;

public class DbMapper {
    private AWSCredentials credentials;
    private AmazonDynamoDBClient client; 
    private DynamoDBMapper mapper;
    
	public DbMapper() throws IOException
	{
    	credentials = new PropertiesCredentials(DbMapper.class.getResourceAsStream("AwsCredentials.properties"));
    	client = new AmazonDynamoDBClient(credentials);
    	client.setEndpoint("https://dynamodb.us-west-1.amazonaws.com");
    	mapper = new DynamoDBMapper(client);
	}
	
	public DynamoDBMapper getMapper() 
	{
		return mapper;
	}
	
	public AmazonDynamoDBClient getClient()
	{
		return client;
	}

}
