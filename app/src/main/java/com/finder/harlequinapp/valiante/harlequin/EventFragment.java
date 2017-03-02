package com.finder.harlequinapp.valiante.harlequin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.TimeUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.flaviofaria.kenburnsview.KenBurnsView;
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
import com.squareup.haha.perflib.Main;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.facebook.FacebookSdk.getCallbackRequestCodeOffset;


public class EventFragment extends Fragment {

    protected String[] ordering = {"nLike","date","eName"};
    public static final String RECYCLER_STATE = "recyclerViewLastState";
    private DatabaseReference myDatabase, mDatabaseLike, mDatabaseFavourites, eventLikeRef,dynamicEvents;
    private FirebaseUser currentUser;
    protected Integer orderingSelector = 1;
    protected  FirebaseRecyclerAdapter<DynamicData,MyEventViewHolder> eventAdapter;
    private boolean mProcessLike = false;
    private RecyclerView recyclerView;

    private Snackbar snackBar;
    private ValueEventListener likeListener;

    private SharedPreferences userData;
    private String userName,userId;
    private Integer userAge;
    private boolean isMale,isSingle;
    protected String citySelector = "Isernia";
    protected ValueEventListener likeUpdater;
    private Float pos = 0.0f;
    private Integer firstItem =0;
    private Parcelable savedRecyclerViewState, rcState;

    protected String current_city = "Isernia";


    protected LinearLayoutManager linearLayoutManager;

    //TODO pulsante like spammabile , spostare l'mProcess like nell' OnComplete della transaction


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView)inflater.inflate(R.layout.event_fragment_layout, container,false);


        getUserData();
        setHasOptionsMenu(true);
        snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), "LUL",Snackbar.LENGTH_SHORT);
        //per cambiare il background della snackbar
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));


        //UI
        eventLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child("Isernia");
        eventLikeRef.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child(current_city);
        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("favList");
        dynamicEvents=FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(citySelector);
        dynamicEvents.keepSynced(true);
        myDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        linearLayoutManager = new LinearLayoutManager((getActivity()));

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        //evita che la cardView "blinki"quando viene premuto il like
        recyclerView.getItemAnimator().setChangeDuration(0);

        return recyclerView;

    }

    //contiene RecyclerView
    public void onStart() {
        super.onStart();


        if(recyclerView.getAdapter()==null) {

            eventAdapter = new FirebaseRecyclerAdapter<DynamicData, MyEventViewHolder>(
                    DynamicData.class,
                    R.layout.event_card,
                    MyEventViewHolder.class, //ordering[((MainUserPage)getActivity()).orderingSelector]
                    dynamicEvents.orderByChild(ordering[orderingSelector])
            ) {
                @Override
                protected void populateViewHolder(final MyEventViewHolder viewHolder, final DynamicData model, int position) {
                    final String post_key = getRef(position).getKey();
                    viewHolder.setEventName(model.geteName());
                    viewHolder.setEventImage(getActivity(), model.getiPath());
                    viewHolder.revealFabInfo(computeMiddleAge(model.getLike(), model.getAge()),  //etàmedia
                            model.getLike(),                  //like totali
                            model.getMaLike(),                //like maschili
                            model.getfLike());                //like femminili
                    viewHolder.setCardDate(fromMillisToStringDate(model.getDate()));
                    viewHolder.setCardTime(fromMillisToStringTime(model.getDate()));
                    viewHolder.setCardPrice(model.getPrice(), model.getFree());
                    viewHolder.setPlaceName(model.getpName());

                    ValueEventListener likeCheckerListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(post_key).hasChild(currentUser.getUid())) {
                                viewHolder.setThumbDown();
                                mDatabaseLike.removeEventListener(this);
                            } else {
                                viewHolder.setThumbUp();
                                mDatabaseLike.removeEventListener(this);
                            }
                            mDatabaseLike.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    mDatabaseLike.addValueEventListener(likeCheckerListener);

                    viewHolder.chiudi.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            viewHolder.mFABRevealLayout.revealMainView();
                        }
                    });

                    //Onclick per il view generalizzato
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent goToEventPage = new Intent(getContext(), EventPage.class);
                            goToEventPage.putExtra("EVENT_ID", post_key);
                            goToEventPage.putExtra("USER_ID", ((MainUserPage) getActivity()).userId);
                            goToEventPage.putExtra("USER_ISMALE", ((MainUserPage) getActivity()).isMale);
                            goToEventPage.putExtra("USER_ISSINGLE", ((MainUserPage) getActivity()).isSingle);
                            goToEventPage.putExtra("USER_AGE", ((MainUserPage) getActivity()).userAge);

                            startActivity(goToEventPage);
                        }
                    });// END onclick view generalizzato

                    viewHolder.fabLike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mProcessLike = true;
                            likeProcess(post_key, model);
                        }
                    });


                }
            };
        }
        recyclerView.setAdapter(eventAdapter);
        if(rcState!=null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(rcState);
        }


    }// END OnStart



    @Override
    public void onResume() {
        super.onResume();

        recyclerView.setAdapter(eventAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerView.setAdapter(null);
        eventAdapter.cleanup();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        eventAdapter.cleanup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        eventAdapter.cleanup();
        recyclerView.setAdapter(null);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.logOutButton:
                logOut();
                break;
            case R.id.orderByJoiners:
                orderingSelector=0;
                recyclerView.setAdapter(null);
                onStart();

                break;
            case R.id.orderByTime:
                orderingSelector=1;
                recyclerView.setAdapter(null);
                onStart();

                break;
            case R.id.alphabetic:
                orderingSelector=2;
                recyclerView.setAdapter(null);
                onStart();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }



    //funziona
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //salva lo stato del recyclerView nel bundle
        outState.putParcelable(RECYCLER_STATE,linearLayoutManager.onSaveInstanceState());
        rcState = outState.getParcelable(RECYCLER_STATE);

   }



    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {

        if(savedInstanceState!=null) {

            //passa l'ultima istanza del recyclerView salvata in OnSavedInstance al layoutManager
            Parcelable savedRecyclerViewState = savedInstanceState.getParcelable(RECYCLER_STATE);
            if(savedRecyclerViewState!=null) {
                linearLayoutManager.onRestoreInstanceState(savedRecyclerViewState);
            }
            else{
            }
        }

        super.onViewStateRestored(savedInstanceState);

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

    protected void logOut(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Intent startingPage = new Intent(getActivity(), MainActivity.class);
        startActivity(startingPage);
    }

    protected Integer computeMiddleAge (Integer likes,Integer totalage){
        int middleAge;
        if(likes==0){
            middleAge = totalage;
            return middleAge;
        }
        else{
            middleAge = (int)totalage/likes;
            return middleAge;
        }
    }

    //rende la data in formato numero + MeseInTreCaratteri
    protected String readableDate (String eventDate){
        String[] splittedDate = eventDate.split("/");
        String eventDay = splittedDate[0];
        String eventMonth = new DateFormatSymbols().getMonths()[Integer.parseInt(splittedDate[1])-1];
        String date = eventDay+" "+eventMonth;
        return date;

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


    protected void getUserData(){
        userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        userName = userData.getString("USER_NAME","Name error");
        userAge = userData.getInt("USER_AGE",25);
        isSingle = userData.getBoolean("IS_SINGLE",true);
        isMale = userData.getBoolean("IS_MALE",true);
        userId = userData.getString("USER_ID","nope");

    }


    //imposta la notifica attraverso un delay
    protected void setPendingNotification (String eventId,String eventName,String userId, Long eventDate,String path){
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID",eventId);
        notificationIntent.putExtra("INTENT_ID",alarmId(eventName,userId,eventDate));
        notificationIntent.putExtra("IMAGE_PATH",path);
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
                        setPendingNotification(eventId,model.geteName(),uid,model.getDate(),model.getiPath());
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


}
