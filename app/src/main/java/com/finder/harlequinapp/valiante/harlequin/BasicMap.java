package com.finder.harlequinapp.valiante.harlequin;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class BasicMap extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {


    Integer markerCounter = 0;
    private SupportMapFragment basic_map;
    private String current_city;
    private DatabaseReference basicMapRef;
    private Double Lat;
    private Double Lon;
    private MarkerOptions customMarker;
    protected Integer minAge, maxAge, minJoiners, maxJoiners, hoursLimit;
    private Boolean getSingleMap = false;
    private String eventId;
    private Geocoder mGeocoder;
    private LatLngBounds latLngBounds;
    private Integer ageFilter=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map);

        mGeocoder = new Geocoder(this, Locale.getDefault());


        minAge = getIntent().getIntExtra("MIN_AGE", 0);
        maxAge = getIntent().getIntExtra("MAX_AGE", 99);
        minJoiners = getIntent().getIntExtra("MIN_JOINERS", 0);
        maxJoiners = getIntent().getIntExtra("MAX_JOINERS", 99999);
        hoursLimit = getIntent().getIntExtra("HOURS_LIMIT", 0);
        getSingleMap = getIntent().getBooleanExtra("SINGLE_MAP", false);
        eventId = getIntent().getStringExtra("EVENT_ID");
        current_city = getIntent().getStringExtra("CURRENT_CITY");
        Lat = getIntent().getDoubleExtra("CITY_LAT", 0.0);
        Lon = getIntent().getDoubleExtra("CITY_LNG", 0.0);

        LatLng first = new LatLng(41.617443, 14.253852);
        LatLng second = new LatLng(41.578224, 14.21355);
        latLngBounds = new LatLngBounds(second, first);

        if (current_city == null) {
            Toasty.error(this, "Errore mappa", Toast.LENGTH_SHORT, true).show();
        }


        customMarker = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        basicMapRef = FirebaseDatabase.getInstance().getReference().child("MapData").child(current_city);
        basicMapRef.keepSynced(true);

        basic_map = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.default_map);
        basic_map.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        addMarkers(googleMap, basicMapRef);
        LatLng currentCity = new LatLng(Lat, Lon);


        //handling per lo zoom
        if(getSingleMap) {
            Double lat = getIntent().getDoubleExtra("LATITUDE",Lat);
            Double lng = getIntent().getDoubleExtra("LONGITUDE",Lon);
            LatLng singleMarkerLatLng = new LatLng(lat,lng);
            CameraUpdate movetoSingleEvent = CameraUpdateFactory.newLatLngZoom(singleMarkerLatLng, UbiquoUtils.ADRESS_LEVEL_ZOOM);
            googleMap.animateCamera(movetoSingleEvent);
        }else{
            CameraUpdate toCity = CameraUpdateFactory.newLatLngZoom(currentCity, UbiquoUtils.CITY_LEVEL_ZOOM);
            googleMap.animateCamera(toCity);
        }



        googleMap.setInfoWindowAdapter(new CustomInfoWindow());

            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                    MapInfo p = (MapInfo) marker.getTag();
                    Intent toEventPage = new Intent(BasicMap.this, EventPage.class);
                    toEventPage.putExtra("EVENT_ID", p.getReferenceKey());
                    startActivity(toEventPage);
                }
            });


    }


    private void addMarkers(final GoogleMap googleMap, final DatabaseReference myReference) {

        if (!getSingleMap) {
            ValueEventListener mapListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        MapInfo info = postSnapshot.getValue(MapInfo.class);
                        Long eventTime = info.getTime();
                        Integer eventLikes = info.getLikes();
                        Integer totalAge = info.getTotalAge();

                        if(eventLikes != 0){
                            ageFilter = Integer.valueOf(totalAge/eventLikes);
                        }else{
                            ageFilter = 0;
                        }






                        //controlla se il marker deve essere aggiunto
                        if (checkIfItHasToBeShown(ageFilter, eventLikes, eventTime)) {
                            double myLat = info.getLat();
                            double myLon = info.getLng();
                            LatLng latLng = new LatLng(myLat, myLon);
                            markerCounter++;
                            googleMap.addMarker(customMarker.position(latLng)
                                    .title(info.geteName())
                                    .snippet(info.getpName()
                                    )).setTag(info)
                            ;


                            CustomInfoWindow myWindow = new CustomInfoWindow();

                            googleMap.setInfoWindowAdapter(myWindow);

                        }
                    }
                    myReference.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            myReference.addListenerForSingleValueEvent(mapListener);
        }
        if (getSingleMap && eventId != null) {
            final ValueEventListener mapListener = new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    MapInfo info = dataSnapshot.getValue(MapInfo.class);
                    Long eventTime = info.getTime();
                    Integer eventLikes = info.getLikes();
                    Integer eventAge = 25;
                    double myLat = info.getLat();
                    double myLon = info.getLng();
                    LatLng latLng = new LatLng(myLat, myLon);
                    googleMap.addMarker(customMarker.position(latLng)
                            .title(info.geteName())
                            .snippet(info.getpName()
                            )).setTag(info)
                    ;

                    googleMap.setInfoWindowAdapter(new CustomInfoWindow());
                    basicMapRef.child(eventId).removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            basicMapRef.child(eventId).addListenerForSingleValueEvent(mapListener);
        }

    }

    protected boolean checkIfItHasToBeShown(Integer eAge, Integer eJoiners, Long eTime) {

        return (minAge <= eAge
                && eAge <= maxAge
                && minJoiners <= eJoiners
                && eJoiners <= maxJoiners
                && hasTheRightTime(eTime));
    }

    protected boolean hasTheRightTime(Long eTime) {
        if (hoursLimit != 0) {
            Long current_time = System.currentTimeMillis();
            Long time_difference = TimeUnit.HOURS.toMillis(hoursLimit);
            return eTime >= current_time && eTime <= current_time + time_difference;
        } else return true;
    }


    @Override
    public void onInfoWindowClick(Marker marker) {


    }

    class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
        private final View myMarkerView;

        CustomInfoWindow() {
            myMarkerView = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, myMarkerView);
            return myMarkerView;
        }


        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        public void render(Marker marker, View view) {
            TextView ename = ((TextView) view.findViewById(R.id.info_name));
            TextView place = ((TextView) view.findViewById(R.id.info_place));
            TextView joiners = ((TextView) view.findViewById(R.id.info_joiners));
            TextView dateAndTime = ((TextView) view.findViewById(R.id.info_time));
            ename.setText(marker.getTitle());
            place.setText(marker.getSnippet());
            MapInfo mapInfo = (MapInfo) marker.getTag();
            Integer partecipanti = mapInfo.getLikes();
            if (partecipanti != null && partecipanti != 1) {
                joiners.setText(partecipanti + " Partecipanti");
            }
            if (partecipanti != null && partecipanti == 1) {
                joiners.setText(partecipanti + " Partecipante");
            }

            String date = fromMillisToStringDate(mapInfo.getTime());
            String time = fromMillisToStringTime(mapInfo.getTime());
            dateAndTime.setText(date + "   ~   " + time);


        }

        //da millisecondi a data
        protected String fromMillisToStringDate(Long time) {
            Date date = new Date(time);
            SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
            String[] splittedDate = format.format(date).split("/");
            return splittedDate[0] + " " + splittedDate[1];
        }

        //da millisecondi ad orario
        protected String fromMillisToStringTime(Long time) {
            Date date = new Date(time);
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            return format.format(date);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSingleMap = false;
    }


}
