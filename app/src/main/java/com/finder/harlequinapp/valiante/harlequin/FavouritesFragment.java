package com.finder.harlequinapp.valiante.harlequin;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class FavouritesFragment extends Fragment {

    public static final String RECYCLER_STATE = "recyclerViewLastState";
    private RecyclerView favouriteEvents;
    private FirebaseUser currentUser;
    private DatabaseReference favouritesListRef,eventReference,eventLikeRef;
    private FirebaseRecyclerAdapter favouritesEventAdapter,thumbnailAdapter;
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
    private final Integer TIITLE_LIMIT = 37;
    private ValueEventListener likeUpdater;
    private Parcelable rcState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
        eventLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child("Isernia");
        eventLikeRef.keepSynced(true);

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



        Toolbar myToolbar = (Toolbar) rootView.findViewById(R.id.main_toolbar);
        ((MainUserPage)getActivity()).setSupportActionBar(myToolbar);



        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //salva lo stato del recyclerView nel bundle
        outState.putParcelable(RECYCLER_STATE,mLinearLayoutManager.onSaveInstanceState());
        rcState = outState.getParcelable(RECYCLER_STATE);
    }



    @Override
    public void onResume() {
        super.onResume();
        favouriteEvents.setAdapter(thumbnailAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(favouriteEvents.getAdapter() == null) {
            thumbnailAdapter = new FirebaseRecyclerAdapter<Boolean, FavouritesViewHolder>(
                    Boolean.class,
                    R.layout.fav_thumbnail,
                    FavouritesViewHolder.class,
                    favouritesListRef
            ) {
                @Override
                protected void populateViewHolder(final FavouritesViewHolder viewHolder, Boolean model, int position) {
                    final String post_key = getRef(position).getKey();

                    ValueEventListener likeChecker = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(post_key)) {
                                final DynamicData data = dataSnapshot.child(post_key).getValue(DynamicData.class);
                                Integer likes = data.getLike();
                                viewHolder.setThumbImage(getActivity(), data.getiPath());
                                viewHolder.setThumbTitle(data.geteName());
                                viewHolder.setJoiners(data.getLike());
                                viewHolder.setThumbTime(data.getDate());
                                viewHolder.setpName(data.getpName());
                                viewHolder.setAgeField(data.getLike(),data.getAge() );

                                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mProcessLike = true;
                                        likeProcess(post_key, data);
                                    }
                                });
                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent toEventPage = new Intent(getActivity(),EventPage.class);
                                        toEventPage.putExtra("EVENT_ID",post_key);
                                        startActivity(toEventPage);
                                    }
                                });

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
        }
        favouriteEvents.setAdapter(thumbnailAdapter);
        if(rcState!=null){
            favouriteEvents.getLayoutManager().onRestoreInstanceState(rcState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        favouriteEvents.setAdapter(null);
        thumbnailAdapter.cleanup();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        favouriteEvents.setAdapter(null);
        thumbnailAdapter.cleanup();
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

    protected void likeProcess(final String eventId, final DynamicData model){

        if(mProcessLike){
            final String uid = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getString("USER_ID",userId);
            final Boolean male = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getBoolean("IS_MALE",isMale);
            final Boolean single = getActivity().getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE)
                    .getBoolean("IS_SINGLE",isSingle);
            final Integer age = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
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
                                data.setLike(data.geteLike()+1);
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
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID",eventId);
        notificationIntent.putExtra("INTENT_ID",alarmId(eventName,userId,eventDate));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(getContext(),alarmId(eventName,userId,eventDate) ,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, oneHourDifference(eventDate), broadcast);
    }

    protected void removePendingNotification  (String eventId,String eventName,String userId, Long eventDate){
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID",eventId);
        notificationIntent.putExtra("INTENT_ID",alarmId(eventName,userId,eventDate));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(getContext(),alarmId(eventName,userId,eventDate) ,
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

}
