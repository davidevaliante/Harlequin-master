package com.finder.harlequinapp.valiante.harlequin;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class FavouritesFragment extends Fragment {


    private RecyclerView favouriteEvents;
    private FirebaseUser currentUser;
    private DatabaseReference favouritesListRef,eventReference;
    private FirebaseRecyclerAdapter favouritesEventAdapter;
    private Boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike, myDatabase;
    private ValueEventListener likeListener;
    private Snackbar snackBar;
    private SharedPreferences userData;
    private String userName,userId;
    private Integer userAge;
    private boolean isMale,isSingle;
    private LinearLayoutManager mLinearLayoutManager;
    private String current_city = "Isernia";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.favourites_fragment_layout, container, false);

        snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), "LUL",Snackbar.LENGTH_SHORT);
        //per cambiare il background della snackbar
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));


        //inizializzazione di Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //prende i dati dalle shared references
        getUserData();

        //RecyclerView
        favouriteEvents = (RecyclerView)rootView.findViewById(R.id.favourite_recycler);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        favouriteEvents.setLayoutManager(mLinearLayoutManager);
        favouriteEvents.setHasFixedSize(true);



        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.keepSynced(true);

        favouritesListRef = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child("Users")
                .child(userId);
        favouritesListRef.keepSynced(true);

        eventReference = FirebaseDatabase.getInstance().getReference()
                                         .child("Events")
                                         .child("Dynamic")
                                         .child(current_city);
        eventReference.keepSynced(true);



        //si assicura che l'utente sia loggato ed inizializza una referenza al database che punta
        //ai preferiti ell'utente



        favouriteEvents.setAdapter(favouritesEventAdapter);

        Toolbar myToolbar = (Toolbar) rootView.findViewById(R.id.main_toolbar);
        ((MainUserPage)getActivity()).setSupportActionBar(myToolbar);
        setHasOptionsMenu(true);



        favouritesEventAdapter = new FirebaseRecyclerAdapter<Boolean, MyFavouriteViewHolder>(
                Boolean.class,
                R.layout.single_fav_event,
                MyFavouriteViewHolder.class,
                favouritesListRef
        ) {
            @Override
            protected void populateViewHolder(final MyFavouriteViewHolder viewHolder, Boolean model, int position) {
                final String post_key = getRef(position).getKey();

                ValueEventListener likeChecker = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(post_key)) {
                            DynamicData data = dataSnapshot.child(post_key).getValue(DynamicData.class);
                            Integer likes = data.getLike();
                            viewHolder.setAvatar(getActivity(), data.getiPath());
                            viewHolder.setName(data.geteName());
                            viewHolder.setPrice(data.getPrice());
                            viewHolder.setEventJoiners(likes);
                            viewHolder.setEventDate(fromMillisToStringDate(data.getDate()));
                            viewHolder.setMaleNumber(data.getMaLike());
                            viewHolder.setFemaleNumber(data.getfLike());
                            viewHolder.setSinglePercentage(likes,data.getsLike());
                            viewHolder.setEngagedPercentage(likes,data.geteLike());
                            viewHolder.setTime(fromMillisToStringTime(data.getDate()));
                        }

                        eventReference.removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };

                eventReference.addValueEventListener(likeChecker);
            }

        };


        favouriteEvents.setAdapter(favouritesEventAdapter);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        favouriteEvents.setAdapter(null);
        favouritesEventAdapter.cleanup();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        favouriteEvents.setAdapter(null);
        favouritesEventAdapter.cleanup();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        favouriteEvents.setAdapter(null);
        favouritesEventAdapter.cleanup();
    }

    //rimuove i listener per le funzionalità like
    protected void removeLikeListener(DatabaseReference myReference, ValueEventListener myListener){
        myReference.removeEventListener(myListener);

    }

    //imposta la notifica attraverso un delay
    protected void setPendingIntent (String eventId,String eventName,String eventCreator,String eventDate,String eventTime){
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID",eventId);
        notificationIntent.putExtra("INTENT_ID",buildAlarmId(eventName,eventCreator,eventDate,eventTime));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(getContext(),buildAlarmId(eventName,eventCreator,eventDate,eventTime) ,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, getDateDifference(eventDate,eventTime), broadcast);
    }

    //rimuove la notifica corrispondente all'evento
    protected void deletePendingIntent(String eventId,String eventName,String eventCreator,String eventDate,String eventTime){
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID",eventId);
        notificationIntent.putExtra("INTENT_ID",buildAlarmId(eventName,eventCreator,eventDate,eventTime));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(getContext(),buildAlarmId(eventName,eventCreator,eventDate,eventTime) ,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(broadcast);
    }

    //costruisce un identificativo unico da passare come Id all'alarm manager
    protected int buildAlarmId( String eventName, String creatorName, String eventDate,String eventTime){
        int uniqueId = 0;
        int dateDifference =(int)getDateDifference(eventDate,eventTime);
        try {
            if (!eventName.isEmpty() && !creatorName.isEmpty()) {
                int nameLength = eventName.length();
                int creatorNameLength = creatorName.length();
                uniqueId = dateDifference + creatorNameLength + nameLength;
                Log.d("UniqueId = ", "" + uniqueId);

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return uniqueId;
        }
    }

    //restituisce la data in formato millisecondi meno un ora
    protected long getDateDifference (String targetDate, String eventTime)  {
        //il tempo da sottrarre rispetto all'inizio dell'evento in millisecondi
        long oneHourInMilliseconds = TimeUnit.HOURS.toMillis(1);
        Log.d("HourConversion","1 hour = "+oneHourInMilliseconds);
        long timeInMilliseconds = 0;
        eventTime = eventTime+":00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date endDate = dateFormat.parse(targetDate+" "+eventTime);
            Log.d("END_TIME**","time"+endDate.getTime());
            timeInMilliseconds = endDate.getTime()-oneHourInMilliseconds;
            return timeInMilliseconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            return timeInMilliseconds;
        }
    }

    protected String readableDate (String eventDate){
        String[] splittedDate = eventDate.split("/");
        String eventDay = splittedDate[0];
        String eventMonth = new DateFormatSymbols().getMonths()[Integer.parseInt(splittedDate[1])-1];
        String date = eventDay+" "+eventMonth;
        return date;

    }

    protected void getUserData(){
        userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        userName = userData.getString("USER_NAME","Name error");
        userAge = userData.getInt("USER_AGE",25);
        isSingle = userData.getBoolean("IS_SINGLE",true);
        isMale = userData.getBoolean("IS_MALE",true);
        userId = userData.getString("USER_ID","nope");


    }

    //da millisecondi a data
    protected String fromMillisToStringDate(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
        String[] splittedDate = format.format(date).split("/");
        return splittedDate[0]+" "+splittedDate[1];
    }

    //da millisecondi ad orario
    protected String fromMillisToStringTime(Long time){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

}
