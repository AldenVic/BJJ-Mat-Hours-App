package com.example.jp.jiu_jitsu;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

public class Promotion extends Activity {
	private ListView lv;
	private EditText et;
	private ArrayList<String> listview_array=new ArrayList<String>();
	private ArrayList<String> array_sort = new ArrayList<String>();
	private ArrayList<Student> student_list = new ArrayList<Student>();
	String origin;
	int textlength = 0;
	ProgressDialog progressDialog;
	DynamoDBMapper p;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        //Set origin from intent
        Bundle extras=getIntent().getExtras();
        origin=extras.getString("origin");
        
        p = new DbMapper((Context)this).getMapper();
        GetStudents g=new GetStudents();
        g.execute();
        progressDialog=ProgressDialog.show(Promotion.this,"","Loading Details");
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
                array_sort.clear();
                for (int i = 0; i < listview_array.size(); i++)
                {
                    if (textlength <= listview_array.get(i).length())
                    {
                        String s2= et.getText().toString();
                        if(listview_array.get(i).toString().contains(s2))
                        {
                            array_sort.add(listview_array.get(i));
                        }
                    }
                }

                lv.setAdapter(new ArrayAdapter<String>
                (Promotion.this,
                android.R.layout.simple_list_item_1, array_sort));
            }
        }); //Text changed Listener ends
        
        	   
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_promotion, menu);
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
    
    private class GetStudents extends AsyncTask<Void,Void,Void>{
    	@Override
		protected Void doInBackground(Void... params) {
			try{
    			
    			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
    			
    			Map<String, Condition> scanFilter = new HashMap<String, Condition>();

    			Condition scanCondition = new Condition()
    			    .withComparisonOperator(ComparisonOperator.NOT_NULL);

    			scanFilter.put("Barcode", scanCondition);
    			
    			scanExpression.setScanFilter(scanFilter);

    			List<Student> studentRecords = p.scan(Student.class, scanExpression);

    			for(Student s : studentRecords){
    				listview_array.add(s.getFirstName()+" "+s.getLastName());
    				student_list.add(s);
    			}
        	}catch(Exception e)
        	{
        		e.printStackTrace();
        	}
			return null;
    	}
    	@Override
		protected void onPostExecute(Void result) {
			lv = (ListView) findViewById(R.id.listView1);
			progressDialog.dismiss();
            int x= lv.getHeaderViewsCount ();
            System.out.println("x========"+x);
            lv.setAdapter(new ArrayAdapter<String>
            (Promotion.this,
            android.R.layout.simple_list_item_1, listview_array));  
            
            lv.setOnItemClickListener(new OnItemClickListener(){

    			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
    					long arg3) {
    				// TODO Auto-generated method stub
    				String full_name=lv.getItemAtPosition(arg2).toString();
    				int i=0;
    				for(i=0;i<student_list.size();i++)
    				{
    					String name=student_list.get(i).getFirstName()+" "+student_list.get(i).getLastName();
    					if(name.equals(full_name))
    						break;
    				}
    				
    				String[] student_data=new String[7];
    				student_data[0]=student_list.get(i).getFirstName();
    				student_data[1]=student_list.get(i).getLastName();
    				student_data[2]=student_list.get(i).getBarcode();
    				student_data[3]=student_list.get(i).getCurrentRank()+"";
    				//SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    				Date lastPromo = student_list.get(i).getLastPromotion();
    				Long millis = lastPromo.getTime();
    				student_data[4]=millis.toString();
    				student_data[5]=student_list.get(i).getPhoneNumber();
    				student_data[6]=student_list.get(i).getEmailAddress();
    				if(origin.equals("promote"))
    				{
	    				Intent intent = new Intent(getApplicationContext(),PromoteStudent.class);
	    				intent.putExtra("student_data",student_data);
	    				startActivity(intent);
    				}
    				else
    				{
    					Intent intent = new Intent(getApplicationContext(),EditStudent.class);
        				intent.putExtra("student_data",student_data);
        				startActivity(intent);
    				}
    			}
            });
    	}
    }

}