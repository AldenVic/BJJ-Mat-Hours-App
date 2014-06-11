package com.example.jp.jiu_jitsu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.example.jp.dal.DbMapper;
import com.example.jp.dal.Student;

public class AddStudent extends Activity {
	
	String[] data=new String[6];
	boolean dbStatus=false;
	String failureMessage=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Load both the Lists - Rank and Title
        Spinner rankList = (Spinner)findViewById(R.id.spinner1);
        List<String> rank= new ArrayList<String>(Arrays.asList("White","Blue","Purple","Brown","Black","Black and Red","Red"));
        rankList.setAdapter((SpinnerAdapter) new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,rank));
        rankList.setFocusable(true);
        rankList.setFocusableInTouchMode(true);
        //rankList.requestFocus();
        rankList.requestFocus(View.FOCUS_DOWN);
        Spinner stripeList = (Spinner)findViewById(R.id.spinner2);
        List<String> stripes= new ArrayList<String>(Arrays.asList("1","2","3","4"));
        stripeList.setAdapter((SpinnerAdapter) new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,stripes));
        stripeList.setFocusable(true);
        stripeList.setFocusableInTouchMode(true);
        //stripeList.requestFocus();
        stripeList.requestFocus(View.FOCUS_DOWN);
        EditText firstname=((EditText)findViewById(R.id.editText1));
        firstname.requestFocus();
        EditText phoneText=((EditText)findViewById(R.id.editText4));
        phoneText.setText(" ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_student, menu);
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
    
    public void proceedToBarcode(View view)
    {
    	Integer rankStripe = (((Spinner)findViewById(R.id.spinner1)).getSelectedItemPosition()*10)+((Spinner)findViewById(R.id.spinner2)).getSelectedItemPosition();
    	data[1]=((EditText)findViewById(R.id.editText1)).getText().toString();
    	data[2]=((EditText)findViewById(R.id.editText2)).getText().toString();
    	data[3]=((EditText)findViewById(R.id.editText3)).getText().toString();
    	data[4]=rankStripe.toString();
    	data[5]= ((EditText)findViewById(R.id.editText4)).getText().toString();
    	
    	int flag=-1;
    	
    	for(int i=1;i<4;i++)
    	{
    		if(data[i].equals(null) || data[i].equals(new String("")))
    		{
    			flag=i;
    			break;
    		}
    	}
    	
    	if(flag!=-1)
    	{
    		 Toast toast = Toast.makeText(this, "Please enter Name and Email!", Toast.LENGTH_LONG);
             //toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
             toast.show();
    	}
    	else
    	{
	    	//Proceed to Barcode Scan Activity via Intent
	    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
	        intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
	        startActivityForResult(intent, 0);
    	}
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                TextView codeText = (TextView)findViewById(R.id.textView7);
                codeText.setVisibility(View.VISIBLE);
                TextView scanCode = (TextView)findViewById(R.id.textView8);
                scanCode.setText(contents+format);
                scanCode.setVisibility(View.VISIBLE);
            	
                data[0]=((TextView)findViewById(R.id.textView8)).getText().toString();
            	
            		//Proceed to dB push only if Name and email are null
	            	new Thread(new Runnable(){
	            		public void run(){
	            			addStudentToDB();
	            		}
	            	}
	            	).start();
	            	try
	            	{
	            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	         
	        			alertDialogBuilder.setTitle("Student record added");
	         
	        			alertDialogBuilder
	        				.setMessage("Click Yes to add more, No to return to home screen")
	        				.setCancelable(false)
	        				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
	        					public void onClick(DialogInterface dialog,int id) {
	        						// if this button is clicked, close
	        						// current activity
	        						startActivity(new Intent(AddStudent.this,AddStudent.class));
	        					}
	        				  })
	        				.setNegativeButton("No",new DialogInterface.OnClickListener() {
	        					public void onClick(DialogInterface dialog,int id) {
	        						// if this button is clicked, just close
	        						// the dialog box and do nothing
	        						startActivity(new Intent(AddStudent.this,AdminActivity.class));
	        					}
	        				});
	        			
	    				AlertDialog alertDialog = alertDialogBuilder.create();
	    				alertDialog.show();
	            	}catch(Exception e)
	            	{
	            		e.printStackTrace();
	            	}
            	}
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
            }
    }
    private void addStudentToDB()
    {
    	try{
    	DynamoDBMapper dynamoDB =  new DbMapper((Context)this).getMapper();
    	Student studentObject = new Student();
    	studentObject.setBarcode(data[0]);
    	studentObject.setFirstName(data[1]);
    	studentObject.setLastName(data[2]);
    	studentObject.setEmailAddress(data[3]);
    	studentObject.setCurrentRank(Integer.parseInt(data[4]));
    	studentObject.setPhoneNumber(data[5]);
    	
    	//Last promotion date would be the current time when adding a student
    	Date sqlDate =  new java.util.Date(System.currentTimeMillis());
    	studentObject.setLastPromotion(sqlDate);
    	
    	dynamoDB.save(studentObject);
    	}catch(Exception ex)
    	{
    		failureMessage=ex.getMessage();
    		//((TextView)findViewById(R.id.textView7)).setText(ex.getMessage());
    		//((TextView)findViewById(R.id.textView7)).setVisibility(View.VISIBLE);
    	}
    }
    public void backAdminView(View view)
    {
    	//Start Admin View activity (Go Back)
        startActivity(new Intent(AddStudent.this,AdminActivity.class));
    }
}
