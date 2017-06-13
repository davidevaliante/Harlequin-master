package com.finder.harlequinapp.valiante.harlequin;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.login.LoginManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nex3z.notificationbadge.NotificationBadge;
import com.piotrek.customspinner.CustomSpinner;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RunnableFuture;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

public class MainUserPage extends AppCompatActivity {

    protected  DatabaseReference myDatabase;
    protected DatabaseReference mDatabaseLike;
    protected TabLayout tabs;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionButton fab;
    private CoordinatorLayout mCoordinatorLayout;
    private NavigationView navigationView;
    public Snackbar mSnackbar;
    private View navHeader;
    private TextView txtName,txtCity;
    private ImageView imgNavHeaderBg;
    private CircularImageView imgProfile;
    protected DatabaseReference userReference;
    protected  boolean isSingle = true;
    protected  boolean isMale = true;
    public  Integer userAge;
    private ValueEventListener mUserDataListener;
    protected  String userId = null;
    protected  User userClass;
    protected String myuserName = null;
    protected CollapsingToolbarLayout collapseLayout;
    protected CircularImageView collapseProfile;
    protected ImageView copertina;
    private SharedPreferences userData;
    private SharedPreferences.Editor editor;
    private Adapter adapter;
    private String current_city = "Isernia";
    protected String[] ordering = {"Data", "Numero partecipanti", "Ordine Alfabetico"};
    private TextView cityView, current_date;
    private CustomSpinner spinner;
    protected MyEventViewHolder viewHolder;
    protected Integer badgeCount = 4;
    protected NotificationBadge mBadge;
    protected ValueEventListener pendingRequestListener;
    private DatabaseReference pendingReference;

    /*
    protected  BroadcastReceiver tokenReceiver;
*/
    protected SharedPreferences.OnSharedPreferenceChangeListener tokenListener;


    private  final String urlNavHeaderBg = "http://www.magic4walls.com/wp-content/uploads/2015/01/abstract-colored-lines-red-material-design-triangles-lilac-background1.jpg";
    // index to identify current nav menu item



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_page);

        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.vector_burger_menu_24));
        FirebaseMessaging.getInstance().subscribeToTopic("android");

        //Inizializzazione Firebase per le notifiche
        pendingReference = FirebaseDatabase.getInstance().getReference().child("PendingRequest")
                                           .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        pendingReference.keepSynced(true);



        //userData shared preferences
        userData = getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE);
        editor = userData.edit();
        /*tokenListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equalsIgnoreCase("USER_TOKEN")){
                    FirebaseDatabase.getInstance().getReference().child("Token")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("user_token").setValue(sharedPreferences.getString(key,"nope"));
                    Toast.makeText(MainUserPage.this, "hey", Toast.LENGTH_SHORT).show();
                }
            }
        };
        userData.registerOnSharedPreferenceChangeListener(tokenListener);*/

        final Typeface tf = Typeface.createFromAsset(MainUserPage.this.getAssets(), "fonts/Hero.otf");



        copertina = (ImageView)findViewById(R.id.copertina);
        collapseProfile = (CircularImageView)findViewById(R.id.circular_collapse_profile);
        mBadge = (NotificationBadge)findViewById(R.id.badge);

        collapseLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar_userpage);
        collapseLayout.setCollapsedTitleTypeface(tf);
        collapseLayout.setExpandedTitleTypeface(tf);

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_content);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.toggle_opened,R.string.toggle_closed);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        //elementi del navigation Header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (CircularImageView) navHeader.findViewById(R.id.drawerAvatar);
        txtCity = (TextView)navHeader.findViewById(R.id.navigationCity);
        cityView = (TextView)findViewById(R.id.collapse_city);
        current_date =(TextView)findViewById(R.id.collapse_date);

        current_date.setText(fromMillisToStringDate(System.currentTimeMillis()));
        cityView.setText(current_city);

        //carica gli elementi del navigation Drawer
        loadNavigationHeader();
        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = myDatabase.child("Likes");
        myDatabase.keepSynced(true);




        //Viewpager per i fragment
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
            setupViewPager(viewPager,savedInstanceState);


        //tablayout per i fragment
        tabs = (TabLayout)findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case(0):
                        updatedToolbarTitle("Ciao "+myuserName);
                        break;
                    case(1):
                        updatedToolbarTitle("Mappa di "+current_city);
                        break;
                    case (2):
                        updatedToolbarTitle("I tuoi eventi preferiti");
                        break;
                    default:
                         updatedToolbarTitle("Ciao "+myuserName);


                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //per cambiare il font nel tablayout
        ViewGroup vg = (ViewGroup) tabs.getChildAt(0);
        changeFontInViewGroup(vg,"fonts/Hero.otf");
        mSnackbar= Snackbar.make(mCoordinatorLayout, "LUL",Snackbar.LENGTH_SHORT);


        //spinner
        spinner = (CustomSpinner)findViewById(R.id.spinner);
        spinner.initializeStringValues(ordering,"Ordina eventi");

        //Onclick per il navigation Drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_item1:
                        Intent changeProfile = new Intent(MainUserPage.this, EditProfile.class);
                        startActivity(changeProfile);
                        break;
                    case R.id.drawer_logout:
                        logOut();
                        break;
                    case R.id.nav_about_us:
                        Intent aboutUs = new Intent(MainUserPage.this, About.class);
                        startActivity(aboutUs);
                        break;


                    default:
                        return false;
                }
                return false;
            }
        });


        pendingRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                   Integer notificationNumber = (int) dataSnapshot.getChildrenCount();
                mBadge.setNumber(notificationNumber);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        pendingReference.addValueEventListener(pendingRequestListener);


        final Handler firebaseTokenHandler = new Handler();
        firebaseTokenHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //refresha il token
                UbiquoUtils.refreshCurrentUserToken(getApplication());
            }
        },5000);



    }//fine OnCreate


    @Override
    protected void onStart() {
        super.onStart();
        loadUserData();
    }

    //qui vengono rimossi i listener dell'activity
    @Override
    protected void onStop() {
        super.onStop();
        myDatabase.child("Users").child(userId).removeEventListener(mUserDataListener);

    }





    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager, Bundle savedInstanceState) {
        adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new EventFragment(), "Eventi");
        adapter.addFragment(new MapFragment(), "Mappe");
        adapter.addFragment(new ProposalFragment(), "Proposte");
        viewPager.setAdapter(adapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*userData.unregisterOnSharedPreferenceChangeListener(tokenListener);*/
        pendingReference.removeEventListener(pendingRequestListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    static class Adapter extends FragmentStatePagerAdapter{
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

    //per la libreria Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

    //menu del Navigation Drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //carica l'immagine nel drawer
    private void loadNavigationHeader(){
        Glide.with(MainUserPage.this)
                .load(urlNavHeaderBg)
                .placeholder(R.drawable.     //da cambiare
                        loading_placeholder) //da cambiare
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_error)
                .crossFade()
                .into(imgNavHeaderBg);
    }





    //cambia il titolo della toolbar, accessibile dai fragments
    protected void updatedToolbarTitle(String title){
        collapseLayout.setTitle(title);
    }

    protected void loadUserData()
    {
        //prende l'utente in firebase
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //se l'utente  correttamente loggato
        if(currentUser !=null) {
            userId = currentUser.getUid();
            if (!userId.isEmpty()) {
                userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            }
        }

        //listener per caricare dalla toolBar
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User myuser = dataSnapshot.getValue(User.class);
                userClass = myuser;
                myuserName = myuser.getUserName();
                final String relationshipStatus = myuser.getUserRelationship();
                 final String avatarUrl = myuser.getProfileImage();
                 String userName = myuser.getUserName()+" "+myuser.getUserSurname();
                String userCity = myuser.getUserCity();
                 String userGender = myuser.getUserGender();
                txtCity.setText(userCity);
                txtName.setText(userName);

                editor.putString("USER_NAME",myuserName);
                editor.putString("USER_CITY",userCity);


                //controlla il sesso

                if(userGender.equalsIgnoreCase("Uomo")){
                    //va bene così isMale è settato di default su maschio
                    editor.putBoolean("IS_MALE", true);
                }
                if(userGender.equalsIgnoreCase("Donna")){
                    editor.putBoolean("IS_MALE", false);
                    isMale = false;
                }
                /*
                Picasso.with(MainUserPage.this)
                        .load(avatarUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(collapseProfile, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext())
                                        .load(avatarUrl).into(collapseProfile);
                            }
                        });
                        */

                Glide.with(MainUserPage.this).load(avatarUrl)
                        .asBitmap()
                        .placeholder(R.drawable.loading_placeholder) //da cambiare
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.ic_error)
                        .into(collapseProfile);
                /*
                Picasso.with(MainUserPage.this)
                        .load(avatarUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imgProfile, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext())
                                        .load(avatarUrl).into(imgProfile);
                            }
                        });
                        */

                Glide.with(MainUserPage.this).load(avatarUrl)
                        .asBitmap()
                        .placeholder(R.drawable.loading_placeholder) //da cambiare
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.ic_error)
                        .into(imgProfile);


                //setta il saluto
                updatedToolbarTitle("Ciao "+myuserName );
                //imposta situazione sentimentale
                if(relationshipStatus.equalsIgnoreCase("Impegnato") ||
                   relationshipStatus.equalsIgnoreCase("Impegnata")){
                    isSingle = false;
                    editor.putBoolean("IS_SINGLE",false);
                }else{
                    editor.putBoolean("IS_SINGLE",true);
                }
                userAge = UbiquoUtils.getAgeIntegerFromString(myuser.getUserAge());
                editor.putInt("USER_AGE",userAge);
                editor.putString("USER_ID",FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.commit();

                imgProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UbiquoUtils.goToProfile(userId,true,MainUserPage.this);
                    }
                });
                collapseProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UbiquoUtils.goToProfile(userId,true,MainUserPage.this);
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        //reference per renderlo removibile
        mUserDataListener = userDataListener;
        //preleva nome dall'Auth personalizzando l'actionBar e i dati principali dell'utente
        myDatabase.child("Users").child(userId).addValueEventListener(userDataListener);

    }
    protected void logOut(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Intent startingPage = new Intent(MainUserPage.this, MainActivity.class);
        startActivity(startingPage);
        finish();
    }

    protected String fromMillisToStringDate(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
        String[] splittedDate = format.format(date).split("/");
        return splittedDate[0] + " " + splittedDate[1];
    }

    //Metodo da lanciare dopo 5 secondi per assicurarsi che il token sia fresco
    protected void refreshUserToken(final String userId){

        final String token = userData.getString("USER_TOKEN","nope");
        if(token.equalsIgnoreCase("nope")){
            FirebaseDatabase.getInstance().getReference().child("Token").child(userId).child("user_token").setValue(token);
        }

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(userId.length()!=0) {
            //reference al nodo utenti
            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

            //legge la useClass e aggiorna il token di registrazione
            ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User current_user = dataSnapshot.getValue(User.class);
                    current_user.setUserToken(token);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(userId).setValue(current_user);
                    userRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userRef.addValueEventListener(userListener);
        }
    }




}
