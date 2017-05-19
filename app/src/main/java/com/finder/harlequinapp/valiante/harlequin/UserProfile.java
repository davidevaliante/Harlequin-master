package com.finder.harlequinapp.valiante.harlequin;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cn.hugeterry.coordinatortablayout.CoordinatorTabLayout;
import cn.hugeterry.coordinatortablayout.listener.LoadHeaderImagesListener;

public class UserProfile extends AppCompatActivity {

    private int[] mImageArray, mColorArray;
    private CoordinatorTabLayout userLayout;
    private ArrayList<Fragment> myFragments;
    private final String[] fragmentTitles = {"Followers","Following","Eventi"};
    private ViewPager userViewPager;
    private int[] colorArray,imageArray;
    protected boolean ownProfile = true;
    private DatabaseReference userRef;
    protected String profileUrl;
    protected String userName;
    private ValueEventListener userListener;

    protected String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userId = getIntent().getExtras().getString("USER_ID");
        ownProfile = getIntent().getExtras().getBoolean("OWN_PROFILE");




        initFragments();
        initViewPager();

        colorArray = new int[]{R.color.colorPrimary,R.color.colorPrimary,R.color.colorPrimary};
        imageArray = new int[]{R.drawable.logo,
                R.drawable.logo,
                R.drawable.logo,
                R.drawable.logo};

        userLayout = (CoordinatorTabLayout)findViewById(R.id.userTabLayout);
        userLayout.getTabLayout().setSelectedTabIndicatorColor(Color.parseColor("#18FFFF"));
        userLayout.setLoadHeaderImagesListener(new LoadHeaderImagesListener() {
            @Override
            public void loadHeaderImages(ImageView imageView, TabLayout.Tab tab) {
                loadProfile(imageView,profileUrl);
            }
        });

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profileUrl = user.getProfileImage();
                userName = user.getUserName()+" "+user.getUserSurname();
                userRef.removeEventListener(this);

                userLayout.setTitle(userName).setBackEnable(true).setImageArray(imageArray,colorArray).setupWithViewPager(userViewPager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addValueEventListener(userListener);












    }

    private void initFragments(){
        myFragments = new ArrayList<>();
        //cicla nell'array fornito e li aggiunge all'array
            myFragments.add(FollowersFragment.getInstance("Followers"));
            myFragments.add(BasicUserFragment.getInstance("Following"));
            myFragments.add(BasicUserFragment.getInstance("Eventi"));

    }

    private void initViewPager(){
        userViewPager = (ViewPager)findViewById(R.id.userViewPager);
        userViewPager.setOffscreenPageLimit(3);
        userViewPager.setAdapter(new UserPageAdapter(getSupportFragmentManager(),myFragments,fragmentTitles));
    }

    private void loadProfile(ImageView imageView, String path){

        Glide.with(UserProfile.this).load(path).asBitmap().centerCrop().into(imageView);
    }

    protected void showProfileDialog(String userId, String token){
        FragmentManager fm = getSupportFragmentManager();
        DialogProfile profileDialog = DialogProfile.newInstance(userId,token);
        profileDialog.show(fm,"activity_dialog_profile");
    }

}
