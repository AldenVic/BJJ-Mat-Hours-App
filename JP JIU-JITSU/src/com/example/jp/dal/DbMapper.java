package com.example.jp.dal;

import android.content.Context;
import android.content.res.AssetManager;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;

public class DbMapper {
    private AWSCredentials credentials;
    private AmazonDynamoDBClient client; 
    private DynamoDBMapper mapper;
    
	public DbMapper(Context myContext)
	{
		try{
			AssetManager a = myContext.getAssets();
	    	credentials = new PropertiesCredentials(a.open("AwsCredentials.properties"));
	    	client = new AmazonDynamoDBClient(credentials);
	    	client.setEndpoint("https://dynamodb.us-west-1.amazonaws.com");
	    	mapper = new DynamoDBMapper(client);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
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
