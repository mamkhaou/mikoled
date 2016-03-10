package com.amkhaou.mikoled2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by leo_s on 07/03/2016.
 */
public class Cmd extends Fragment {


    private boolean mBound = false;
    private MikoService mService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.cmd_layout,container,false);

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
        getActivity().bindService(intent, mConnection, 0/*Context.BIND_AUTO_CREATE*/);

    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }


}
