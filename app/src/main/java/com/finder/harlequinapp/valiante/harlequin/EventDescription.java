package com.finder.harlequinapp.valiante.harlequin;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
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

import java.util.ArrayList;


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
    private DatabaseReference eventReference,mapDataReference,staticDataReference;

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
    private TextView avarAge, singlesNumber, engagedNumber,joiners_number;
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
    private ValueEventListener eventDataListener,mapDataListener,staticDataListener;
    private FloatingActionButton joiners_fab;
    private TextView map_btn;
    private NestedScrollView mNestedScrollView;
    private LinearLayout joinersData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCoordinatorLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_event_description, container, false);

        map_btn = (TextView)mCoordinatorLayout.findViewById(R.id.map_btn);
        joiners_number = (TextView)mCoordinatorLayout.findViewById(R.id.joiners_number);
        joiners_fab = (FloatingActionButton)mCoordinatorLayout.findViewById(R.id.joiners_counter);
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
        mNestedScrollView = (NestedScrollView)mCoordinatorLayout.findViewById(R.id.nested_scroll);
        joinersData = (LinearLayout)mCoordinatorLayout.findViewById(R.id.someText);


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

        staticDataReference = FirebaseDatabase.getInstance().getReference()
                                                            .child("Events")
                                                            .child("Static")
                                                            .child(current_city)
                                                            .child(((EventPage)getActivity()).eventId);
        staticDataReference.keepSynced(true);


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
                avarAge.setText("Età media : "+age);
                engagedNumber.setText(engagedLikes+"  Impegnati");
                singlesNumber.setText(singleLikes+"  Singles");
                if(likes !=0){
                malePercentage.setText(getMalePercentage(likes,maleLikes)+" % Uomini");
                femalePercentage.setText(getFemalePercentage(likes,femaleLikes)+" % Donne");
                }
                else{
                    malePercentage.setText("0 % Uomini");
                    femalePercentage.setText("0 % Donne");
                }
                placeName.setText(pName);
                if(likes!=1) {
                    joiners_number.setText(likes + " partecipanti");
                }else{
                    joiners_number.setText(likes+" partecipante");
                }
                eventTitle.setText(eventName);





            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        eventReference.addValueEventListener(eventDataListener);

        //TODO aggiungere alla mappa la via del locale
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

        staticDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StaticData data = dataSnapshot.getValue(StaticData.class);
                //setta la descrizione dell'evento
                eEventDescription.setText(data.getDesc());
                staticDataReference.removeEventListener(this);

                if(((EventPage)getActivity()).names.size()==0 && ((EventPage)getActivity()).numbers.size()==0) {
                    for (DataSnapshot postSnapshot : dataSnapshot.child("names").getChildren()) {
                        ((EventPage) getActivity()).names.add(postSnapshot.getValue(String.class));
                    }
                    for (DataSnapshot postSnapshot : dataSnapshot.child("numbers").getChildren()) {
                        ((EventPage) getActivity()).numbers.add(postSnapshot.getValue(String.class));
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        staticDataReference.addValueEventListener(staticDataListener);

        placePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = placePhone.getText().toString().trim();
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+number));
                getActivity().startActivity(call);
            }
        });

        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showOnMap = new Intent(getContext(),BasicMap.class);
                showOnMap.putExtra("SINGLE_MAP",true);
                showOnMap.putExtra("EVENT_ID",((EventPage)getActivity()).eventId);
                startActivity(showOnMap);
            }
        });

        joiners_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                focusOnView();
            }
        });



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
        staticDataReference.removeEventListener(staticDataListener);
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

    private final void focusOnView(){
        mNestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                mNestedScrollView.smoothScrollTo(0, joinersData.getTop()-200);
            }
        });
    }
}
