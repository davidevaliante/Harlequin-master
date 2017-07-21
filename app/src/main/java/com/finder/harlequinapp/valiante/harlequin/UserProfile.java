package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.design.widget.AppBarLayout;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.List;

import cn.hugeterry.coordinatortablayout.CoordinatorTabLayout;
import cn.hugeterry.coordinatortablayout.listener.LoadHeaderImagesListener;
import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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
    private DatabaseReference userRef, pendingReference;
    protected String profileUrl;
    protected String userName;
    private AdapterUser adapter;
    private TextView name,city,phone,mail,facebook;
    private AppBarLayout appBarLayout;
    private RelativeLayout facebookLayout,mailLayout,phoneLayout,followButton;
    private String facebookLink;
    private ImageView gendersymbol,engagedsymbol;
    private ValueEventListener pendingRequestListener,userFollowersListener,userListener;
    protected NotificationBadge mBadge;
    protected DatabaseReference userFollowersReference,userFollowingReference,topicReference, userReference,eventReference,pendingRequest;
    protected TextView followText;
    protected ImageView followIcon;
    protected CardView followCardView;
    private Boolean isAlreadyFollowing;
    private String uid,token;



    protected String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_coordinator);
        userId = getIntent().getExtras().getString("USER_ID");
        ownProfile = getIntent().getExtras().getBoolean("OWN_PROFILE");



        //solo da lollipop e superiore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(UserProfile.this,R.color.matte_blue));
        }

       /* Toolbar toolbar = (Toolbar)findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.vector_burger_menu_24));*/

        final Typeface tf = Typeface.createFromAsset(UserProfile.this.getAssets(), "fonts/Hero.otf");


        followButton = (RelativeLayout)findViewById(R.id.profileFollowButton);
        followCardView = (CardView)findViewById(R.id.followCardView);
        //nasconde pulsante se non è il proprio profilo
        if(ownProfile){
            followCardView.setVisibility(View.INVISIBLE);
        }
        followIcon = (ImageView)findViewById(R.id.followIcon);
        followText = (TextView)findViewById(R.id.followText);
        mBadge = (NotificationBadge)findViewById(R.id.profileBadge);
        engagedsymbol = (ImageView)findViewById(R.id.engagedSymbol);
        gendersymbol = (ImageView)findViewById(R.id.genderSymbol);
        facebookLayout = (RelativeLayout)findViewById(R.id.facebookLayout);
        phone = (TextView)findViewById(R.id.profileUserPhone);
        mail = (TextView)findViewById(R.id.profileUserMail);
        facebook = (TextView)findViewById(R.id.profileFacebook);
        city = (TextView)findViewById(R.id.userCity);
        appBarLayout = (AppBarLayout)findViewById(R.id.profileAppbar);
        name = (TextView)findViewById(R.id.userName);
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

        //Firebase references
        userFollowersReference = FirebaseDatabase.getInstance().getReference().child("Followers");
        userFollowingReference = FirebaseDatabase.getInstance().getReference().child("Following");
        topicReference = FirebaseDatabase.getInstance().getReference().child("Topics");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        eventReference = FirebaseDatabase.getInstance().getReference().child("Likes").child("Users");
        pendingRequest = FirebaseDatabase.getInstance().getReference().child("PendingRequest");
        pendingRequest.keepSynced(true);
        userFollowingReference.keepSynced(true);
        userFollowersReference.keepSynced(true);

        facebookLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(facebookLink!=null) {
                    Intent toFacebook = newFacebookIntent(getPackageManager(), facebookLink);
                    if(UbiquoUtils.isPackageExisted(UserProfile.this,"com.facebook.katana")) {
                        startActivity(toFacebook);
                    }else{
                        Toasty.error(UserProfile.this,"L'app di Facebook non è installa su questo dispositivo",Toast.LENGTH_SHORT,true).show();
                    }
                }else{
                    Toasty.info(UserProfile.this,"Questo utente non ha specificato il suo profilo Facebook",Toast.LENGTH_SHORT,true).show();
                }
            }
        });

        profileTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    appBarLayout.setExpanded(false,true);
                }
                if(tab.getPosition()==1){
                    appBarLayout.setExpanded(false,true);
                }
                if(tab.getPosition()==3){
                    appBarLayout.setExpanded(false,true);
                }
                if(tab.getPosition()==2){
                    appBarLayout.setExpanded(false,true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    uid = dataSnapshot.getKey();
                    token = user.getUserToken();
                    profileUrl = user.getProfileImage();
                    userName = user.getUserName() + " " + user.getUserSurname();
                    userRef.removeEventListener(this);
                    name.setText(userName);
                    city.setText(user.getUserCity());
                    phone.setText("Non disponibile");
                    String isSingle = user.getUserRelationship();
                    String isMale = user.getUserGender();
                    String user_mail = user.getUserEmail();
                    if (user_mail.equalsIgnoreCase("default@gmail.com") || user_mail.equalsIgnoreCase("default@facebook.com") || user_mail == null) {
                        mail.setText("Non disponibile");
                    } else {
                        mail.setText(user_mail);
                    }
                    final String facebook_link = user.getFacebookProfile();
                    if (facebook_link != null || !facebook_link.equalsIgnoreCase("NA")) {
                        facebook.setText("#" + user.getUserName() + user.getUserSurname());
                        facebookLink = facebook_link;
                    }

                    if (isMale.equalsIgnoreCase("Uomo")) {
                        gendersymbol.setImageDrawable(ContextCompat.getDrawable(UserProfile.this, R.drawable.male_24_blue));
                    } else {
                        gendersymbol.setImageDrawable(ContextCompat.getDrawable(UserProfile.this, R.drawable.female_24_purple));

                    }

                    if (isSingle.equalsIgnoreCase("Single")) {
                        engagedsymbol.setImageDrawable(ContextCompat.getDrawable(UserProfile.this, R.drawable.unlock_24));
                    } else {
                        engagedsymbol.setImageDrawable(ContextCompat.getDrawable(UserProfile.this, R.drawable.locked_24));

                    }

                    loadProfile(profileCircular, profileUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addValueEventListener(userListener);

        //Inizializzazione Firebase per le notifiche
        pendingReference = FirebaseDatabase.getInstance().getReference().child("PendingRequest")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        pendingReference.keepSynced(true);

        pendingRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Integer notificationNumber = (int) dataSnapshot.getChildrenCount();
                    mBadge.setNumber(notificationNumber);

                    //se non è il proprio profilo
                    //if (ownProfile != true) {

                    if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        //la richiesta è pending
                        followButton.setBackgroundColor(ContextCompat.getColor(UserProfile.this,R.color.light_grey));
                        followText.setText("Richiesta Inviata");
                        Drawable pending_icon = ContextCompat.getDrawable(UserProfile.this,R.drawable.vector_white_clock_18);
                        followIcon.setImageDrawable(pending_icon);
                    }
                    //}
                }else{
                    mBadge.clear();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        pendingReference.addValueEventListener(pendingRequestListener);

        userFollowersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        followButton.setBackgroundColor(ContextCompat.getColor(UserProfile.this,R.color.positive_accent));
                        followText.setText("Segui già");
                        Drawable check_icon = ContextCompat.getDrawable(UserProfile.this,R.drawable.white_check);
                        followIcon.setImageDrawable(check_icon);
                        isAlreadyFollowing=true;
                    }
                }else{
                    followButton.setBackground(ContextCompat.getDrawable(UserProfile.this,R.drawable.secondary_gradient));
                    followText.setText("Segui");
                    Drawable check_icon = ContextCompat.getDrawable(UserProfile.this,R.drawable.vector_right_arrow_18);
                    followIcon.setImageDrawable(check_icon);
                    isAlreadyFollowing=false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        userFollowersReference.child(userId).addValueEventListener(userFollowersListener);

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAlreadyFollowing){
                    //dati necessari
                    String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String targetId = uid;
                    SharedPreferences userData = getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
                    String senderToken = FirebaseInstanceId.getInstance().getToken();
                    String targetToken = token;
                    //costruttore Pending Notification
                    Long request_time = System.currentTimeMillis();
                    PendingFollowingRequest newPendingRequest = new PendingFollowingRequest(senderToken,senderId,targetToken,targetId,request_time);

                    //trigger della pending notification
                    UbiquoUtils.pendingNotificationTrigger(senderId,targetId,newPendingRequest);
                    isAlreadyFollowing = true;
                }else{
                    //dati necessari
                    String sender_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String receiver_uid = uid;
                    //rimozione delle interazioni social
                    UbiquoUtils.removeFollowInteractions(sender_uid,receiver_uid);

                    isAlreadyFollowing = false;
                }
            }
        });

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
        adapter.addFragment(new BasicUserFragment(), "Eventi");
        adapter.addFragment(new FollowersFragment(), "Followers");
        adapter.addFragment(new FollowingFragment(), "Following");

        if(ownProfile){
            adapter.addFragment(new FragmentRequests(),"Richieste");
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userListener!=null){
            userRef.removeEventListener(userListener);
        }
        if (userFollowersListener!=null){
            userFollowersReference.child(userId).removeEventListener(userFollowersListener);
        }
        if(pendingRequestListener!=null){
            pendingReference.removeEventListener(pendingRequestListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener!=null){
            userRef.removeEventListener(userListener);
        }
        if (userFollowersListener!=null){
            userFollowersReference.child(userId).removeEventListener(userFollowersListener);
        }
        if(pendingRequestListener!=null){
            pendingReference.removeEventListener(pendingRequestListener);
        }
    }

    private void loadProfile(ImageView imageView, String path){

        Glide.with(UserProfile.this).load(path).asBitmap().centerCrop().into(profileCircular);
    }

    protected void showProfileDialog(String userId, String token){
        FragmentManager fm = getSupportFragmentManager();
        DialogProfile profileDialog = DialogProfile.newInstance(userId,token);
        profileDialog.show(fm,"activity_dialog_profile");
    }

    //per la libreria Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    //metodo per costruire correttamente l'intent per aprire la pagina di facebook
    //se facebook non è installato restituisce il link per aprirlo dal browser
    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }



}
