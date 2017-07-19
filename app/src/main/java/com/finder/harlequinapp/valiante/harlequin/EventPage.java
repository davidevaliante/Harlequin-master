package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
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
import com.google.firebase.FirebaseApp;
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
    private ValueEventListener eventDataListener,likeUpdater,likeCheckerListener;
    private DatabaseReference eventReference,eventLikeRef,mDatabaseLike,classRef,mapDataReference,staticDataReference, contactsName,contactsNumbers;
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
    private String userName;
    String userId;

    protected ArrayList<String> names = new ArrayList<>();
    protected ArrayList<String> numbers = new ArrayList<>();

    private  boolean isMale,isSingle;
    private int userAge;
    String LOG = "INTENT_LOG:";
    private SharedPreferences userData;
    private Adapter adapter;
    private TabLayout tabs;
    private String current_city;
    private DynamicData myEventClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);


        //recuperare dati dall'INTENT
        eventId = getIntent().getExtras().getString("EVENT_ID");
        SharedPreferences prefs = getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        current_city = prefs.getString("USER_CITY","NA");

        if(eventId == null){
            Toast.makeText(this, "Closing eventId = null", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(current_city == null || current_city.equalsIgnoreCase("NA")){
            Toast.makeText(this, "Closing current_city = null", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(eventId != null && current_city.equalsIgnoreCase("NA")){
            Toast.makeText(this, eventId+"  "+current_city, Toast.LENGTH_SHORT).show();

        }
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
        snackBar = Snackbar.make(coordinatorLayout, "" ,Snackbar.LENGTH_SHORT);
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        classRef = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(current_city).child(eventId);

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child(current_city);
        mDatabaseLike.keepSynced(true);

        eventLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child("Isernia");
        eventLikeRef.keepSynced(true);

        contactsName = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child("Isernia").child(eventId).child("names");
        contactsNumbers = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child("Isernia").child(eventId).child("numbers");

        //imposta il font ed il colore per la toolbar per la collapsingToolBar
        final Typeface tf = Typeface.createFromAsset(EventPage.this.getAssets(), "fonts/Hero.otf");
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);
        collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this,R.color.pureWhite));

        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(this,R.color.pureWhite));


        eventReference = FirebaseDatabase.getInstance().getReference()
                .child("Events")
                .child("Dynamic")
                .child(current_city)
                .child(eventId);
        //eventReference.keepSynced(true);

        mapDataReference = FirebaseDatabase.getInstance().getReference()
                .child("MapData")
                .child(current_city)
                .child(eventId);
        mapDataReference.keepSynced(true);

        staticDataReference = FirebaseDatabase.getInstance().getReference()
                .child("Events")
                .child("Static")
                .child(current_city)
                .child(eventId);
        staticDataReference.keepSynced(true);


        //Viewpager per i fragment
        ViewPager viewPager = (ViewPager)findViewById(R.id.event_viewpager);
        viewPager.setOffscreenPageLimit(0);
        setupViewPager(viewPager,savedInstanceState);






        //restituisce l'istanza necessaria della classe evento da rappresentare
        ValueEventListener getClass = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myEventClass = dataSnapshot.getValue(DynamicData.class);
                classRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        classRef.addListenerForSingleValueEvent(getClass);

        //tablayout per i fragment
        tabs = (TabLayout)findViewById(R.id.event_tabs);
        tabs.setupWithViewPager(viewPager);

        //per cambiare il font nel tablayout
        ViewGroup vg = (ViewGroup) tabs.getChildAt(0);
        changeFontInViewGroup(vg,"fonts/Hero.otf");

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


        //controlla se mettere stella pienao  stella vuota
         likeCheckerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(eventId).hasChild(userId)) {
                    fab.setImageResource(R.drawable.white_star_full_24);

                } else {
                    fab.setImageResource(R.drawable.white_star_empty_24);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseLike.addValueEventListener(likeCheckerListener);

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
                mProcessLike=true;
                likeProcess(eventId,myEventClass);
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
       // Toast.makeText(this, ""+names.get(0), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseLike.removeEventListener(likeCheckerListener);

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

    protected void likeProcess(final String eventId, final DynamicData model){

        if(mProcessLike){
            final String uid = this.getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getString("USER_ID",userId);
            final Boolean male = this.getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getBoolean("IS_MALE",isMale);
            final Boolean single = this.getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE)
                    .getBoolean("IS_SINGLE",isSingle);
            final Integer age = this.getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getInt("USER_AGE",userAge);
            likeUpdater = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(eventId).hasChild(uid)){
                        //vanno rimosse le cose e diminuiti i contatori
                        eventLikeRef.child(eventId).child(uid).removeValue();
                        removePendingNotification(eventId,model.geteName(),uid,model.getDate());
                        //rimuove l'iddell'utente dai like dell'utente
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Likes")
                                .child("Events")
                                .child(current_city)
                                .child(eventId)
                                .child(uid).removeValue();
                        //rimuove id evento ai like dell'utente
                        FirebaseDatabase.getInstance().getReference()
                                .child("Likes")
                                .child("Users")
                                .child(userId)
                                .child(eventId)
                                .removeValue();
                        FirebaseDatabase.getInstance().getReference()
                                .child("MapData")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                MapInfo map = mutableData.getValue(MapInfo.class);
                                if(map==null){
                                    return Transaction.success(mutableData);
                                }
                                map.setLikes(map.getLikes()-1);
                                map.setTotalAge(map.getTotalAge()-age);
                                mutableData.setValue(map);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                //nientedi speciale
                            }
                        });

                        FirebaseDatabase.getInstance().getReference()
                                .child("Events")
                                .child("Dynamic")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                DynamicData data = mutableData.getValue(DynamicData.class);
                                if(data==null){
                                    return Transaction.success(mutableData);
                                }
                                //update incondizionali
                                data.setLike(data.getLike()-1);
                                data.setnLike(data.getnLike()+1);
                                data.setAge(data.getAge()-age);

                                //se è uomo
                                if(male){
                                    data.setMaLike(data.getMaLike()-1);
                                    if(single){
                                        data.setsLike(data.getsLike()-1);
                                    }else{
                                        data.seteLike(data.geteLike()-1);
                                    }
                                }
                                //se è donna
                                else{
                                    data.setfLike(data.getfLike()-1);
                                    if(single){
                                        data.setsLike(data.getsLike()-1);
                                    }else{
                                        data.seteLike(data.geteLike()-1);
                                    }
                                }

                                mutableData.setValue(data);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                removeLikeListener(eventLikeRef,likeUpdater);
                                snackBar.setText("Evento rimosso dai preferiti");
                                snackBar.show();
                                mProcessLike=false;
                            }
                        });
                        mProcessLike=false;
                    }
                    else{
                        eventLikeRef.child(eventId).child(uid).setValue(true);
                        setPendingNotification(eventId,model.geteName(),uid,model.getDate());
                        //aggiunge id utente ai like dell'evento
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Likes")
                                .child("Events")
                                .child(current_city)
                                .child(eventId)
                                .child(uid).setValue(true);
                        //aggiunge id evento ai like dell'utente
                        FirebaseDatabase.getInstance().getReference()
                                .child("Likes")
                                .child("Users")
                                .child(userId)
                                .child(eventId)
                                .setValue(true);

                        FirebaseDatabase.getInstance().getReference()
                                .child("MapData")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                MapInfo map = mutableData.getValue(MapInfo.class);
                                if(map==null){
                                    return Transaction.success(mutableData);
                                }
                                map.setLikes(map.getLikes()+1);
                                map.setTotalAge(map.getTotalAge()+age);
                                mutableData.setValue(map);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                //nientedi speciale
                            }
                        });

                        //Transaction primaria
                        FirebaseDatabase.getInstance().getReference()
                                .child("Events")
                                .child("Dynamic")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                DynamicData data = mutableData.getValue(DynamicData.class);
                                if(data==null){
                                    return Transaction.success(mutableData);
                                }
                                //update incondizionali
                                data.setLike(data.getLike()+1);
                                data.setnLike(data.getnLike()-1);
                                data.setAge(data.getAge()+age);

                                //se è uomo
                                if(male){
                                    data.setMaLike(data.getMaLike()+1);
                                    if(single){
                                        data.setsLike(data.getsLike()+1);
                                    }else{
                                        data.seteLike(data.geteLike()+1);
                                    }
                                }
                                //se è donna
                                else{
                                    data.setfLike(data.getfLike()+1);
                                    if(single){
                                        data.setsLike(data.getsLike()+1);
                                    }else{
                                        data.seteLike(data.geteLike()+1);
                                    }
                                }

                                mutableData.setValue(data);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                removeLikeListener(eventLikeRef,likeUpdater);
                                snackBar.setText("Evento aggiunto ai preferiti");
                                snackBar.show();
                                mProcessLike=false;
                            }
                        });


                        mProcessLike=false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            eventLikeRef.addListenerForSingleValueEvent(likeUpdater);
        }
    }

    //imposta la notifica attraverso un delay
    protected void setPendingNotification (String eventId,String eventName,String userId, Long eventDate){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID",eventId);
        notificationIntent.putExtra("INTENT_ID",alarmId(eventName,userId,eventDate));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(this,alarmId(eventName,userId,eventDate) ,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, oneHourDifference(eventDate), broadcast);
    }

    protected void removePendingNotification  (String eventId,String eventName,String userId, Long eventDate){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID",eventId);
        notificationIntent.putExtra("INTENT_ID",alarmId(eventName,userId,eventDate));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(this,alarmId(eventName,userId,eventDate) ,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(broadcast);
    }
    protected int alarmId( String eventName, String userId, Long eventDate){

        //TODO da migliorare
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(eventDate);
        int uniqueId = 0;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        try {
            if (!eventName.isEmpty() && !userId.isEmpty()) {

                int nameLength = eventName.length();
                int creatorNameLength = userId.length();
                if(nameLength%2==0) {
                    uniqueId = nameLength+creatorNameLength+day+hour+minute;
                    Log.d("UniqueId = ", "" + uniqueId);
                }
                else{
                    uniqueId=nameLength*creatorNameLength+day+hour+minute;
                    Log.d("UniqueId = ", "" + uniqueId);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return uniqueId;
        }
    }

    protected Long oneHourDifference(Long eventDate){
        return eventDate -TimeUnit.HOURS.toMillis(1);
    }

    protected void showProfileDialog(String userId, String token){
        FragmentManager fm = getSupportFragmentManager();
        DialogProfile profileDialog = DialogProfile.newInstance(userId,token);
        profileDialog.show(fm,"activity_dialog_profile");
    }

    private Integer getMalePercentage (Integer totalLikes, Integer maleLikes){
        Integer malePercentage ;

        malePercentage = Integer.valueOf((100 * maleLikes) / totalLikes);
        return malePercentage;
    }

    private Integer getFemalePercentage (Integer totalLikes, Integer femaleLikes){
        Integer femalePercentage;
        femalePercentage = Integer.valueOf((100*femaleLikes)/totalLikes);
        return femalePercentage;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
