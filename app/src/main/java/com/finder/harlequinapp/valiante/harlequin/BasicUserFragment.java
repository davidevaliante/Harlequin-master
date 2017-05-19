package com.finder.harlequinapp.valiante.harlequin;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class BasicUserFragment extends Fragment {

    public static final String RECYCLER_STATE = "recyclerViewLastState";
    private RecyclerView favouriteEvents;
    private FirebaseUser currentUser;
    private DatabaseReference eventLikeRef;
    private FirebaseRecyclerAdapter favouritesEventAdapter,thumbnailAdapter;

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
    private List<String> mDatas;
    private RecyclerView userRecyclerView;
    private String userTitle;
    private FirebaseRecyclerAdapter userRecyclerAdapter;
    private DatabaseReference favouritesListRef,eventReference;
    private Boolean mProcessLike = false;

    public BasicUserFragment() {
        // Required empty public constructor
    }

    //restituisce un istanza di questo fragment con il titolo fornito
    public static BasicUserFragment getInstance(String title){
        BasicUserFragment fra = new BasicUserFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        fra.setArguments(bundle);
        return fra;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        userTitle = bundle.getString("title");
        favouritesListRef = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child("Users")
                .child(((UserProfile)getActivity()).userId);
        favouritesListRef.keepSynced(true);
        eventReference = FirebaseDatabase.getInstance().getReference()
                .child("Events")
                .child("Dynamic")
                .child("Isernia");
        eventReference.keepSynced(true);
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.keepSynced(true);
        eventLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child("Isernia");
        eventLikeRef.keepSynced(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_basic_user, container, false);


        userRecyclerView = (RecyclerView)v.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(userRecyclerView.getContext()));
        userRecyclerView.setHasFixedSize(true);
        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(userRecyclerView.getAdapter() == null) {
            userRecyclerAdapter = new FirebaseRecyclerAdapter<Boolean, FavouritesViewHolder>(
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

                                if(((UserProfile)getActivity()).ownProfile) {
                                    viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            mProcessLike = true;
                                            likeProcess(post_key, data);
                                        }
                                    });
                                }else{
                                    viewHolder.delete.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.white_eye));
                                }
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
        userRecyclerView.setAdapter(userRecyclerAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        userRecyclerAdapter.cleanup();
        userRecyclerView.setAdapter(null);
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
        return eventDate - TimeUnit.HOURS.toMillis(1);
    }

    protected void removeLikeListener(DatabaseReference myReference, ValueEventListener myListener){
        myReference.removeEventListener(myListener);

    }


}
