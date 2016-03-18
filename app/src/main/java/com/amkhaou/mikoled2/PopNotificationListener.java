package com.amkhaou.mikoled2;

import android.annotation.TargetApi;
import android.content.Intent;
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

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("Service", "Inside  on create");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //..............
        Log.d("Service", "Ca marche");
        //senddata('C',255,255,255);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //..............
    }
}