package com.amkhaou.mikoled2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ValueBar;


/**
 * Created by leo_s on 07/03/2016.
 */
public class Cmd extends Fragment implements ColorPicker.OnColorSelectedListener,ValueBar.OnValueChangedListener,
        CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {


    private boolean mBound = false;
    private MikoService mService;
    private ColorPicker mColorPicker;
    private ValueBar mValueBar;
    private int mRed;
    private int mGreen;
    private int mBlue;
    private RadioButton mRgbButton;
    private RadioButton mWhiteButton;
    private SeekBar mWhiteBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.cmd_layout,container,false);
        // get color picker and valuebar
        mColorPicker = (ColorPicker) v.findViewById(R.id.color_picker);
        mValueBar = (ValueBar) v.findViewById(R.id.valuebar);
        //  get radiobutton
        mRgbButton = (RadioButton) v.findViewById(R.id.rgbbutton);
        mWhiteButton = (RadioButton) v.findViewById(R.id.whitebutton);
        // get seekbar
        mWhiteBar = (SeekBar) v.findViewById(R.id.whitebar);

        // connect value bar to colorpicker

        mColorPicker.addValueBar(mValueBar);
        mColorPicker.setOnColorSelectedListener(this);
        mColorPicker.setShowOldCenterColor(false);
        // add listeners
        mValueBar.setOnValueChangedListener(this);
        mRgbButton.setOnCheckedChangeListener(this);
        mWhiteButton.setOnCheckedChangeListener(this);
        mWhiteBar.setOnSeekBarChangeListener(this);

        return v;

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
        Log.d("Cmd", "On Start mBound " + mBound);

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



    @Override
    public void onColorSelected(int color) {

        mBlue =  color & 255;
        mGreen = (color >> 8) & 255;
        mRed =   (color >> 16) & 255;
        if(mRgbButton.isChecked())
            mService.senddata('C', mRed, mGreen, mBlue);


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if( mWhiteButton.isChecked())
        {
            mService.senddata('C', progress, progress, progress);
            mRed=mGreen=mBlue= progress;
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onValueChanged(int value) {

        mBlue =  value & 255;
        mGreen = (value >> 8) & 255;
        mRed =   (value >> 16) & 255;
        if( mRgbButton.isChecked())
            mService.senddata('C', mRed, mGreen, mBlue);

    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        int color =0;

        // TODO Auto-generated method stub

        if(mRgbButton.isChecked()&&(button==mRgbButton))
        {
            // check if visualizer is enable
            //	if(communicator.getvisualizerstatus())
            //		communicator.disablevisualizer();
            //clear white button
            mWhiteButton.setChecked(false);
            // get rgb colors
            color = mColorPicker.getColor();
            mBlue =  color & 255;
            mGreen = (color >> 8) & 255;
            mRed = (color >> 16) & 255;
            mService.senddata('C', mBlue, mGreen, mRed);
        }

        if(mWhiteButton.isChecked()&&(button==mWhiteButton))
        {
            // check if visualizer is enable
            //	if(communicator.getvisualizerstatus())
            //		communicator.disablevisualizer();
            // clear rgb button
            mRgbButton.setChecked(false);
            mRed=mGreen=mBlue= mWhiteBar.getProgress();
            mService.senddata('C', mRed, mGreen,mBlue);

        }

    }
}
