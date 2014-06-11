package com.example.jp.jiu_jitsu;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class AddEventsToCalendar extends Activity {

	String calInfo;
	InputStream input;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);
        AssetManager a = getAssets();
        try{
        input=a.open("JPSchedule.xml");
        }catch(Exception e)
        {
        	//Toast
        	//TextView t=(TextView) findViewById(R.id.textEventID);
	    	//t.setText(e.getMessage());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    public void addNewEvent(View view)
    {
    	ScheduleParser p=new ScheduleParser();
    	ArrayList<EventsData> eventList=p.readSchedule(input);
    	UpdateCalendar u=new UpdateCalendar();
    	u.execute(eventList);
    }
    private class UpdateCalendar extends AsyncTask<ArrayList<EventsData>,Void,Long[]>{
    	boolean google=true;
    	String acname=null;
    	protected Long[] doInBackground(ArrayList<EventsData>...eventList){
    		long calID = -1;
    		Long[] eventIDs=new Long[eventList.length];
    		try
    		{	
    			Uri uri = CalendarContract.Calendars.CONTENT_URI;
    			String[] projection = new String[] {
    			       CalendarContract.Calendars._ID,
    			       CalendarContract.Calendars.ACCOUNT_NAME,
    			       CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    			       CalendarContract.Calendars.NAME,
    			       CalendarContract.Calendars.ACCOUNT_TYPE,
    			       CalendarContract.Calendars.CALENDAR_COLOR
    			};
    			calInfo=" ";
    			Cursor calendarCursor = getContentResolver().query(uri, projection, null, null, null);
    			if(calendarCursor.moveToFirst())
    			{
    				do{
    					calInfo+="\nCal i\n";
    					for(int i=0;i<projection.length;i++)
    					{
    						calInfo+=calendarCursor.getString(calendarCursor.getColumnIndex(projection[i]))+" ";
    					}
    					
    				}while(calendarCursor.moveToNext());

    				calID=getJPCal(calendarCursor,projection,"gmail.com");
    				ContentResolver contentResolver = getContentResolver();
    				if(calID==-1)
    				{
    					calInfo+="\n Cal to be created.";
    					//If no google calendar synced in phone
    					//Add calendar under Local account
    					addCalendar(contentResolver,"JP Jiu Jitsu",CalendarContract.ACCOUNT_TYPE_LOCAL);//"com.google");
    					calendarCursor = getContentResolver().query(uri, projection, null, null, null);
    					calID=getJPCal(calendarCursor,projection,"JP Jiu Jitsu");
    					google=false;
    				}
    				calInfo+="\n CalID created : "+calID;

    				calID=calendarCursor.getLong(calendarCursor.getColumnIndex(projection[0]));
    				Uri eventUri = CalendarContract.Events.CONTENT_URI;    				
    				String[] eventProjection = {"calendar_id","title", "description", "dtstart","dtend","rrule"};
    				String eventDescription = "JayPagesJiuJitsuEvent";
    				String recRule = "FREQ=WEEKLY";
    				for(int i=0;i<eventList[0].size();i++)
        			{
    					Cursor eventCursor = getContentResolver().query(eventUri, eventProjection,"calendar_id="+calID+" AND dtstart="+eventList[0].get(i).getEventStart()+" AND dtend="+eventList[0].get(i).getEventEnd(), null, null);
    	    			calInfo+="\n"+eventCursor.getCount();
    	    			//Fix it with a global variable and check condition like that
    					if(eventCursor.getCount()==0)
    	    			{
    	    				ContentResolver cr = getContentResolver();
        	    			ContentValues values = new ContentValues();
        	    			values.put(CalendarContract.Events.DTSTART, eventList[0].get(i).getEventStart());
        	    			values.put(CalendarContract.Events.DTEND, eventList[0].get(i).getEventEnd());
        	    			values.put(CalendarContract.Events.TITLE, eventList[0].get(i).getEventName());
        	    			values.put(CalendarContract.Events.DESCRIPTION, eventDescription);
        	    			values.put(CalendarContract.Events.CALENDAR_ID, calID);
        	    			values.put(CalendarContract.Events.RRULE, recRule);
        	    			values.put(CalendarContract.Events.EVENT_TIMEZONE, "eventTimezone");
        	    			uri = cr.insert(Events.CONTENT_URI, values);
        	    			eventList[0].get(i).setEventId(Long.parseLong(uri.getLastPathSegment()));
        	    			calInfo+="Here\n";
    	    			}
        			}
        			calInfo+="\n"+eventList[0].size()+"Events added to calendar";
    			}
    			else
    				calInfo="No data in cursor";
    		}
    		catch(Exception ex){
    			calInfo+="\n"+ex.getMessage();
    			ex.printStackTrace();
    		}
			return eventIDs;
    	}
    	
        protected void onPostExecute(Long[] eventID){
        	//Toast here
        	String message=null;
        	if(google)
        		message="Added events to calendar "+acname;
        	else
        		message="Added events to the new JP Jiu Jitsu calendar created on the phone";
        	Toast t=Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        	t.show();
    	}
        
        public long getJPCal(Cursor calendarCursor,String[] projection, String type)
        {
        	calendarCursor.moveToFirst();
        	do{
				String account_name=calendarCursor.getString(calendarCursor.getColumnIndex(projection[1]));
				if(account_name.contains(type))//if(display_name.equals("JP Jiu Jitsu"))
				{
					acname=account_name;
					return calendarCursor.getLong(calendarCursor.getColumnIndex(projection[0]));
				}
			}while(calendarCursor.moveToNext());
        	return -1;
        }
        public int removeLocalCalendar( ContentResolver contentResolver, long id )
        {
            int result = contentResolver.delete( CalendarContract.Calendars.CONTENT_URI, BaseColumns._ID + "=?", new String[] {Long.toString(id)});
            Log.d( "trace", "Calendar deleted: " + result );
            return result;
        }
        
        public long addCalendar(ContentResolver contentResolver, String account_name, String account_type)
        {
            TimeZone timeZone = TimeZone.getDefault();

            if(account_type=="com.google")
            	account_name="JP Jiu Jitsu";
           
            ContentValues contentValues = new ContentValues();

            contentValues.put( CalendarContract.Calendars.ACCOUNT_NAME, account_name );
            contentValues.put( CalendarContract.Calendars.ACCOUNT_TYPE, account_type ); //CalendarContract.ACCOUNT_TYPE_LOCAL );
            contentValues.put( CalendarContract.Calendars.OWNER_ACCOUNT, account_name );

            contentValues.put(CalendarContract.Calendars.NAME, "JP Jiu Jitsu");
            contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "JP Jiu Jitsu" );
            contentValues.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF000000 + Math.floor( Math.random() * 0xFF0000));
            contentValues.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);

            contentValues.put(CalendarContract.Calendars.VISIBLE, 1);
            contentValues.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
            contentValues.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());        

            Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
            calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account_name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account_type)
                .build();

            try
            {
                Uri result = contentResolver.insert(calendarUri, contentValues);
                return Long.parseLong( result.getLastPathSegment());
            }
            catch( Exception exception )
            {
                Log.e( "trace", exception.toString() );
            }
            
            return -1;
        }
    }
}