package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
       private TextView eEventDescription;

    protected TextView eventTitle;
    private DatabaseReference eventReference,mapDataReference,staticDataReference;


    protected TextView malePercentage,femalePercentage,placeName,placeAdress,placePhone;



    private Snackbar snackBar;
    private CoordinatorLayout coordinatorLayout;
    protected TextView avarAge, singlesNumber, engagedNumber,joiners_number;

    private String phone;
    private String current_city;
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
    private static Double latitude,longitude;



    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences prefs = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        current_city = prefs.getString("USER_CITY","NA");


        mCoordinatorLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_event_description, container, false);

        map_btn = (TextView)mCoordinatorLayout.findViewById(R.id.map_btn);
        joiners_number = (TextView)mCoordinatorLayout.findViewById(R.id.joiners_number);
        joiners_fab = (FloatingActionButton)mCoordinatorLayout.findViewById(R.id.joiners_counter);
        eventTitle = (TextView)mCoordinatorLayout.findViewById(R.id.pEventTitle);
        eEventDescription = (TextView)mCoordinatorLayout.findViewById(R.id.pEventDescription);
        malePercentage = (TextView)mCoordinatorLayout.findViewById(R.id.malePercentage);
        femalePercentage = (TextView)mCoordinatorLayout.findViewById(R.id.femalePercentage);

        coordinatorLayout = (CoordinatorLayout)mCoordinatorLayout.findViewById(R.id.eventPageCoordinatorLayout);
        avarAge = (TextView)mCoordinatorLayout.findViewById(R.id.averageAge);
        singlesNumber = (TextView)mCoordinatorLayout.findViewById(R.id.singlesNumber);
        engagedNumber = (TextView)mCoordinatorLayout.findViewById(R.id.engagedNumber);
        //UIper le mapInfo
        placeName = (TextView)mCoordinatorLayout.findViewById(R.id.placeName);
        placeAdress = (TextView)mCoordinatorLayout.findViewById(R.id.placeAdress);
        placePhone = (TextView)mCoordinatorLayout.findViewById(R.id.placePhone);

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
                if(dataSnapshot.exists()) {
                    DynamicData data = dataSnapshot.getValue(DynamicData.class);

                    eventName = data.geteName();
                    age = data.getAge();
                    date = data.getDate();
                    likes = data.getLike();
                    engagedLikes = data.geteLike();
                    singleLikes = data.getsLike();
                    image = data.getiPath();
                    femaleLikes = data.getfLike();
                    maleLikes = data.getMaLike();
                    pName = data.getpName();

                    if (eventName.length() <= 25) {
                        ((EventPage) getActivity()).collapsingToolbar.setTitle(eventName);
                    } else {
                        String ellipsed = eventName.subSequence(0, 22).toString() + "...";
                        ((EventPage) getActivity()).collapsingToolbar.setTitle(ellipsed);
                    }

                    Picasso.with(getContext())
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(((EventPage) getActivity()).eventImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    //va bene così non deve fare nulla
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getContext()).load(image).into(((EventPage) getActivity()).eventImage);
                                }
                            });
                    if (likes != 0) {
                        avarAge.setText("Età media : " + Integer.valueOf(age / likes));
                    } else {
                        avarAge.setText("Età media : 0 anni");
                    }
                    engagedNumber.setText(engagedLikes + "  Impegnati");
                    singlesNumber.setText(singleLikes + "  Singles");
                    if (likes != 0) {
                        malePercentage.setText(getMalePercentage(likes, maleLikes) + " % Uomini");
                        femalePercentage.setText(getFemalePercentage(likes, femaleLikes) + " % Donne");
                    } else {
                        malePercentage.setText("0 % Uomini");
                        femalePercentage.setText("0 % Donne");
                    }
                    placeName.setText(pName);
                    if (likes != 1) {
                        joiners_number.setText(likes + " partecipanti");
                    } else {
                        joiners_number.setText(likes + " partecipante");
                    }
                    eventTitle.setText(eventName);
                }else{
                    getActivity().finish();
                }





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
                String adress = info.getAdress();
                if (phone.length() != 0) {
                    placePhone.setText(phone);
                } else {
                    placePhone.setText("Non disponibile");
                }
                if (adress.length() != 0) {
                    placeAdress.setText(adress);
                } else {
                    placeAdress.setText("Indirizzo non disponibile");
                }

                //latlng per l'intent che manda alla mappa
                latitude = info.getLat();
                longitude = info.getLng();


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

                if (((EventPage) getActivity()).names.size() == 0 && ((EventPage) getActivity()).numbers.size() == 0) {
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
                if(placePhone.getText().toString().trim().equalsIgnoreCase("Non disponibile")){
                    Toast.makeText(getContext(), "Numero telefonico non disponibile", Toast.LENGTH_SHORT).show();
                }else {
                    String number = placePhone.getText().toString().trim();
                    Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                    getActivity().startActivity(call);
                }
            }
        });

        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTargetLatLng(current_city);
            }
        });

        joiners_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                focusOnView();
            }
        });

        setVectorDrabables();



        return mCoordinatorLayout;
    }

    private void setVectorDrabables(){
        joiners_number.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(),R.drawable.ic_group_of_users_silhouette),null,null,null);
        avarAge.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(),R.drawable.age_purple_2),null,null,null);
        malePercentage.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(),R.drawable.male_purple_24), null,null,null);
        femalePercentage.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(),R.drawable.female_purple_24),null,null,null);
        singlesNumber.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(),R.drawable.unlock_24),null,null,null);
        engagedNumber.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(),R.drawable.locked_24),null,null,null);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
        mapDataReference.removeEventListener(mapDataListener);
        staticDataReference.removeEventListener(staticDataListener);
        eventReference.removeEventListener(eventDataListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapDataReference.removeEventListener(mapDataListener);
        staticDataReference.removeEventListener(staticDataListener);
        eventReference.removeEventListener(eventDataListener);    }

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

    private final void focusOnView(){
        mNestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                mNestedScrollView.smoothScrollTo(0, joinersData.getTop()-200);
            }
        });
    }

    private void getTargetLatLng(final String current_city){
        DatabaseReference cityReference = FirebaseDatabase.getInstance().getReference().child("CityMapByName").child(current_city);

        cityReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CityMapData mapData = dataSnapshot.getValue(CityMapData.class);
                Double Lat = mapData.getLatitude();
                Double Lon = mapData.getLongitude();

                Intent toMap = new Intent(getActivity(), BasicMap.class);
                toMap.putExtra("CURRENT_CITY",current_city);
                toMap.putExtra("CITY_LAT",Lat);
                toMap.putExtra("CITY_LNG",Lon);
                toMap.putExtra("SINGLE_MAP",true);
                toMap.putExtra("EVENT_ID",((EventPage)getActivity()).eventId);
                toMap.putExtra("LATITUDE",latitude);
                toMap.putExtra("LONGITUDE",longitude);

                startActivity(toMap);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
