package com.finder.harlequinapp.valiante.harlequin;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.rangebar.RangeBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;


public class MapFragment extends Fragment {

    CoordinatorLayout map_frag;
    RangeBar joinersRangeBar,ageRangeBar;
    Integer maxJoiners=99999;
    Integer minJoiners=0;
    Integer minAge=0;
    Integer maxAge=99;
    Integer hoursLimiting = 0;
    TextView maxDisplay,minDisplay,ageMinText,ageMaxText;
    RadioRealButtonGroup radioGroup;
    RadioRealButton defaultHour,second,third,fourth;
    RelativeLayout map;
    protected String current_city;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        map_frag = (CoordinatorLayout)inflater.inflate(R.layout.fragment_map,container,false);

        current_city = ((MainUserPage)getActivity()).current_city;
        map = (RelativeLayout)map_frag.findViewById(R.id.layoutButton);
        maxDisplay = (TextView)map_frag.findViewById(R.id.maxText);
        minDisplay = (TextView)map_frag.findViewById(R.id.minText);
        ageMinText = (TextView)map_frag.findViewById(R.id.ageMinText);
        ageMaxText = (TextView)map_frag.findViewById(R.id.ageMaxText);

        radioGroup = (RadioRealButtonGroup)map_frag.findViewById(R.id.radioGroup);
        defaultHour = (RadioRealButton)map_frag.findViewById(R.id.defaultHour);
        second = (RadioRealButton)map_frag.findViewById(R.id.twelveHours);
        third = (RadioRealButton)map_frag.findViewById(R.id.twentyfourHours);
        fourth = (RadioRealButton)map_frag.findViewById(R.id.fourtyfourHours);



        joinersRangeBar = (RangeBar)map_frag.findViewById(R.id.joiners_rangebar);
        ageRangeBar = (RangeBar)map_frag.findViewById(R.id.ageRangebar);



        map.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                /*Intent toMap = new Intent(getActivity(), BasicMap.class);
                toMap.putExtra("MIN_JOINERS",minJoiners);
                toMap.putExtra("MAX_JOINERS",maxJoiners);
                toMap.putExtra("MIN_AGE",minAge);
                toMap.putExtra("MAX_AGE",maxAge);
                toMap.putExtra("HOURS_LIMIT",hoursLimiting);
                toMap.putExtra("CURRENT_CITY",current_city);
                startActivity(toMap);*/
                getTargetLatLng(current_city);
            }
        });

        radioGroup.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                //aggiorna Hours limiting
                switch (position){
                    case(0):
                        hoursLimiting = 0;
                        break;
                    case(1):
                        hoursLimiting = 12;
                        break;
                    case(2):
                        hoursLimiting = 24;
                        break;
                    case (3):
                        hoursLimiting = 48;
                        break;

                    default:
                        hoursLimiting = 0;
                }

            }
        });



        ageRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int min, int max) {
                    maxAge=max;         //aggiorna maxAge
                    minAge=min;         //aggiorna minAge
                    if(maxAge>=98 ){
                        ageMaxText.setText("Età media massima :\n nessun limite");
                    }
                    else{
                        ageMaxText.setText("Età media massima :\n"+maxAge);
                    }
                    if(minAge<8){
                        ageMinText.setText("Età media minima : \n nessun limite");
                    }
                    else{
                        ageMinText.setText("Età media minima :\n"+minAge);
                    }
                }

        });

        joinersRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int min, int max) {
                maxJoiners = max;           //aggiorna maxJoiners
                minJoiners = min;           //aggiorna minJoiners
                if(maxJoiners>=199 ){
                    maxDisplay.setText("Partecipanti massimi :\n nessun limite");
                }
                else{
                    maxDisplay.setText("Partecipanti massimi :\n"+maxJoiners);
                }
                if(minJoiners<=1){
                    minDisplay.setText("Partecipanti minimi : \n nessun limite");
                }
                else{
                    minDisplay.setText("Partecipanti minimi :\n"+minJoiners);
                }
            }
        });


        return map_frag;
    }


    private void getTargetLatLng(final String current_city){
        DatabaseReference cityReference = FirebaseDatabase.getInstance().getReference().child("CityMapByName").child(current_city);

        cityReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    CityMapData mapData = dataSnapshot.getValue(CityMapData.class);
                    Double Lat = mapData.getLatitude();
                    Double Lon = mapData.getLongitude();

                    Intent toMap = new Intent(getActivity(), BasicMap.class);
                    toMap.putExtra("MIN_JOINERS", minJoiners);
                    toMap.putExtra("MAX_JOINERS", maxJoiners);
                    toMap.putExtra("MIN_AGE", minAge);
                    toMap.putExtra("MAX_AGE", maxAge);
                    toMap.putExtra("HOURS_LIMIT", hoursLimiting);
                    toMap.putExtra("CURRENT_CITY", current_city);
                    toMap.putExtra("CITY_LAT", Lat);
                    toMap.putExtra("CITY_LNG", Lon);
                    startActivity(toMap);
                }else{
                    Toasty.error(getActivity(),"C'è stato un errore nel reperire la città, selezionala di nuovo dal menu in alto a sinistra e riprova", Toast.LENGTH_SHORT, true).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
