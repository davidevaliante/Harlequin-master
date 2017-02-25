package com.finder.harlequinapp.valiante.harlequin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

import static java.security.AccessController.getContext;

public class EventPage extends AppCompatActivity  {


    protected KenBurnsView eventImage;
    private TextView eEventDescription;
    protected String eventId;
    private FirebaseUser currentUser;

    private TextView eventTitle;
    private ValueEventListener eventDataListener;
    private DatabaseReference eventReference;
    private LinearLayout mapInfo;
    protected CollapsingToolbarLayout collapsingToolbar;
    private ImageButton toolBarArrow;
    private FloatingActionButton fab;
    private Boolean isLiked = false;
    private Snackbar snackBar;
    private CoordinatorLayout coordinatorLayout;
    private TextView avarAge, singlesNumber, engagedNumber;
    private boolean mProcessLike = false;
    private ValueEventListener likeSetterListener;
    private String userName, userId;

    private  boolean isMale,isSingle;
    private int userAge;
    String LOG = "INTENT_LOG:";
    private SharedPreferences userData;
    private Adapter adapter;
    private TabLayout tabs;
    private String current_city="Isernia";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);

        //recuperare dati dall'INTENT
        eventId = getIntent().getExtras().getString("EVENT_ID");
        getUserData();


        eventImage = (KenBurnsView)findViewById(R.id.pEventImage);
        eventTitle = (TextView)findViewById(R.id.pEventTitle);
        eEventDescription = (TextView)findViewById(R.id.pEventDescription);

        toolBarArrow = (ImageButton)findViewById(R.id.backToUserPage);
        fab = (FloatingActionButton)findViewById(R.id.likeFab);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.eventPageCoordinatorLayout);
        collapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);

        singlesNumber = (TextView)findViewById(R.id.singlesNumber);
        engagedNumber = (TextView)findViewById(R.id.engagedNumber);

        //TODO controllare se bisogna implementare una condizione IF in base aall'SDK per la toolbar su versioni precedenti
        //per cambiare il background della snackbar
        snackBar = Snackbar.make(coordinatorLayout, "LUL",Snackbar.LENGTH_SHORT);
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));





        //imposta il font ed il colore per la toolbar per la collapsingToolBar
        final Typeface tf = Typeface.createFromAsset(EventPage.this.getAssets(), "fonts/Roboto_Bold.ttf");
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);
        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.pureWhite));

        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.transparentColor));


        //Viewpager per i fragment
        ViewPager viewPager = (ViewPager)findViewById(R.id.event_viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager,savedInstanceState);


        //tablayout per i fragment
        tabs = (TabLayout)findViewById(R.id.event_tabs);
        tabs.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){




                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //per cambiare il font nel tablayout
        ViewGroup vg = (ViewGroup) tabs.getChildAt(0);
        changeFontInViewGroup(vg,"fonts/Hero.otf");


        //onclickListeners
        toolBarArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //pulsante per il like
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }

        }); //[END] fine OnClickListener




    }//fine di OnCreate

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager, Bundle savedInstanceState) {
        adapter = new Adapter(getSupportFragmentManager());

        adapter.addFragment(new EventDescription(), "Descrizione");
        adapter.addFragment(new JoinersList(), "Partecipanti");
        adapter.addFragment(new Contacts(), "Contatti");
        viewPager.setAdapter(adapter);


    }



    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private Float getMalePercentage (Integer totalLikes, Integer maleLikes){
        Float malePercentage ;
            malePercentage = Float.valueOf((100 * maleLikes) / totalLikes);
            return malePercentage;
    }

    private Float getFemalePercentage (Integer totalLikes, Integer femaleLikes){
        Float femalePercentage;
        femalePercentage = Float.valueOf((100*femaleLikes)/totalLikes);
        return femalePercentage;
    }

    //per usare i font personalizzati
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

     protected void removeLikeListener(DatabaseReference myReference, ValueEventListener myListener){
        myReference.removeEventListener(myListener);

    }



    protected void getUserData(){
        userData = getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        userName = userData.getString("USER_NAME","Name error");
        userAge = userData.getInt("USER_AGE",25);
        isSingle = userData.getBoolean("IS_SINGLE",true);
        isMale = userData.getBoolean("IS_MALE",true);
        userId = userData.getString("USER_ID","nope");

    }

    //cambia il titolo della toolbar, accessibile dai fragments
    protected void updatedToolbarTitle(String title){
        collapsingToolbar.setTitle(title);
    }

    //per cambiare il font nella toolBar
    void changeFontInViewGroup(ViewGroup viewGroup, String fontPath) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (TextView.class.isAssignableFrom(child.getClass())) {

                CalligraphyUtils.applyFontToTextView(child.getContext(), (TextView) child, fontPath);
            } else if (ViewGroup.class.isAssignableFrom(child.getClass())) {
                changeFontInViewGroup((ViewGroup) viewGroup.getChildAt(i), fontPath);
            }
        }
    }

}
