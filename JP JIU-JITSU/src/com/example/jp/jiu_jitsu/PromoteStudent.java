package com.example.jp.jiu_jitsu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeAction;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodb.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.UpdateItemRequest;
import com.example.jp.dal.DbMapper;

public class PromoteStudent extends Activity {

	AmazonDynamoDBClient dynamoDB;
	int newRank=-1;
	String[] student_data=new String[7];
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote_student);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras=getIntent().getExtras();
        if(extras!=null)
        {
        	//Fill the Activity with student details
        	
        	//Load both the Lists - Rank and Stripes
            Spinner rankList = (Spinner)findViewById(R.id.spinner1);
            List<String> rank= new ArrayList<String>(Arrays.asList("White","Blue","Purple","Brown","Black","Black and Red","Red"));
            rankList.setAdapter((SpinnerAdapter) new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,rank));
            Spinner stripeList = (Spinner)findViewById(R.id.spinner2);
            List<String> stripes= new ArrayList<String>(Arrays.asList("1","2","3","4"));
            stripeList.setAdapter((SpinnerAdapter) new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,stripes));
        	student_data=extras.getStringArray("student_data");
            
        	//Set the Belt and Stripe in Spinners
        	int r=Integer.parseInt(student_data[3]);
        	int beltIndex=r/10;
        	int stripeIndex=r%10;
        	rankList.setSelection(beltIndex);
        	stripeList.setSelection(stripeIndex);
        	
        	//Set First, Last Names and Last Promo Date
        	TextView firstName = (TextView)findViewById(R.id.textView3);
        	TextView lastPromoDate = (TextView)findViewById(R.id.textView11);
        	
        	firstName.setText(student_data[0]+" "+student_data[1]);
        	Date lastPromo = new Date(Long.valueOf(student_data[4]));
        	SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
        	lastPromoDate.setText(formatter.format(lastPromo));
        }
        else
        {
        	Toast t=Toast.makeText(getApplicationContext(), "Something is not right! Please go back to home screen and Try Again!!", Toast.LENGTH_LONG);
        	t.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_promote_student, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void backToAdmin(View view)
    {
    	//Go back to student selection activity
    	startActivity(new Intent(PromoteStudent.this,AdminActivity.class));
    }

    public void updatePromotion(View view)
    {
    	//If the rank value selected from spinner is different then update DB
    	int beltIndex=((Spinner)findViewById(R.id.spinner1)).getSelectedItemPosition();
    	int stripeIndex=((Spinner)findViewById(R.id.spinner2)).getSelectedItemPosition();
    	
    	newRank=(beltIndex*10)+stripeIndex;
    	int oldRank=Integer.parseInt(student_data[3]);
    	
    	//Push to DB only if both ranks are different
    	if(newRank!=oldRank)
    	{
    		dynamoDB =  new DbMapper((Context)this).getClient();
    		UpdateRank u=new UpdateRank();
    		u.execute();
    	}
    }
    
    private class UpdateRank extends AsyncTask<Void,Void,Void>{
    	@Override
		protected Void doInBackground(Void... params) {
    		Map<String, AttributeValueUpdate> updateItems = new HashMap<String, AttributeValueUpdate>();
            //Map<String, ExpectedAttributeValue> expectedValues = new HashMap<String, ExpectedAttributeValue>(); 
    		Key pk= new Key(new AttributeValue().withS(student_data[2]));
        	
    		updateItems.put("CurrentRank",  new AttributeValueUpdate()
            .withAction(AttributeAction.PUT)
            .withValue(new AttributeValue().withN(String.valueOf(newRank))));
    		
    		//Last promotion date would be the current time when adding a student
        	Date sqlDate =  new java.util.Date(System.currentTimeMillis());
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    		updateItems.put("LastPromotion",  new AttributeValueUpdate()
            .withAction(AttributeAction.PUT)
            .withValue(new AttributeValue().withS(formatter.format(sqlDate))));
    		
            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                .withTableName("Students")
                .withKey(pk)
                .withAttributeUpdates(updateItems);
            
            formatter = new SimpleDateFormat("dd/MM/yyyy");
            try{
            dynamoDB.updateItem(updateItemRequest);
            student_data[4]=formatter.format(sqlDate);
            }catch (ConditionalCheckFailedException cse) {
                // Reload object and retry code.
                System.err.println("Conditional check failed in Students table");
            } catch (AmazonServiceException ase) {
                System.err.println("Error updating item in Students table");
            }
            return null;
    	}
    	
    	@Override
		protected void onPostExecute(Void result) {
    		TextView lastPromoDate = (TextView)findViewById(R.id.textView11);
    		lastPromoDate.setText(student_data[4]);
    		Toast t=Toast.makeText(getApplicationContext(), "Updated database", Toast.LENGTH_LONG);
        	t.show();
    	}
		
    }
}
