package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 */
public class SecondPageRegistrationEmailFragment extends Fragment {


    private TextView privacyButton;
    private SupportPlaceAutocompleteFragment autoCompleteCity;
    private RadioRealButtonGroup gender,single;
    private RelativeLayout nextButton;
    private Boolean isSingle = true;
    private Boolean isMale = true;
    private Geocoder mGeocoder;
    private String cityName;
    private String editId;
    private String databaseCityName;

    public SecondPageRegistrationEmailFragment() {
        // Required empty public constructor
    }

    public static SecondPageRegistrationEmailFragment newInstance(){
        SecondPageRegistrationEmailFragment newSecondFragment = new SecondPageRegistrationEmailFragment();
        return newSecondFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_second_page_registration_email,container,false);

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
        editId = ((RegistrationEmail)getActivity()).editId;

        privacyButton = (TextView)rootView.findViewById(R.id.privacyPolicyButton);
        privacyButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(),R.drawable.privacy_lock_24),null,null,null);
        autoCompleteCity = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.reg_autocomplete_city);
        gender = (RadioRealButtonGroup)rootView.findViewById(R.id.regGenderGroup);
        single = (RadioRealButtonGroup)rootView.findViewById(R.id.regSingleGroup);
        nextButton = (RelativeLayout)rootView.findViewById(R.id.regNextButton);

        AutocompleteFilter filter =
                new AutocompleteFilter.Builder().setCountry("IT").build();
        autoCompleteCity.setFilter(filter);
        autoCompleteCity.setHint("Cerca la tua città");


        autoCompleteCity.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                try {
                   cityName = getCityNameByCoordinates(place.getLatLng().latitude,place.getLatLng().longitude);
                    //getCityFromDatabase(place.getLatLng().latitude,place.getLatLng().longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                    cityName = "NA";
                    //getCityFromDatabase(place.getLatLng().latitude,place.getLatLng().longitude);
                }


            }

            @Override
            public void onError(Status status) {

            }
        });

        single.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if(position==0){
                    isSingle=true;
                }
                if(position==1){
                    isSingle=false;
                }
            }
        });

        gender.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if(position==0){
                    isMale=true;
                }
                if(position==1){
                    isMale=false;
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editId == null){
                    if(canGoNext()){
                        saveDataIntoRegistrationPreferences();
                        ((RegistrationEmail)getActivity()).registrationViewPager.setCurrentItem(2,true);
                    }
                }else{
                    if(canGoNext()){
                        saveDataIntoEditPreferences();
                        ((RegistrationEmail)getActivity()).registrationViewPager.setCurrentItem(2,true);
                    }
                }
            }
        });

        privacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PrivacyFragment privacyFragment = new PrivacyFragment().newInstance();
                privacyFragment.show(fm,"privcy_frag");
            }
        });


        return rootView;
    }

    private String getCityNameByCoordinates(final double lat, final double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }else{
            FirebaseDatabase.getInstance().getReference().child("CityMapByName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        CityMapData city = data.getValue(CityMapData.class);
                        if(city.getLatitude() == lat && city.getLongitude() == lon){
                            databaseCityName = data.getKey().toString();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return null;
    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;

        if(cityName == null){
            Toasty.error(getActivity(),"Inserisci la tua città", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(cityName.isEmpty()){
            Toasty.error(getActivity(),"Inserisci la tua città", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(cityName.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Non siamo riusciti a trovare la tua città scegli la città più vicina a te",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        return canGoNext;

    }

    private void saveDataIntoRegistrationPreferences(){
        SharedPreferences preferences = getActivity().getSharedPreferences("EMAIL_REG", Context.MODE_PRIVATE);
        SharedPreferences.Editor regEditor = preferences.edit();
        Boolean single = isSingle;
        Boolean male = isMale;
        String city = cityName;
        regEditor.putBoolean("USER_ISSINGLE",single);
        regEditor.putBoolean("USER_ISMALE",male);
        regEditor.putString("USER_CITY",city);
        regEditor.apply();

        UbiquoUtils.printPreferences(preferences);
    }

    private void saveDataIntoEditPreferences(){
        SharedPreferences preferences = getActivity().getSharedPreferences("EDIT_REG", Context.MODE_PRIVATE);
        SharedPreferences.Editor regEditor = preferences.edit();
        Boolean single = isSingle;
        Boolean male = isMale;
        String city = cityName;
        regEditor.putBoolean("USER_ISSINGLE",single);
        regEditor.putBoolean("USER_ISMALE",male);
        regEditor.putString("USER_CITY",city);
        regEditor.apply();

        UbiquoUtils.printPreferences(preferences);
    }

    protected void getCityFromDatabase(final Double lat, final Double lon){
        FirebaseDatabase.getInstance().getReference().child("CityMapByName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    CityMapData city = data.getValue(CityMapData.class);
                    if(city.getLatitude() == lat && city.getLongitude() == lon){
                        databaseCityName = data.getRef().getKey().toString();
                        Log.d("Database city : ", databaseCityName);
                    }else{
                        Log.d("Database city : ", "different latlng");

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
