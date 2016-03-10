package com.amkhaou.mikoled2;

import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;



public class Control extends Activity {
    private BluetoothAdapter btAdapter = null;
    private Set<BluetoothDevice> btpaireddevices;
    private BluetoothDevice device = null;
    protected String mMikoname = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);
        Log.d("test", "In onCreate()");
        btAdapter = BluetoothAdapter.getDefaultAdapter();

       
    }


    @Override
    protected void onResume() {
        super.onResume();
        showdevicelist();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
        // This above line close correctly
    }

    void showdevicelist() {
        String[] tmppdlistname = new String[20];
        String[] pdlistname;
        int numdevices = 0;


        if (btAdapter != null) {
            //check if bluetooth is enabled. if no ask activation
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            } else {
                //get list of paired devices and store names in array of string
                btpaireddevices = btAdapter.getBondedDevices();
                if (btpaireddevices.size() > 0) {
                    for (BluetoothDevice pdevice : btpaireddevices) {
                        tmppdlistname[numdevices] = pdevice.getName();
                        numdevices++;
                    }
                    //store the final list of devices
                    pdlistname = new String[numdevices];
                    System.arraycopy(tmppdlistname, 0, pdlistname, 0, numdevices);
                    //pdlistname = tmppdlistname;
                    // creat the Array adapter and the list view
                    ArrayAdapter<String> btArrayAdapter = new ArrayAdapter<String>(this, R.layout.bt_itemtv, pdlistname);
                    ListView btlistview = (ListView) findViewById(R.id.paireddiveceslistview);
                    btlistview.setAdapter(btArrayAdapter);

                    btlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            TextView tvtmp = (TextView) view;
                            String tmp = tvtmp.getText().toString();
                            BluetoothDevice btdtmp;
                            Iterator<BluetoothDevice> it = btpaireddevices.iterator();
                            while (it.hasNext()) {
                                btdtmp = it.next();
                                if (tmp.equals(btdtmp.getName()))
                                    device = btdtmp;
                            }
                            // start the led control activity
                            if (device != null) {
                                Intent intent = new Intent(Control.this, PageViewer.class);
                                intent.putExtra("MIKONAME", device.getName());
                                startActivity(intent);
                                finish();
                            }


                        }
                    });

                }
            }
        }
    }
}