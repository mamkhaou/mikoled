package com.amkhaou.mikoled2;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.support.v7.widget.Toolbar;

public class PageViewer extends ActionBarActivity {

    ViewPager mainpager;
    protected String mMikoname = null;
    protected android.support.v7.app.ActionBar mActionBar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_viewer);
        mainpager = (ViewPager) findViewById(R.id.main_pager);
        PageAdapter padapter = new PageAdapter(getSupportFragmentManager());
        // set pageadapter of mainpager
        mainpager.setAdapter(padapter);

        //get Mikoled name
        Intent intent =  getIntent();
        mMikoname= intent.getStringExtra("MIKONAME");

        //change actionbar name
        mActionBar = getSupportActionBar();
        mActionBar.setTitle(Html.fromHtml("<small>"+mMikoname+"</small>"));

       /* getSupportActionBar().setLogo(R.drawable.light_bulb_4);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/


        // start service foreground service
        Intent serviceintent = new Intent(this,MikoService.class);
        serviceintent.putExtra("MIKONAME", mMikoname);
        startService(serviceintent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }
}
