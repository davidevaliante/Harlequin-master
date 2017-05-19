package com.finder.harlequinapp.valiante.harlequin;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by akain on 19/05/2017.
 */

public class UserPageAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> myFragments = new ArrayList<>();
    private String[] fragmentTitles;

    public UserPageAdapter(FragmentManager fm, ArrayList<Fragment> myFragments, String[] fragmentTitles) {
        super(fm);
        this.myFragments = myFragments;
        this.fragmentTitles = fragmentTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return myFragments.get(position);
    }

    @Override
    public int getCount() {
        return myFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles[position];
    }
}
