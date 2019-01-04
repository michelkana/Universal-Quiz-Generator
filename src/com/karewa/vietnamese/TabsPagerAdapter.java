package com.karewa.vietnamese;

import com.karewa.vietnamese.FilterDataFragment;
import com.karewa.vietnamese.LearnFragment;
import com.karewa.vietnamese.LoadDataFragment;
import com.karewa.vietnamese.QuizFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
 
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            return new QuizFragment();
        case 1:
            return new LearnFragment();
        /*case 2:
            return new LoadDataFragment();*/
	    case 2:
	        return new FilterDataFragment();
	    }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
 
}
