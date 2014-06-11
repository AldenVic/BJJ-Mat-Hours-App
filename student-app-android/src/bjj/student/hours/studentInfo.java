package bjj.student.hours;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.mathours.dal.DbMapper;
import com.mathours.dal.Rank;
import com.mathours.dal.Student;

public class studentInfo {
	private String[] belts = new String[] { "White", "Blue", "Purple", "Brown", "Black" };

	public JSONObject getStudents(){
		
		List<Student> studentRecords = new ArrayList<Student>();
		JSONObject json = new JSONObject();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
		DbMapper db;
		
		try {
			db = new DbMapper();
			DynamoDBMapper dyMapper = db.getMapper();
			
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			
			Map<String, Condition> scanFilter = new HashMap<String, Condition>();
			
			Condition scanCondition = new Condition()
			    .withComparisonOperator(ComparisonOperator.NOT_NULL);
			
			scanFilter.put("Barcode", scanCondition);        
			scanExpression.setScanFilter(scanFilter);
			
			studentRecords = dyMapper.scan(Student.class, scanExpression);
			
			JSONArray vals = new JSONArray();
	        for (int i = 0; i < studentRecords.size(); i++) {
	        	JSONObject obj = new JSONObject();
	        	int r = studentRecords.get(i).getCurrentRank();
	        	int beltIndex=r/10;
	        	int stripeIndex=r%10;
	        	obj.put("belt", belts[beltIndex]);
	        	obj.put("stripes", stripeIndex);
	        	obj.put("name", studentRecords.get(i).getLastName() + ", " + studentRecords.get(i).getFirstName());
	        	obj.put("barcode", studentRecords.get(i).getBarcode());
	        	obj.put("email", studentRecords.get(i).getEmailAddress());
	        	obj.put("phone", studentRecords.get(i).getPhoneNumber());
	        	obj.put("last_promotion", df.format(studentRecords.get(i).getLastPromotion()));
	        	vals.put(obj);
	        }
	        json.put("d", vals);
			
			return json;
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public JSONObject getMatHoursLog(String barcode, String daysSpecified){

		DbMapper db;
		JSONObject json = new JSONObject();
		String startDateStr, endDateStr;	
		SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
		SimpleDateFormat of = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
		
		Calendar today = Calendar.getInstance();
		today.setTime(new Date());
		startDateStr = df.format(today.getTime());
		
		int days = Integer.parseInt(daysSpecified);
		if(days == 0)
		{
			//Subtract ~100 years. Hopefully we have more useful technology by then.
			today.add(Calendar.DATE, -36524);
		}
		else
		{
			today.add(Calendar.DATE, -days);
		}		
		
		endDateStr = df.format(today.getTime());
		
		try {
			db = new DbMapper();
			Condition rangeKeyCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.BETWEEN.toString())
				.withAttributeValueList(new AttributeValue().withS(endDateStr),
						new AttributeValue().withS(startDateStr));
						
			QueryRequest queryRequest = new QueryRequest().withTableName("MatHours")
					.withHashKeyValue(new AttributeValue().withS(barcode))
					.withRangeKeyCondition(rangeKeyCondition)
					.withAttributesToGet(Arrays.asList("Timestamp", "Reason", "NumHours"));
			
			QueryResult result = db.getClient().query(queryRequest);
			JSONArray vals = new JSONArray();
	        for (int i = 0; i < result.getCount(); i++) {
	        	Map<String, AttributeValue> item = result.getItems().get(i);
	        	JSONObject obj = new JSONObject();
	        	Date d = df.parse(item.get("Timestamp").getS());
	        	obj.put("timestamp", of.format(d));
	        	obj.put("reason", item.get("Reason").getS());
	        	obj.put("num_hours", item.get("NumHours").getN());
	        	vals.put(obj);
	        }
	        json.put("d", vals);
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	
	public JSONObject getMatHoursProgress(String barcode){
	
		float sum = 0, requiredHours = 0;
		JSONObject json = new JSONObject();
		Student s = new Student();
		Date lastRankPromotion = new Date();
		String belt;
		int stripes;
		String startDateStr, endDateStr;
		DbMapper db;
		
		
		try {
			
			db = new DbMapper();
			
			s = getStudentInfoByBarcode(barcode);
			belt = belts[(s.getCurrentRank() / 10)];
			stripes = s.getCurrentRank() % 10;
			requiredHours = getRequiredHours(belt, stripes);
			lastRankPromotion = s.getLastPromotion();
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
			
			Calendar today = Calendar.getInstance();
			today.setTime(new Date());
			startDateStr = df.format(today.getTime());
		
			endDateStr = df.format(lastRankPromotion.getTime());
			
			Condition rangeKeyCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.BETWEEN.toString())
				.withAttributeValueList(new AttributeValue().withS(endDateStr),
						new AttributeValue().withS(startDateStr));
						
			QueryRequest queryRequest = new QueryRequest().withTableName("MatHours")
					.withHashKeyValue(new AttributeValue().withS(barcode))
					.withRangeKeyCondition(rangeKeyCondition)
					.withAttributesToGet(Arrays.asList("NumHours"));
			
			QueryResult result = db.getClient().query(queryRequest);
			
			System.out.println(result.getItems().size());
			
	        for (Map<String, AttributeValue> item : result.getItems()) {
	            sum += Float.parseFloat(item.get("NumHours").getN());	        
	        }
	        
	        json.put("current_hours", sum);
	        json.put("hours", requiredHours);
	        json.put("current_belt", belt);
	        
	        return json;
	    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private float getRequiredHours(String belt, int stripes) {
		try
		{
			DbMapper db = new DbMapper();
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			Map<String, Condition> scanFilter = new HashMap<String, Condition>();
			Condition scanCondition = new Condition()
			    .withComparisonOperator(ComparisonOperator.NOT_NULL);
			scanFilter = new HashMap<String, Condition>();
			scanFilter.put("RankID", scanCondition);
			scanExpression.setScanFilter(scanFilter);
			List<Rank> rankList = db.getMapper().scan(Rank.class, scanExpression);
			for(int i = 0; i < rankList.size(); i++)
			{
				if(rankList.get(i).getBelt().equals(belt) && rankList.get(i).getStripe() == stripes) 
				{
					return rankList.get(i).getRequiredHours();
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}
	private Student getStudentInfoByBarcode(String bCodeStr){
		Student s = new Student();
		DbMapper db;
		
		try {
			db = new DbMapper();
			s = db.getMapper().load(Student.class, bCodeStr);
			return s;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
