package com.example.jp.jiu_jitsu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.example.jp.dal.DbMapper;
import com.example.jp.dal.Rank;
import com.example.jp.dal.Student;

public class ViewProgress extends Activity {
	private ListView lv;
	private EditText et;
	private ArrayList<String> listview_array=new ArrayList<String>();
	private List<Map<String,String>> twoline_array=new ArrayList<Map<String, String>>();
	private List<Map<String,String>> twoline_array_sort=new ArrayList<Map<String, String>>();
	int textlength = 0;
	DynamoDBMapper p;
	DbMapper db;
	ProgressDialog progressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);
        
        p = new DbMapper((Context)this).getMapper();
        db = new DbMapper((Context)this);
        GetStudents g=new GetStudents();
        g.execute();
        progressDialog=ProgressDialog.show(ViewProgress.this,"","Loading Details");
        
        et = (EditText) findViewById(R.id.editText1);
        //Text Changed Listener to filter Student list
        et.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                // Abstract Method of TextWatcher Interface.

            }
            public void beforeTextChanged(CharSequence s,
            int start, int count, int after)
            {

                // Abstract Method of TextWatcher Interface.

            }
            public void onTextChanged(CharSequence s,
            int start, int before, int count)
            {
                textlength = et.getText().length();
                twoline_array_sort.clear();
                String editText = et.getText().toString();
                for(Map<String,String> item : twoline_array)
                {
                	String key="name";
                		if(item.get(key).contains(editText))
                			twoline_array_sort.add(item);
                }
                
                
                
//                for (int i = 0; i < listview_array.size(); i++)
//                {
//                    if (textlength <= listview_array.get(i).length())
//                    {
//                        String s2= et.getText().toString();
//                        if(listview_array.get(i).toString().contains(s2))
//                        {
//                            array_sort.add(listview_array.get(i));
//                        }
//                    }
//                }
//
//                lv.setAdapter(new ArrayAdapter<String>
//                (ViewProgress.this,
//                android.R.layout.simple_list_item_1, array_sort));
                
                SimpleAdapter adapter = new SimpleAdapter(ViewProgress.this, twoline_array_sort,
                        android.R.layout.simple_list_item_2,
                        new String[] {"name", "line2"},
                        new int[] {android.R.id.text1,
                                   android.R.id.text2});
    			lv.setAdapter(adapter);
            }                
            
          }); //Text changed Listener ends
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_view_progress, menu);
        return true;
    }
    
    
    private class GetStudents extends AsyncTask<Void,Void,Void>{
    	List<Rank> rankList;
    	@Override
		protected Void doInBackground(Void... params) {
			try{
    			
    			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
    			
    			Map<String, Condition> scanFilter = new HashMap<String, Condition>();

    			Condition scanCondition = new Condition()
    			    .withComparisonOperator(ComparisonOperator.NOT_NULL);

    			scanFilter.put("Barcode", scanCondition);
    			
    			scanExpression.setScanFilter(scanFilter);
    			//Scan all students under studentRecords List
    			List<Student> studentRecords = p.scan(Student.class, scanExpression);

    			scanFilter = new HashMap<String, Condition>();
    			scanFilter.put("RankID", scanCondition);
    			scanExpression.setScanFilter(scanFilter);
    			//ranks list stored below
    			rankList = p.scan(Rank.class, scanExpression);
    			
    			for(Student s : studentRecords){
    				String studentName = s.getFirstName()+" "+s.getLastName();
    				Date lastPromo = s.getLastPromotion();
    				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    				String lastPromoStr = df.format(lastPromo);
    				
    				
    				Condition rangeKeyCondition = new Condition()
    			    .withComparisonOperator(ComparisonOperator.GT.toString())
    			    .withAttributeValueList(new AttributeValue().withS(lastPromoStr));
    				
    				QueryRequest queryRequest = new QueryRequest().withTableName("MatHours")
    						.withHashKeyValue(new AttributeValue().withS(s.getBarcode()))
    						.withRangeKeyCondition(rangeKeyCondition)
    						.withAttributesToGet(Arrays.asList("NumHours"));
    				
    				QueryResult result = db.getClient().query(queryRequest);
    				
    				int hoursNeeded = getHoursNeeded(s.getCurrentRank());
    				
    				float h=0;
    				for (Map<String, AttributeValue> item : result.getItems()) {
    					for(Map.Entry<String, AttributeValue> val : item.entrySet())
    					{
	    					AttributeValue value = val.getValue();
	    					float temp = Float.parseFloat(value.getN()); 
	    					h+=temp;
	    				}
    		        }
    				
    				String line2;
    				if(hoursNeeded == 0)
    					line2="Mat Hours since last promotion "+h;
    				else
    					line2="Mat Hours status for next promotion "+h+"/"+hoursNeeded;
    				listview_array.add(studentName+"\n"+line2);
    				Map<String,String> item_data=new HashMap<String,String>(2);
    				item_data.put("name",studentName);
    				item_data.put("line2", line2);
    				twoline_array.add(item_data);
    			}
    			
        	}catch(Exception e)
        	{
        		e.printStackTrace();
        	}
			return null;
    	}
    	
    	private int getHoursNeeded(int currentRank) {			
    		int currentBelt = currentRank/10;
			int currentStripe = currentRank%10;
			String[] belts = {"White", "Blue", "Purple", "Brown", "Black", "Black and Red", "Red"};
			String beltColor = belts[currentBelt];
			
			for(Rank r : rankList){
				if(r.getBelt().equals(beltColor) && currentStripe == r.getStripe())
					return r.getRequiredHours();
			}
			
    		return 0;
		}
		@Override
		protected void onPostExecute(Void result) {
    		progressDialog.dismiss();
			lv = (ListView) findViewById(R.id.listView1);
			SimpleAdapter adapter = new SimpleAdapter(ViewProgress.this, twoline_array,
                    android.R.layout.simple_list_item_2,
                    new String[] {"name", "line2"},
                    new int[] {android.R.id.text1,
                               android.R.id.text2});
			lv.setAdapter(adapter);
			
            int x= lv.getHeaderViewsCount ();
            System.out.println("x========"+x);
    	}
    }

}
