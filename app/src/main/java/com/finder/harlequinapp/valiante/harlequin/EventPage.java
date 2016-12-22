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
import com.google.firebase.database.ValueEventListener;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EventPage extends AppCompatActivity  {


    private ImageView eventImage;
    private TextView eEventTitle,eEventDescription;
    private Context ctx;
    private String eTitle;
    private String eventId, eDescription, eImage;
    private DatabaseReference myEventReference,myLikeReference;
    private MaterialRippleLayout rippleHome,rippleChat,rippleProfile;
    private ImageButton likeButton,chatButton;
    private TextView favourites;
    private FirebaseUser currentUser;
    private String userId;
    private MaterialViewPager mViewPager;
    private ObservableScrollView mScrollView;
    private ImageView mImageView;
    private Integer mParallaxImageHeight;
    private ImageButton eventLike,eventChatRoom;
    private TextView eventLikeCounter,eventTitle,eventDesc;
    private DatabaseReference eventReference;
    private boolean isMale = true;
    private DatabaseReference userReference;
    private TextView malePercentage,femalePercentage,placeName,placeAdress,placePhone;
    private DatabaseReference mapInfoReference,likeReference;
    private LinearLayout mapInfo;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageButton toolBarArrow;
    private FloatingActionButton fab;
    private Boolean isLiked = false;
    private Snackbar snackBar;
    private CoordinatorLayout coordinatorLayout;
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

        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        likeReference = FirebaseDatabase.getInstance().getReference().child("Likes").child(eventId);

        eventReference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventId);
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //l'evento ha già il like dell'utente
                if(isLiked){
                    fab.setImageResource(R.drawable.white_star_empty_24);
                    isLiked=false;
                    snackBar.setText("Evento rimosso dai preferiti");
                    snackBar.show();
                }

                //l'evento non ha il like dell'evento
                else{
                    fab.setImageResource(R.drawable.white_star_full_24);
                    isLiked=true;
                    snackBar.setText("Evento aggiunto ai preferiti");
                    snackBar.show();
                }
            }
        });

        //setta stella piena o stella vuota inizialmente
        likeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userId)){
                    fab.setImageResource(R.drawable.white_star_full_24);
                    isLiked=true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mapInfoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(eventId)){
                    mapInfoReference.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            MapInfo currentEventInfo = dataSnapshot.getValue(MapInfo.class);
                            placeName.setText("Presso : "+currentEventInfo.getPlaceName());
                            placeAdress.setText(currentEventInfo.getPlaceLocation());
                            placePhone.setText("Telefono : "+currentEventInfo.getPlacePhone());
                            mapInfo.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        eventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event currentEvent = dataSnapshot.getValue(Event.class);

                eEventDescription.setText(currentEvent.getDescription());
                collapsingToolbar.setTitle(currentEvent.getEventName());
                //TODO mettere la policy offLine
                Picasso.with(getApplicationContext()).load(currentEvent.getEventImagePath())
                       .into(eventImage);
                Integer totalLikes = currentEvent.getLikes();
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
        });


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        } else {
            Toast.makeText(EventPage.this, "Problema di autenticazione", Toast.LENGTH_SHORT).show();
            finish();
        }

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if(currentUser.getUserGender().equalsIgnoreCase("Female")){
                    isMale = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
