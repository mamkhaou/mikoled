package com.amkhaou.mikoled2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by leo_s on 07/03/2016.
 */
public class Options extends Fragment implements CompoundButton.OnCheckedChangeListener {


    private Switch mMusicVisualizersw;
    private Switch mCallNotification;
    private Switch mPopNotification;
    SharedPreferences mPreferences;
    SharedPreferences.Editor editor;
    private boolean mBound = false;
    private MikoService mService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.options_layout, container, false);
        // get switches
        mMusicVisualizersw = (Switch) v.findViewById(R.id.Musicvisualizer);
        mCallNotification = (Switch) v.findViewById(R.id.Callnotification);
        mPopNotification = (Switch) v.findViewById(R.id.PopNotification);
        // get shared preferences instance
        mPreferences = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        editor = mPreferences.edit();

        // Bind to LocalService
        Intent intent = new Intent(getActivity(), MikoService.class);
        mBound = getActivity().bindService(intent, mConnection, 0/*Context.BIND_AUTO_CREATE*/);
        Log.d("Options","On CreatView ");



        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // add listener to vizualier switch
        mMusicVisualizersw.setOnCheckedChangeListener(this);
        // get visualizer status
//        mMusicVisualizersw.setChecked(mService.getvisualizerstatus());

        // add listeners expect vizulazer wich is created on service, so should be listned only after creating activity ( service )

        mCallNotification.setOnCheckedChangeListener(this);
        mPopNotification.setOnCheckedChangeListener(this);

        // get  call and notifications preferences
        mCallNotification.setChecked(mPreferences.getBoolean("callnotification", true));

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
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(getActivity(), MikoService.class);
        mBound = getActivity().bindService(intent, mConnection, 0/*Context.BIND_AUTO_CREATE*/);
        Log.d("Options", "On Start mBound" + mBound);
        mBound = mService.getvisualizerstatus();
        Log.d("Options", "On Start mBound" + mBound);


    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Options", "On Stop ");
        // Unbind from the service
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {/*
        if((button == mMusicVisualizersw)&&mService.mService_status)
        {
            // visualizer actions
            if (mMusicVisualizersw.isChecked())
                mService.enablevisualizer();
            else
                mService.disablevisualizer();
        }
        // call notification actions
        if((button == mCallNotification)&&mService.mService_status)
        {
            mService.callnotificationsetenable(button.isChecked());
            editor.putBoolean("callnotification", button.isChecked());
            editor.apply();
        }
        // pop notification actions
        if((button == mPopNotification)&&mService.mService_status)
        {
            mService.popnotificationsetenable(button.isChecked());
            editor.putBoolean("popnotification", button.isChecked());
            editor.apply();

        }
   */ }
}
