package com.amkhaou.mikoled2;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by leo_s on 07/03/2016.
 */
public class PageAdapter extends FragmentPagerAdapter {

    public PageAdapter(FragmentManager fm)
    {
        super(fm);

    }
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0 : return new Cmd();
            case 1 : return new Options();
            default: break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
