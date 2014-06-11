package com.example.jp.jiu_jitsu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.example.jp.dal.DbMapper;
import com.example.jp.dal.HoursEntry;

public class RecentClass extends Activity {
	String selectedBarcode=null;
	List<Map<String,String>> classNames=new ArrayList<Map<String,String>>();
	ArrayList<Float> duration=new ArrayList<Float>();
	float selectedTime=0;
	String selectedClass=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_class);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        GetEvents g = new GetEvents();
        g.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_recent_class, menu);
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
    
    //Triggered on Button onClick event buttons dynamically created based on the no. of recent events found
    public void addHours(int index)
    {
    	selectedTime=duration.get(index);
    	Map<String,String> temp=classNames.get(index);
    	selectedClass=temp.get("name");
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        startActivityForResult(intent, 0);
    }
    
    //Triggered onClick for button Add Events
    public void addEvents(View view)
    {
    	//Goto Main activity which will add Events
    	startActivity(new Intent(RecentClass.this,AddEventsToCalendar.class));
    }
    
    //Triggered once Barcode scanning is successful
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                selectedBarcode=contents+format;
                new Thread(new Runnable(){
                	public void run(){
                		addHoursToDB(selectedBarcode,selectedClass,selectedTime);
                	}
                }).start();
            			
            	try
            	{
            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         
        			alertDialogBuilder.setTitle("Mat Hours added");
         
        			alertDialogBuilder
        				.setMessage("Click Yes to add more, No to return to home screen")
        				.setCancelable(false)
        				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog,int id) {
        						// if this button is clicked, close
        						// current activity
        						startActivity(new Intent(RecentClass.this,RecentClass.class));
        					}
        				  })
        				.setNegativeButton("No",new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog,int id) {
        						// if this button is clicked, just close
        						// the dialog box and do nothing
        						startActivity(new Intent(RecentClass.this,AdminActivity.class));
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
    //Method to add entry to the Dynamo DB database
    private void addHoursToDB(String bar, String r, float time)
    {
    	try{
	        	DynamoDBMapper dynamoDB =  new DbMapper((Context)this).getMapper();
	        	HoursEntry hoursObject = new HoursEntry();
	        	hoursObject.setBarcode(bar);
	        	hoursObject.setReason(r);
	        	hoursObject.setNumHours(time);
	        	hoursObject.setTimestamp(new Date(System.currentTimeMillis()));
	        	dynamoDB.save(hoursObject);
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    }

    //AsyncTask to do the background processing to get the recent class from the schedule uploaded in Android Calendar
    private class GetEvents extends AsyncTask<Void,Void,List<Map<String,String>>>{
    	protected List<Map<String,String>> doInBackground(Void...empty){
    		try{
    			//Get calendarId
    			int calID=-1;
    			Uri calUri = CalendarContract.Calendars.CONTENT_URI;
    			String[] calProjection = new String[] {
    			       CalendarContract.Calendars._ID,
    			       CalendarContract.Calendars.ACCOUNT_NAME,
    			       CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    			       CalendarContract.Calendars.NAME,
    			       CalendarContract.Calendars.ACCOUNT_TYPE,
    			       CalendarContract.Calendars.CALENDAR_COLOR
    			};
    			Cursor calendarCursor = getContentResolver().query(calUri, calProjection, null, null, null);
    			calendarCursor.moveToFirst();
            	do{
    				String account_name=calendarCursor.getString(calendarCursor.getColumnIndex(calProjection[1]));
    				if(account_name.contains("gmail.com") || account_name.equals("JP Jiu Jitsu"))//if(display_name.equals("JP Jiu Jitsu"))
    				{
    					calID=calendarCursor.getInt(calendarCursor.getColumnIndex(calProjection[0]));
    					break;
    				}
    			}while(calendarCursor.moveToNext());
    			
    		 	Uri uri = CalendarContract.Events.CONTENT_URI;
    	        String[] projection = {"calendar_id","title", "description", "dtstart","dtend","duration"};
    	        long currentMillis = System.currentTimeMillis();
    	        //Cursor eventCursor = getContentResolver().query(uri, projection,"calendar_id="+calID+" AND "+currentMillis+"-dtend<1800000 AND "+currentMillis+"-dtend>0 AND description='JayPagesJiuJitsuEvent'", null, null);
    	        
    	        //Updated query to fetch 5 recent events from JP Schedule in calendar
    	        Cursor eventCursor = getContentResolver().query(uri, projection,
    	        					"calendar_id="+calID+" AND dtstart<"+currentMillis+" AND description='JayPagesJiuJitsuEvent'", 
    	        					null, "dtstart desc limit "+5);
    	        
    	        if(eventCursor.moveToFirst())
    	        {
    	        	do{
    	        		String title=eventCursor.getString(eventCursor.getColumnIndex(projection[1]));
    	        		long startMills=eventCursor.getLong(eventCursor.getColumnIndex(projection[3]));
    	        		long endMills=eventCursor.getLong(eventCursor.getColumnIndex(projection[4]));
    	        		SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d,''yy h:mm a");
    	        		String start=df.format(new Date(startMills));
    	        		df = new SimpleDateFormat("h:mm a");
    	        		String end=df.format(new Date(endMills));
    	        		float diff=(float)(endMills-startMills)/(1000*60*60);
    	        		duration.add(diff);
    	        		Map<String,String> classData= new HashMap<String,String>(2);
    	        		classData.put("name", title);
    	        		classData.put("time",start+"-"+end);
    	        		classNames.add(classData);
    	        	}while(eventCursor.moveToNext());
    	        }	
    	        return classNames;
    		}catch(Exception ex){
    			return classNames;
    		}
    	}
    	protected void onPostExecute(List<Map<String,String>> classNames){
    		ListView lv = (ListView) findViewById(R.id.listView1);
    		TextView t1 =  (TextView) findViewById(R.id.textView1);
    		
    		if(classNames.isEmpty())
    		{
    			t1.setText("No classes found in calendar");
    			lv.setVisibility(View.INVISIBLE);
    		}
    		else
    		{
    			SimpleAdapter adapter = new SimpleAdapter(RecentClass.this, classNames,
                        android.R.layout.simple_list_item_2,
                        new String[] {"name", "time"},
                        new int[] {android.R.id.text1,
                                   android.R.id.text2});
    			lv.setAdapter(adapter);
    			 lv.setOnItemClickListener(new OnItemClickListener(){

    	    			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
    	    					long arg3) {
    	    				addHours(position);
    	    			}
    			 });
    		}
    	}
    }
}
