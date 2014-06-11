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
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.example.jp.dal.DbMapper;
import com.example.jp.dal.HoursEntry;

public class ManualAdd extends Activity {

	String selectedBarcode=null;
	Float duration=null;
	String description=null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add);
        
        Spinner hoursList = (Spinner)findViewById(R.id.spinner1);
        List<String> hours= new ArrayList<String>(Arrays.asList("0.5","1","1.5","2","2.5","3"));
        hoursList.setAdapter((SpinnerAdapter) new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hours));        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_manual_add, menu);
        return true;
    }
    
    //Button onClick to Barcode Scan
    public void proceedToBarcode(View view)
    {
    	//spinner item selected + 1 times 0.5 gives no. of hours selected
    	duration = (float) ((((Spinner)findViewById(R.id.spinner1)).getSelectedItemPosition()+1)*0.5);

    	description = ((EditText)findViewById(R.id.editText1)).getText().toString();
    	//Proceed to Barcode Scan Activity or Intent
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        startActivityForResult(intent, 0);
    }

    //Return from Barcode scan Intent update dB
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
//                TextView codeText = (TextView)findViewById(R.id.textView4);
//                codeText.setVisibility(View.VISIBLE);
//                TextView scanCode = (TextView)findViewById(R.id.textView5);
//                scanCode.setText(contents+format);
//                scanCode.setVisibility(View.VISIBLE);

                selectedBarcode = contents+format;

            	new Thread(new Runnable(){
            		public void run(){
            			addHoursToDB();
            		}
            	}
            	).start();
            	try
            	{
            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         
        			alertDialogBuilder.setTitle("Hours entered!");
         
        			alertDialogBuilder
        				.setMessage("Click Yes to add more, No to return to home screen")
        				.setCancelable(false)
        				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog,int id) {
        						// if this button is clicked, close
        						// current activity
        						}
        				  })
        				.setNegativeButton("No",new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog,int id) {
        						// if this button is clicked, just close
        						// the dialog box and do nothing
        						startActivity(new Intent(ManualAdd.this,AdminActivity.class));
        					}
        				});
        			
    				AlertDialog alertDialog = alertDialogBuilder.create();
    				alertDialog.show();
            	}catch(Exception e)
            	{
            		e.printStackTrace();
            	}
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
            }
        }
    }
    private void addHoursToDB()
    {
    	try{
	        	DynamoDBMapper dynamoDB =  new DbMapper((Context)this).getMapper();
	        	HoursEntry hoursObject = new HoursEntry();
	        	hoursObject.setBarcode(selectedBarcode);
	        	hoursObject.setReason(description);
	        	hoursObject.setNumHours(duration);
	        	hoursObject.setTimestamp(new Date(System.currentTimeMillis()));
	        	dynamoDB.save(hoursObject);
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    }
    
    public void backAdminView(View view)
    {
    	//Start Admin View activity (Go Back)
        startActivity(new Intent(ManualAdd.this,AdminActivity.class));
    }
}
