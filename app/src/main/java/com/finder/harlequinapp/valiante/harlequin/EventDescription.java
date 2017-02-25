package com.finder.harlequinapp.valiante.harlequin;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventDescription extends Fragment {

    private CoordinatorLayout mCoordinatorLayout;
    private ImageView eventImage;
    private TextView eEventDescription;
    private String eventId;
    private FirebaseUser currentUser;

    private TextView eventTitle;
    private DatabaseReference eventReference,mapDataReference;

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
    private ValueEventListener likeSetterListener;
    private String userName, userId;
    private String phone;
    private  boolean isMale,isSingle;
    private int userAge;
    String LOG = "INTENT_LOG:";
    private SharedPreferences userData;
    private EventPage.Adapter adapter;
    private TabLayout tabs;
    private String current_city="Isernia";
    public EventDescription() {
        // Required empty public constructor
    }
    protected String eventName,image,pName;
    protected Integer likes,engagedLikes,singleLikes,age,femaleLikes,maleLikes;
    protected Long date;
    private ValueEventListener eventDataListener,mapDataListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCoordinatorLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_event_description, container, false);


        eventTitle = (TextView)mCoordinatorLayout.findViewById(R.id.pEventTitle);

        eEventDescription = (TextView)mCoordinatorLayout.findViewById(R.id.pEventDescription);

        malePercentage = (TextView)mCoordinatorLayout.findViewById(R.id.malePercentage);
        femalePercentage = (TextView)mCoordinatorLayout.findViewById(R.id.femalePercentage);
        toolBarArrow = (ImageButton)mCoordinatorLayout.findViewById(R.id.backToUserPage);
        fab = (FloatingActionButton)mCoordinatorLayout.findViewById(R.id.likeFab);
        coordinatorLayout = (CoordinatorLayout)mCoordinatorLayout.findViewById(R.id.eventPageCoordinatorLayout);
        avarAge = (TextView)mCoordinatorLayout.findViewById(R.id.averageAge);
        singlesNumber = (TextView)mCoordinatorLayout.findViewById(R.id.singlesNumber);

        engagedNumber = (TextView)mCoordinatorLayout.findViewById(R.id.engagedNumber);
        //UIper le mapInfo
        placeName = (TextView)mCoordinatorLayout.findViewById(R.id.placeName);
        placeAdress = (TextView)mCoordinatorLayout.findViewById(R.id.placeAdress);
        placePhone = (TextView)mCoordinatorLayout.findViewById(R.id.placePhone);
        mapInfo =(LinearLayout)mCoordinatorLayout.findViewById(R.id.mapInfo);


        //TODO controllare se bisogna implementare una condizione IF in base aall'SDK per la toolbar su versioni precedenti
        //per cambiare il background della snackbar
        snackBar = Snackbar.make(mCoordinatorLayout, "LUL",Snackbar.LENGTH_SHORT);
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        //UIper le mapInfo
        placeName = (TextView)mCoordinatorLayout.findViewById(R.id.placeName);

        placeAdress = (TextView)mCoordinatorLayout.findViewById(R.id.placeAdress);
        placePhone = (TextView)mCoordinatorLayout.findViewById(R.id.placePhone);
        mapInfo =(LinearLayout)mCoordinatorLayout.findViewById(R.id.mapInfo);

        collapsingToolbar =  (CollapsingToolbarLayout)mCoordinatorLayout.findViewById(R.id.collapsing_toolbar);

        eventReference = FirebaseDatabase.getInstance().getReference()
                .child("Events")
                .child("Dynamic")
                .child(current_city)
                .child(((EventPage)getActivity()).eventId);
        eventReference.keepSynced(true);

        mapDataReference = FirebaseDatabase.getInstance().getReference()
                                                          .child("MapData")
                                                          .child(current_city)
                                                            .child(((EventPage)getActivity()).eventId);
        mapDataReference.keepSynced(true);


        eventDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DynamicData data = dataSnapshot.getValue(DynamicData.class);

                eventName = data.geteName();
                age=data.getAge();
                date=data.getDate();
                likes=data.getLike();
                engagedLikes=data.geteLike();
                singleLikes=data.getsLike();
                image=data.getiPath();
                femaleLikes=data.getfLike();
                maleLikes=data.getMaLike();
                pName=data.getpName();

                if(eventName.length()<=25) {
                    ((EventPage)getActivity()).collapsingToolbar.setTitle(eventName);
                }else{
                    String ellipsed = eventName.subSequence(0,22).toString()+"...";
                    ((EventPage)getActivity()). collapsingToolbar.setTitle(ellipsed);
                }

                Picasso.with(getContext())
                        .load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(((EventPage)getActivity()).eventImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                //va bene così non deve fare nulla
                            }
                            @Override
                            public void onError() {
                                Picasso.with(getContext()).load(image).into(((EventPage)getActivity()).eventImage);
                            }
                        });
                avarAge.setText("Età media dei partecipanti : "+age);
                engagedNumber.setText("Impegnati : "+engagedLikes);
                singlesNumber.setText("Singles : "+singleLikes);
                malePercentage.setText(getMalePercentage(likes,maleLikes)+"% Uomini");
                femalePercentage.setText(getFemalePercentage(likes,femaleLikes)+"% Donne");
                placeName.setText(pName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        eventReference.addValueEventListener(eventDataListener);

        mapDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MapInfo info = dataSnapshot.getValue(MapInfo.class);
                phone = info.getPhone();
                placePhone.setText(phone);
                placeAdress.setText("Via locale, numero Locale");

                mapDataReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mapDataReference.addValueEventListener(mapDataListener);

        return mCoordinatorLayout;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
        mapDataReference.removeEventListener(mapDataListener);
        eventReference.removeEventListener(eventDataListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapDataReference.removeEventListener(mapDataListener);
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
}
