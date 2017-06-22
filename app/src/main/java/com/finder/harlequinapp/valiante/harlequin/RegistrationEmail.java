package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.SectionIndexer;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegistrationEmail extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public CustomViewPager registrationViewPager;
    protected PagerAdapter myAdapter;
    protected String editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_email);

        //stringa inizializzata a seconda se si è in edit mode o in registrazione normale
        editId = getIntent().getStringExtra("EDIT_ID");

        List<Fragment> registrationFragments = initializeFragments();
        registrationViewPager = (CustomViewPager)findViewById(R.id.registrationViewPager);
        myAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),registrationFragments);
        registrationViewPager.setPagingEnabled(false);
        registrationViewPager.setAdapter(myAdapter);

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
        if (registrationViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            registrationViewPager.setCurrentItem(registrationViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private List<Fragment> initializeFragments(){
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(FirstPageRegistrationEmailFragment.newInstance());
        fList.add(SecondPageRegistrationEmailFragment.newInstance());
        fList.add(ThirdPageRegistrationEmailFragment.newInstance());

        return fList;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        FirstPageRegistrationEmailFragment firstPage = (FirstPageRegistrationEmailFragment) getSupportFragmentManager().getFragments().get(0);
        Integer fixedMonth = monthOfYear+1;
        firstPage.birthday.setText(dayOfMonth+"/"+fixedMonth+"/"+year);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = getSharedPreferences("EMAIL_REG",Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
