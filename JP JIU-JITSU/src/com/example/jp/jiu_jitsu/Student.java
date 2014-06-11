package com.example.jp.jiu_jitsu;

import java.util.Date;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Students")
public class Student  {
	 	
		private String barcode;
		private String firstName;
		private String lastName;
		private String emailAddress;
		private String phoneNumber;
		private int currentRank;
		private Date lastPromotion;
		
		@DynamoDBHashKey(attributeName="Barcode")
		public String getBarcode()
		{
			return barcode;
		}
		public void setBarcode(String value) 
		{
			barcode = value;
		}
		
		@DynamoDBAttribute(attributeName="FirstName")
		public String getFirstName()
		{
			return firstName;
		}
		public void setFirstName(String value) 
		{
			firstName = value;
		}
		
		@DynamoDBAttribute(attributeName="LastName")
		public String getLastName()
		{
			return lastName;
		}
		public void setLastName(String value) 
		{
			lastName = value;
		}
		
		@DynamoDBAttribute(attributeName="EmailAddress")
		public String getEmailAddress()
		{
			return emailAddress;
		}
		public void setEmailAddress(String value) 
		{
			emailAddress = value;
		}
		
		@DynamoDBAttribute(attributeName="PhoneNumber")
		public String getPhoneNumber()
		{
			return phoneNumber;
		}
		public void setPhoneNumber(String value) 
		{
			phoneNumber = value;
		}
		
		@DynamoDBAttribute(attributeName="CurrentRank")
		public int getCurrentRank() 
		{ 
			return currentRank; 
		}
		public void setCurrentRank(int value)
		{ 
			currentRank = value; 
		}

		@DynamoDBAttribute(attributeName="LastPromotion")
		public Date getLastPromotion()
		{
			return lastPromotion;
		}
		public void setLastPromotion(Date value) 
		{
			lastPromotion = value;
		}
		
}
