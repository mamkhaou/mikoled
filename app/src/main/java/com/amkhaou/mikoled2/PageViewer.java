package com.amkhaou.mikoled2;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.support.v7.widget.Toolbar;

public class PageViewer extends FragmentActivity {

    ViewPager mainpager;
    protected String mMikoname = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_viewer);
        mainpager= (ViewPager) findViewById(R.id.main_pager);
        PageAdapter padapter = new PageAdapter(getSupportFragmentManager());
        // set pageadapter of mainpager
        mainpager.setAdapter(padapter);

        //get Mikoled name
        Intent intent =  getIntent();
        mMikoname= intent.getStringExtra("MIKONAME");

        // start service foreground service
        Intent serviceintent = new Intent(this,MikoService.class);
        serviceintent.putExtra("MIKONAME", mMikoname);
        startService(serviceintent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_control,menu);
        return true;
    }
}
