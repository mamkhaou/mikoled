/**
 * Created by leo_s on 07/03/2016.
 */
package com.amkhaou.mikoled2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MikoService extends Service implements Runnable,OnDataCaptureListener {

    // Binder given to clients

    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothSocket mBtSocket = null;
    private OutputStream mOutStream = null;
    private InputStream mInStream = null;
    private BluetoothDevice mDevice;
    private String mMikoname=null;
    private boolean mPowerstatus = false;
    private boolean mCallNotification=false;
    private boolean mPopNotification=false;
    public int mRed;
    public int mGreen;
    public int mBlue;
    private Visualizer mMusicVisualizer;
    private boolean mVisualizerstatus=false;
    // Well known SPP UUID
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /* Class used for the client Binder.  Because we know this service always
    * runs in the same process as its clients, we don't need to deal with IPC.
    */
    public class LocalBinder extends Binder {
        MikoService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MikoService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("Service", "Service started with work commit test 2878");

        Intent notificationIntent = new Intent(this, Cmd.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // bluetooth connection/reconnection
        mMikoname= intent.getStringExtra("MIKONAME");
        if(mBtSocket == null)
            btconnect(mMikoname);


        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Mikoled application")
                .setTicker("Mikoled application")
                .setContentText(mMikoname)
                .setSmallIcon(R.drawable.light_bulb_4)
                .setContentIntent(pendingIntent)
                .addAction(0,"SWITCH OFF",pendingIntent)
                .setOngoing(true).build();
        startForeground(1,notification);

        // start read thread
        Thread t = new Thread (this);
        t.start();

        // check time synchronization
        sendrequest ('t');
        //check power status
        sendrequest ('c');


        // creat visualizer with audio mix out
        if(!mVisualizerstatus)
        {
            mMusicVisualizer = new Visualizer(0);
            //set capture size
            mMusicVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]); //512 sample  rang 1
            // set listener
            mMusicVisualizer.setDataCaptureListener(this,Visualizer.getMaxCaptureRate()/2 , false, true);
        }

        // phone listener
        MyPhoneStateListener phone = new MyPhoneStateListener();
        TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyMgr.listen(phone, PhoneStateListener.LISTEN_CALL_STATE);
        PopNotificationListener pp;


        return START_REDELIVER_INTENT;
    }

    @Override
    public void run() {
        char cmd;
        byte buffer[] = new byte[6];
        Calendar calendar;
        int hour,minute,second,day,month,year;
        // TODO Auto-generated method stub

        while ( true )
        {
            if (!mBtSocket.isConnected())
                btconnect(mMikoname);
            else
            {
                try {
                    // read cmd byte
                    cmd=(char) mInStream.read();
                    switch (cmd)
                    {
                        case 't': 	mInStream.read(buffer,0,6);
                            calendar =Calendar.getInstance();
                            // get current date and time
                            hour = calendar.get(Calendar.HOUR);
                            minute = calendar.get(Calendar.MINUTE);
                            second = calendar.get(Calendar.SECOND);
                            day = calendar.get(Calendar.DAY_OF_MONTH);
                            month = calendar.get(Calendar.MONTH);
                            year = calendar.get(Calendar.YEAR);
                            //check if the mikoled is synchronized
                            if((hour != buffer[0])||(minute != buffer[1])
                                    ||(day != buffer[3])||(month != buffer[4])||((year-2000) != buffer[5]))
                            {
                                sendtime(hour, minute, second, day, month, year);
                            }
                            break;
                        case 'c':	mInStream.read(buffer,0,3);
                            // check if the mikoled is on
                            if((buffer[0]!=0)||(buffer[1]!=0)||(buffer[2]!=0))
                                mPowerstatus = true;
                            else
                                mPowerstatus = false;

                            mRed= buffer[0];
                            mGreen= buffer[1];
                            mBlue= buffer[2];
                            break;
                        default  :  break;



                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }


    void btconnect(String btname)
    {
        // getting btadatper
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btdevicetmp;
        //try to get the chosen Mikoled only if bt is activated
        if ((mBtAdapter != null)&&(mBtAdapter.isEnabled()))
        {
            // get list of paired devices
            Set<BluetoothDevice> btpd = mBtAdapter.getBondedDevices();
            Iterator<BluetoothDevice> it = btpd.iterator();
            // check all paired device with the received name
            while (it.hasNext())
            {
                btdevicetmp=it.next();
                // if we found the selected device get btsocket
                if(btname.equals(btdevicetmp.getName()))
                {
                    mDevice = btdevicetmp;
                    //get btsocket from device
                    try {
                        mBtSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // cancel bt discovering
                    //mBtAdapter.cancelDiscovery();
                    //connect Btsocket
                    try {
                        mBtSocket.connect();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // get input outout strems
                    try {
                        mOutStream = mBtSocket.getOutputStream();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    try {
                        mInStream = mBtSocket.getInputStream();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }

        }

    }


    public void stopService()
    {
        Log.d("test","stopService ");
        //close in/out streams
        if(mInStream != null)
        {
            try {
                mInStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        if(mOutStream != null)
        {
            try {
                mOutStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(mBtSocket != null)
        {
            try {
                mBtSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // stop service
        stopSelf();
        // exit application

    }

    public void senddata(char C, int data1,int data2,int data3)
    {
        if(mOutStream != null)
        {
            mRed=data1;
            mGreen=data2;
            mBlue=data3;
            try {
                mOutStream.write(C);
                mOutStream.write(data1&0xFF);
                mOutStream.write(data2&0xFF);
                mOutStream.write(data3&0xFF);


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public void sendtime(int hour,int minute,int second	,int day,int month,int year)
    {
        if(mOutStream != null)
        {
            try {
                mOutStream.write('T');
                mOutStream.write(hour&0xFF);
                mOutStream.write(minute&0xFF);
                mOutStream.write(second&0xFF);
                mOutStream.write(day&0xFF);
                mOutStream.write(month&0xFF);
                mOutStream.write(year&0xFF);


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public void sendrequest(int request)
    {
        try {
            mOutStream.write(request&0xFF);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean getpowerstatus()
    {
        return mPowerstatus;
    }

    public byte[] getrgbvalue()
    {
        byte rgb[] = new byte[3];
        rgb[0]=(byte) mRed;
        rgb[1]=(byte) mGreen;
        rgb[2]=(byte) mBlue;

        return rgb;

    }
    public void enablevisualizer()
    {
        if(mMusicVisualizer != null)
        {
            mVisualizerstatus =true;
            mMusicVisualizer.setEnabled(true);
        }

    }

    public void disablevisualizer()
    {
        if(mMusicVisualizer != null)
        {
            mVisualizerstatus = false;
            mMusicVisualizer.setEnabled(false);
        }
    }

    public void callnotificationsetenable(boolean status)
    {
        mCallNotification= status;
    }

    public void popnotificationsetenable(boolean status)
    {
        mPopNotification = status;
    }


    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] buffer, int samplingRate) {
        // TODO Auto-generated method stub

        mBlue=(int) ( Math.sqrt((buffer[2]*buffer[2])+(buffer[3]*buffer[3]))*1.78);// bass
        mGreen=(int) ( Math.sqrt((buffer[22]*buffer[22])+(buffer[23]*buffer[23]))*2);// mid
        mRed=(int) ( Math.sqrt((buffer[96]*buffer[96])+(buffer[97]*buffer[97]))*4.25);// treble
        // send rgb
        senddata('C', mRed, mGreen, mBlue);
    }


    @Override
    public void onWaveFormDataCapture(Visualizer arg0, byte[] arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    public boolean getvisualizerstatus() {
        // TODO Auto-generated method stub
        return mMusicVisualizer.getEnabled();
    }

    public class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if(mCallNotification)
            {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d("DEBUG", "IDLE");
                        // send phone blinking cmd stop
                        sendrequest('p');
                        senddata('C',mRed,mGreen,mBlue);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.d("DEBUG", "OFFHOOK");

                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d("DEBUG", "RINGING");
                        // stop visualizer if enabled
                        if(mMusicVisualizer.getEnabled())
                            mMusicVisualizer.setEnabled(false);
                        // send phone blinking cmd start
                        sendrequest('P');

                        break;
                }
            }
        }

    }




}
