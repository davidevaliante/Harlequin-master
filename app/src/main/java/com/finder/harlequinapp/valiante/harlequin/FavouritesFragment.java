package com.finder.harlequinapp.valiante.harlequin;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class FavouritesFragment extends Fragment {


    private RecyclerView favouriteEvents;
    private FirebaseUser currentUser;
    private DatabaseReference favouritesListRef;
    private String userId;
    private FirebaseRecyclerAdapter favouritesEventAdapter;
    private Boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike, myDatabase;
    private boolean isMale = MainUserPage.isMale;
    private ValueEventListener likeListener;
    private Snackbar snackBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.favourites_fragment_layout, container, false);

        snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), "LUL",Snackbar.LENGTH_SHORT);
        //per cambiare il background della snackbar
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        favouriteEvents = (RecyclerView)rootView.findViewById(R.id.favourite_recycler);
        favouriteEvents.setHasFixedSize(true);
        favouriteEvents.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.keepSynced(true);
        //inizializzazione di Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();



        //si assicura che l'utente sia loggato ed inizializza una referenza al database che punta
        //ai preferiti ell'utente
        if (userId.isEmpty()){

        }
        else{
            favouritesListRef = FirebaseDatabase.getInstance().getReference().child("favList").child(userId);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //TODO ogni fragment non prende l'adapter dal fragment stesso ma dalla classe di riferimento
        favouritesEventAdapter = new FirebaseRecyclerAdapter<Event,MainUserPage.FavouritesViewHolder>(
                Event.class,
                R.layout.single_fav_event,
                MainUserPage.FavouritesViewHolder.class,
                favouritesListRef.orderByChild("eventName")
        ) {
            @Override
            protected void populateViewHolder(MainUserPage.FavouritesViewHolder viewHolder, final Event model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setAvatar(getApplicationContext(), model.getEventImagePath());
                viewHolder.setEventDate(model.getEventDate());
                viewHolder.setTime(model.getEventTime());
                viewHolder.setName(model.getEventName());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent goToEventPage = new Intent (getActivity(),EventPage.class);
                        goToEventPage.putExtra("EVENT_ID",post_key);
                        startActivity(goToEventPage);
                    }
                });


                //like && dislike
                viewHolder.cardLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;
                        likeListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //se il tasto like è "spento"
                                if(mProcessLike) {
                                    //se l'utente è presente fra i like del rispettivo evento
                                    if (dataSnapshot.child(post_key).hasChild(MainUserPage.userId)) {

                                        //rimuove la notifica
                                        deletePendingIntent(post_key,                //id dell'evento
                                                model.getEventName(),    //nome dell'evento
                                                model.getCreatorName(),  //nome del creatore
                                                model.getEventDate(),    //data evento
                                                model.getEventTime()     //orario evento
                                        );
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("favList")
                                                .child(MainUserPage.userId)
                                                .child(post_key)
                                                .removeValue();

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Likes")
                                                .child(post_key)
                                                .child(MainUserPage.userId).removeValue();
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Events")
                                                .child(post_key).runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                Event event = mutableData.getValue(Event.class);
                                                if(event == null){
                                                    return Transaction.success(mutableData);
                                                }

                                                //like utente maschio
                                                if (isMale){
                                                    event.likes--;
                                                    event.rLikes++;
                                                    event.maleFav--;
                                                    event.totalAge = event.totalAge - MainUserPage.userAge;
                                                    //maschio e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles--;
                                                    }

                                                    //maschio e impegnato
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged--;
                                                    }

                                                }
                                                //like utente donna
                                                if(!isMale){
                                                    event.likes--;
                                                    event.rLikes++;
                                                    event.femaleFav--;
                                                    event.totalAge = event.totalAge - MainUserPage.userAge;
                                                    //donna e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles--;
                                                    }

                                                    //donna e impegnata
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged--;
                                                    }
                                                }
                                                mutableData.setValue(event);
                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                removeLikeListener(FirebaseDatabase.getInstance().getReference()
                                                        .child("Likes"),likeListener);

                                                snackBar.setText("Evento rimosso dai preferiti");
                                                snackBar.show();
                                            }
                                        });

                                        mProcessLike = false;
                                        //se l'utente non è presente nei like dell'evento
                                    } else {
                                        //aggiunge la notifica
                                        setPendingIntent(post_key,                //id dell'evento
                                                model.getEventName(),    //nome dell'evento
                                                model.getCreatorName(),  //nome del creatore
                                                model.getEventDate(),    //data evento
                                                model.getEventTime()     //orario evento
                                        );
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("favList")
                                                .child(MainUserPage.userId)
                                                .child(post_key)
                                                .setValue(model);
                                        //aggiunge Id e sesso ai like dell'evento
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Likes")
                                                .child(post_key)
                                                .child(MainUserPage.userId).setValue(isMale);
                                        //Transaction per incrementare i contatori dei like
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Events")
                                                .child(post_key).runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                Event event = mutableData.getValue(Event.class);
                                                //handler per nullpointer
                                                if(event == null){
                                                    return Transaction.success(mutableData);
                                                }

                                                //like utente maschio
                                                if (isMale){
                                                    event.likes++;
                                                    event.rLikes--;
                                                    event.maleFav++;
                                                    event.totalAge = event.totalAge + MainUserPage.userAge;
                                                    //maschio e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles++;
                                                    }
                                                    //maschio e impegnato
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged++;
                                                    }
                                                }
                                                //like utente donna
                                                if(!isMale){
                                                    event.likes++;
                                                    event.rLikes--;
                                                    event.femaleFav++;
                                                    event.totalAge = event.totalAge + MainUserPage.userAge;
                                                    //donna e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles++;
                                                    }
                                                    //donna e impegnata
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged++;
                                                    }
                                                }
                                                mutableData.setValue(event);
                                                return Transaction.success(mutableData);
                                            }
                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                removeLikeListener(FirebaseDatabase.getInstance().getReference()
                                                        .child("Likes"),likeListener);
                                                snackBar.setText("Evento aggiunto ai preferiti");
                                                snackBar.show();
                                            }
                                        });

                                        mProcessLike = false;
                                    }
                                }//[END]if mProcessLike
                            }//[END] DataSnapshot

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}

                        }; //[END] fine ValueEventListener

                        FirebaseDatabase.getInstance().getReference().child("Likes").addValueEventListener(likeListener);
                    }
                });

            }
        };
        favouriteEvents.setAdapter(favouritesEventAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
}
