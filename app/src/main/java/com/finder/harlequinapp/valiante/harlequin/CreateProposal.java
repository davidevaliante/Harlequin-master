package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateProposal extends FragmentActivity {

    protected CustomViewPager proposalViewPager;
    protected ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_proposal);

        //inizializzazione Shared Preferences relative a quest'attività
        SharedPreferences proposalPref = getSharedPreferences("NEWPROPOSAL_PREF",Context.MODE_PRIVATE);

        proposalViewPager = (CustomViewPager)findViewById(R.id.proposalViewPager);
        List<Fragment> registrationFragements = initializeFragments();
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),registrationFragements);
        proposalViewPager.setPagingEnabled(false);
        proposalViewPager.setAdapter(pagerAdapter);
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> registrationFragments;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Fragment> registrationFragments) {
            super(fm);
            this.registrationFragments = registrationFragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.registrationFragments.get(position);
        }

        @Override
        public int getCount() {
            return this.registrationFragments.size();
        }


    }

    @Override
    public void onBackPressed() {
        if (proposalViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.

            ProposalDataFragment firstpage = (ProposalDataFragment)getSupportFragmentManager().getFragments().get(0);
            firstpage.saveData();
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            proposalViewPager.setCurrentItem(proposalViewPager.getCurrentItem() - 1);
        }
    }

    private List<Fragment> initializeFragments(){
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(ProposalDataFragment.newInstance());

        return fList;
    }

    //per la libreria Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
