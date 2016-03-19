package com.amkhaou.mikoled2;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by p077491 on 18/03/2016.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PopNotificationListener extends NotificationListenerService {

    private MikoService mService;
    private boolean mBound;

    @Override
    public void onCreate(){
        super.onCreate();
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MikoService.LocalBinder binder = (MikoService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };



    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //..............
        Log.d("Service", sbn.toString());

        // Bind to LocalService
        Intent intent = new Intent(getApplicationContext(), MikoService.class);
        mBound = getApplicationContext().bindService(intent, mConnection, 0/*Context.BIND_AUTO_CREATE*/);

        // send notification commande to mikoled
        if( mService != null) {
            if(mService.getpopnotificationstatus())
                mService.sendrequest('N');
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //..............
    }
}