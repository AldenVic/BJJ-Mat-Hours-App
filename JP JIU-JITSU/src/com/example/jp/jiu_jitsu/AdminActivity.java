package com.example.jp.jiu_jitsu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class AdminActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_admin, menu);
        return true;
    }
    public void addHours(View view)
    {
    	//Start AddHours Activity
    	startActivity(new Intent(AdminActivity.this,ChooseHoursAddType.class));
    }
    public void addStudent(View view)
    {
    	//Start AddStudent Activity
        startActivity(new Intent(AdminActivity.this,AddStudent.class));
    }
    public void promoteStudent(View view)
    {
    	//Start Promotion Activity with origin as promote
    	Intent i = new Intent(AdminActivity.this,StudentList.class);
    	i.putExtra("origin","promote");
    	startActivity(i);
    }
    public void editStudent(View view)
    {
    	//Start Promotion Activity with origin as edit
    	Intent i = new Intent(AdminActivity.this,StudentList.class);
    	i.putExtra("origin","edit");
    	startActivity(i);
    }
    
    public void viewProgress(View view)
    {
    	//Start AddStudent Activity
        startActivity(new Intent(AdminActivity.this,ViewProgress.class));
    }
}