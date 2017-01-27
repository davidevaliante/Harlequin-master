package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
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

import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static java.security.AccessController.getContext;

public class EventPage extends AppCompatActivity  {


    private ImageView eventImage;
    private TextView eEventDescription;
    private String eventId;
    private FirebaseUser currentUser;
    private String userId;
    private TextView eventTitle;
    private DatabaseReference eventReference,mDatabaseLike,mDatabaseFavourites;
    private boolean isMale = true;
    private DatabaseReference userReference;
    private TextView malePercentage,femalePercentage,placeName,placeAdress,placePhone;
    private DatabaseReference mapInfoReference,likeReference,myDatabase;
    private LinearLayout mapInfo;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageButton toolBarArrow;
    private FloatingActionButton fab;
    private Boolean isLiked = false;
    private Snackbar snackBar;
    private CoordinatorLayout coordinatorLayout;
    private TextView avarAge, singlesNumber, engagedNumber;
    private boolean mProcessLike = false;
    private ValueEventListener likeListener;
    private ValueEventListener likeSetterListener,eventDataListener,mapInfoChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);

        eventId = getIntent().getExtras().getString("EVENT_ID");
        eventImage = (ImageView)findViewById(R.id.pEventImage);
        eventTitle = (TextView)findViewById(R.id.pEventTitle);
        eEventDescription = (TextView)findViewById(R.id.pEventDescription);
        malePercentage = (TextView)findViewById(R.id.malePercentage);
        femalePercentage = (TextView)findViewById(R.id.femalePercentage);
        toolBarArrow = (ImageButton)findViewById(R.id.backToUserPage);
        fab = (FloatingActionButton)findViewById(R.id.likeFab);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.eventPageCoordinatorLayout);
        avarAge = (TextView)findViewById(R.id.averageAge);
        singlesNumber = (TextView)findViewById(R.id.singlesNumber);
        engagedNumber = (TextView)findViewById(R.id.engagedNumber);

        //TODO controllare se bisogna implementare una condizione IF in base aall'SDK per la toolbar su versioni precedenti
        //per cambiare il background della snackbar
        snackBar = Snackbar.make(coordinatorLayout, "LUL",Snackbar.LENGTH_SHORT);
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        //UIper le mapInfo
        placeName = (TextView)findViewById(R.id.placeName);
        placeAdress = (TextView)findViewById(R.id.placeAdress);
        placePhone = (TextView)findViewById(R.id.placePhone);
        mapInfo =(LinearLayout)findViewById(R.id.mapInfo);
        mapInfo.setVisibility(View.GONE);
        collapsingToolbar =  (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        //firebase references
        likeReference = FirebaseDatabase.getInstance().getReference().child("Likes").child(eventId);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        eventReference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventId);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("favList");
        myDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        eventReference.keepSynced(true);
        mapInfoReference = FirebaseDatabase.getInstance().getReference().child("MapInfo");
        mapInfoReference.keepSynced(true);


        //imposta il font ed il colore per la toolbar per la collapsingToolBar
        final Typeface tf = Typeface.createFromAsset(EventPage.this.getAssets(), "fonts/Sansation_Bold.ttf");
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);
        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.pureWhite));
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.pureWhite));



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
                final String post_key = eventId;
                mProcessLike = true;
                likeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //se il tasto like è "spento"
                        if(mProcessLike) {
                            //se l'utente è presente fra i like del rispettivo evento
                            if (dataSnapshot.child(post_key).hasChild(MainUserPage.userId)) {
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

                                        removeLikeListener(FirebaseDatabase.getInstance().getReference().child("Likes"),likeListener);
                                        snackBar.setText("Evento rimosso dai preferiti");
                                        snackBar.show();
                                    }
                                });

                                mProcessLike = false;
                                //se l'utente non è presente nei like dell'evento
                            } else {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Likes")
                                        .child(post_key)
                                        .child(MainUserPage.userId).setValue(isMale);
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
                                        removeLikeListener(FirebaseDatabase.getInstance().getReference().child("Likes"),likeListener);
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

        }); //[END] fine OnClickListener

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        } else {
            Toast.makeText(EventPage.this, "Problema di autenticazione", Toast.LENGTH_SHORT).show();
            finish();
        }

        //setta stella piena o stella vuota nel fab
        likeSetterListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userId)){
                    fab.setImageResource(R.drawable.white_star_full_24);
                    isLiked=true;
                }
                else{
                    fab.setImageResource(R.drawable.white_star_empty_24);
                    isLiked=false;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        likeReference.addValueEventListener(likeSetterListener);

        //Listeners per le info google maps, aggiunti e rimossi OnStart
        mapInfoChecker = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(eventId)){
                    MapInfo currentEventInfo = dataSnapshot.child(eventId).getValue(MapInfo.class);
                    placeName.setText("Presso : " + currentEventInfo.getPlaceName());
                    placeAdress.setText(currentEventInfo.getPlaceLocation());
                    placePhone.setText("Telefono : " + currentEventInfo.getPlacePhone());
                    mapInfo.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mapInfoReference.addValueEventListener(mapInfoChecker);

        eventDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event currentEvent = dataSnapshot.getValue(Event.class);
                eEventDescription.setText(currentEvent.getDescription());
                collapsingToolbar.setTitle(currentEvent.getEventName());
                int totalAge = currentEvent.getTotalAge();
                int totalLikes = currentEvent.getLikes();
                singlesNumber.setText("Singles : "+currentEvent.getNumberOfSingles());
                engagedNumber.setText("Impegnati : "+currentEvent.getNumberOfEngaged());

                //se i like non sono zero
                if(totalLikes!=0){
                    avarAge.setText("Età media dei partecipanti : "+totalAge/totalLikes +" anni");
                }
                else{
                    avarAge.setText("Non ci sono ancora partecipanti");
                }
                //TODO mettere la policy offLine
                Picasso.with(getApplicationContext()).load(currentEvent.getEventImagePath())
                        .into(eventImage);

                if(totalLikes !=0) {
                    malePercentage.setText(getMalePercentage(currentEvent.getLikes(), currentEvent.getMaleFav()) + "%");
                    femalePercentage.setText(getFemalePercentage(currentEvent.getLikes(), currentEvent.getFemaleFav()) + "%");
                }
                else{
                    malePercentage.setText(0+ "%");
                    femalePercentage.setText(0+ "%");
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        eventReference.addValueEventListener(eventDataListener);


    }//fine di OnCreate

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        likeReference.removeEventListener(likeSetterListener);
        eventReference.removeEventListener(eventDataListener);
        mapInfoReference.removeEventListener(mapInfoChecker);
        eventReference.removeEventListener(eventDataListener);
    }

    private Float getMalePercentage (Integer totalLikes, Integer maleLikes){
        Float malePercentage ;
            malePercentage = Float.valueOf((100 * maleLikes) / totalLikes);
            return malePercentage;
    }

    private Float getFemalePercentage (Integer totalLikes, Integer femaleLikes){
        Float femalePercentage;
        femalePercentage = Float.valueOf((100*femaleLikes)/totalLikes);
        return femalePercentage;
    }

    //per usare i font personalizzati
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private int decreaseTotalAge(Integer totalAge){
        int updatedAge = totalAge - MainUserPage.userAge;
        return updatedAge;
    }

    private int increaseTotalAge(Integer totalAge){
        int updatedAge = totalAge + MainUserPage.userAge;
        return updatedAge;
    }

    protected void removeLikeListener(DatabaseReference myReference, ValueEventListener myListener){
        myReference.removeEventListener(myListener);

    }
}
