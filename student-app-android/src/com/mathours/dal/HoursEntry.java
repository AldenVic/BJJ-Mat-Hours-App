package com.mathours.dal;
import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="MatHours")
public class HoursEntry {
	private String barcode;
	private int numHours;
	private String reason;
	private Date timestamp;
	
	@DynamoDBHashKey(attributeName="Barcode")	
	public String getBarcode()
	{
		return barcode;
	}
	public void setBarcode(String value)
	{
		barcode = value;
	}
	
	@DynamoDBRangeKey(attributeName="Timestamp")	
	public Date getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(Date value)
	{
		timestamp = value;
	}
	
	@DynamoDBAttribute(attributeName="NumHours")
	public int getNumHours()
	{
		return numHours;
	}
	public void setNumHours(int value)
	{
		numHours = value;
	}
	
	@DynamoDBAttribute(attributeName="Reason")
	public String getReason()
	{
		return reason;
	}
	public void setReason(String value)
	{
		reason = value;
	}
	

	
}
