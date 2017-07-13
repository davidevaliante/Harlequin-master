package com.finder.harlequinapp.valiante.harlequin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.ContactsContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.List;

import cn.hugeterry.coordinatortablayout.CoordinatorTabLayout;
import cn.hugeterry.coordinatortablayout.listener.LoadHeaderImagesListener;

public class UserProfile extends AppCompatActivity {

    private int[] mImageArray, mColorArray;
    private CoordinatorLayout profileCoordinator;
    private  ViewPager profileViewpager;
    private TabLayout profileTabs;
    private CircularImageView profileCircular;
    private CollapsingToolbarLayout profileCollapse;
    private CoordinatorTabLayout userLayout;
    private ArrayList<Fragment> myFragments;
    private final String[] fragmentTitles = {"Followers","Following","Eventi"};
    private ViewPager userViewPager;
    private int[] colorArray,imageArray;
    protected boolean ownProfile;
    private DatabaseReference userRef;
    protected String profileUrl;
    protected String userName;
    private ValueEventListener userListener;
    private AdapterUser adapter;



    protected String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_coordinator);
        userId = getIntent().getExtras().getString("USER_ID");
        ownProfile = getIntent().getExtras().getBoolean("OWN_PROFILE");

        Toolbar toolbar = (Toolbar)findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.vector_burger_menu_24));

        final Typeface tf = Typeface.createFromAsset(UserProfile.this.getAssets(), "fonts/Hero.otf");


        profileViewpager = (ViewPager)findViewById(R.id.profileViewpager);
        profileViewpager.setOffscreenPageLimit(3);
        setupViewPager(profileViewpager);
        profileCoordinator = (CoordinatorLayout)findViewById(R.id.profileCoordinator);
        profileCollapse = (CollapsingToolbarLayout)findViewById(R.id.profileCollapsing);
        profileCollapse.setCollapsedTitleTypeface(tf);
        profileCollapse.setExpandedTitleTypeface(tf);
        profileCircular = (CircularImageView)findViewById(R.id.profileCircularImage);
        profileTabs = (TabLayout)findViewById(R.id.profileTabs);
        profileTabs.setupWithViewPager(profileViewpager);



        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profileUrl = user.getProfileImage();
                userName = user.getUserName()+" "+user.getUserSurname();
                userRef.removeEventListener(this);
                profileCollapse.setTitle(userName);
                loadProfile(profileCircular,profileUrl);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addValueEventListener(userListener);



    }

    static class AdapterUser extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public AdapterUser(FragmentManager manager) {
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

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        adapter = new AdapterUser(getSupportFragmentManager());
        if(ownProfile){
            adapter.addFragment(new FragmentRequests(),"Richieste");
        }
        adapter.addFragment(new FollowersFragment(), "Followers");
        adapter.addFragment(new FollowingFragment(), "Following");
        adapter.addFragment(new BasicUserFragment(), "Eventi");
        viewPager.setAdapter(adapter);
    }



    private void loadProfile(ImageView imageView, String path){

        Glide.with(UserProfile.this).load(path).asBitmap().centerCrop().into(profileCircular);
    }

    protected void showProfileDialog(String userId, String token){
        FragmentManager fm = getSupportFragmentManager();
        DialogProfile profileDialog = DialogProfile.newInstance(userId,token);
        profileDialog.show(fm,"activity_dialog_profile");
    }

}
