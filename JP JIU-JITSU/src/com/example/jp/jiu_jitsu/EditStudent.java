package com.example.jp.jiu_jitsu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.amazonaws.services.dynamodb.model.DeleteItemRequest;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.UpdateItemRequest;
import com.example.jp.dal.DbMapper;

public class EditStudent extends Activity {

	String[] student_data=new String[7];
	String first=null,last=null,p=null,mail=null;
	AmazonDynamoDBClient dynamoDB;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);
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
        	rankList.setEnabled(false);
        	stripeList.setEnabled(false);
        	
        	//Set First, Last Names and Last Promo Date
        	EditText firstName = (EditText)findViewById(R.id.editText1);
        	firstName.setText(student_data[0]);
        	
        	EditText lastName = (EditText)findViewById(R.id.editText2);
        	lastName.setText(student_data[1]);
        	
        	EditText email = (EditText)findViewById(R.id.editText3);
        	email.setText(student_data[6]);
        	
        	EditText phone = (EditText)findViewById(R.id.editText4);
        	phone.setText(student_data[5]);
        	
        	TextView lastPromoDate = (TextView)findViewById(R.id.textView11);
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
        getMenuInflater().inflate(R.menu.activity_edit_student, menu);
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
    
    public void updateStudent(View view)
    {
    	//Store all edit text values in global variables
    	dynamoDB =  new DbMapper((Context)this).getClient();
    	first=((EditText)findViewById(R.id.editText1)).getText().toString();
    	last=((EditText)findViewById(R.id.editText2)).getText().toString();
    	mail=((EditText)findViewById(R.id.editText3)).getText().toString();
    	p=((EditText)findViewById(R.id.editText4)).getText().toString();
    	UpdateStudent u=new UpdateStudent();
    	u.execute();
    }
    
    public void removeStudent(View view)
    {
    	dynamoDB =  new DbMapper((Context)this).getClient();
    	
    	AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        
    	confirmDialog.setTitle("Confirm Deletion");

    	confirmDialog
			.setMessage("Click Yes to remove, No to return")
			.setCancelable(false)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
						DeleteStudent d=new DeleteStudent();
				    	d.execute();
				    	backToAdmin();
					}
			  })
			.setNegativeButton("No",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
				}
			});    	
    	AlertDialog confirm = confirmDialog.create();
    	confirm.show();
    }
    
    private void backToAdmin()
    {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        
		alertDialogBuilder.setTitle("Record Deleted");

		alertDialogBuilder
			.setMessage("Deleted "+student_data[0]+" "+student_data[1]+"'s record. Press Ok to go back to home screen")
			.setCancelable(false)
			.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					//Go back to main screen
					startActivity(new Intent(EditStudent.this,AdminActivity.class));
					}
			  });
		
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
    
    private class UpdateStudent extends AsyncTask<Void,Void,Void>{
    	boolean flag=false;
    	@Override
		protected Void doInBackground(Void... params) {
    		Map<String, AttributeValueUpdate> updateItems = new HashMap<String, AttributeValueUpdate>();
            //Map<String, ExpectedAttributeValue> expectedValues = new HashMap<String, ExpectedAttributeValue>(); 
    		Key pk= new Key(new AttributeValue().withS(student_data[2]));
        	try{
	    		if(student_data[0]!=first)
	    		{
	    			flag=true;
	    			updateItems.put("FirstName",  new AttributeValueUpdate()
	                .withAction(AttributeAction.PUT)
	                .withValue(new AttributeValue().withS(first)));
	    		}
	    		
	    		if(student_data[1]!=last)
	    		{
	    			flag=true;
	    			updateItems.put("LastName",  new AttributeValueUpdate()
	                .withAction(AttributeAction.PUT)
	                .withValue(new AttributeValue().withS(last)));
	    		}
	    		
	    		if(student_data[6]!=mail)
	    		{
	    			flag=true;
	    			updateItems.put("EmailAddress",  new AttributeValueUpdate()
	                .withAction(AttributeAction.PUT)
	                .withValue(new AttributeValue().withS(mail)));
	    		}
	  
	    		if(student_data[5]!=p)
	    		{
	    			flag=true;
	    			updateItems.put("PhoneNumber",  new AttributeValueUpdate()
	                .withAction(AttributeAction.PUT)
	                .withValue(new AttributeValue().withS(p)));
	    		}
	    		
	            if(flag)
	            {
		    		UpdateItemRequest updateItemRequest = new UpdateItemRequest()
		                .withTableName("Students")
		                .withKey(pk)
		                .withAttributeUpdates(updateItems);
		            
		            try{
		            dynamoDB.updateItem(updateItemRequest);
		            }catch (ConditionalCheckFailedException cse) {
		                // Reload object and retry code.
		                System.err.println("Conditional check failed in Students table");
		            } catch (AmazonServiceException ase) {
		                System.err.println("Error updating item in Students table");
		            } catch (Exception e){
		            	String m=null;
		            	m=e.getMessage();
		            	System.err.println(m);
		            	e.printStackTrace();
		            }
	            }
        	}catch (Exception e) {
				// TODO: handle exception
        		System.err.println(e.getMessage());
        		e.printStackTrace();
			}
            return null;
    	}
    	
    	@Override
		protected void onPostExecute(Void result) {
    		String message=null;
    		if(flag)
    			message="Updated Student Record";
    		else
    			message="No changes to save!";
    		Toast t=Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        	t.show();
    	}
    }
    
    private class DeleteStudent extends AsyncTask<Void,Void,Void>{
    	@Override
		protected Void doInBackground(Void... params) {
    		Key pk= new Key(new AttributeValue().withS(student_data[2]));
    		
    		DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
	                .withTableName("Students")
	                .withKey(pk);
	            
    		    try{
    		    			dynamoDB.deleteItem(deleteItemRequest);
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
    		
    	}
    }
}
