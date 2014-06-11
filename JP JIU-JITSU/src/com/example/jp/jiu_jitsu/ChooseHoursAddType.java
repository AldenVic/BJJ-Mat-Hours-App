package com.example.jp.jiu_jitsu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ChooseHoursAddType extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_hours_add_type);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_choose_hours_add_type, menu);
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
    public void autoAdd(View view)
    {
    	//Start Activity to retrieve recent Class
    	startActivity(new Intent(ChooseHoursAddType.this,RecentClass.class));
    }
    public void manualAdd(View view)
    {
    	//Start Activity to manually enter mat hours
    	startActivity(new Intent(ChooseHoursAddType.this,ManualAdd.class));
    }
}
