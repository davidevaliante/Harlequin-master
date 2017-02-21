package com.finder.harlequinapp.valiante.harlequin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BasicMap extends AppCompatActivity implements OnMapReadyCallback,
                                                           GoogleMap.OnInfoWindowClickListener

                                                            {

    private SupportMapFragment basic_map;
    private String current_city="Isernia";
    private DatabaseReference basicMapRef;
    private Float isLat = 41.596545f;
    private Float isLon = 14.233357f;
    private MarkerOptions customMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map);


        customMarker = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        basicMapRef = FirebaseDatabase.getInstance().getReference().child("MapData").child(current_city);
        basicMapRef.keepSynced(true);

        basic_map = (SupportMapFragment)getSupportFragmentManager()
                                        .findFragmentById(R.id.default_map);
        basic_map.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng pos = new LatLng(isLat,isLon);
        addMarkers(googleMap,basicMapRef);
        LatLng currentCity = new LatLng(isLat,isLon);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentCity));
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(currentCity,13);
        googleMap.animateCamera(location);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setInfoWindowAdapter(new CustomInfoWindow());

    }




    private void addMarkers(final GoogleMap googleMap, final DatabaseReference myReference){

        ValueEventListener mapListener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    MapInfo info = postSnapshot.getValue(MapInfo.class);
                    double myLat = info.getLat();
                    double myLon = info.getLng();
                    LatLng latLng = new LatLng(myLat,myLon);

                    googleMap.addMarker(customMarker.position(latLng)
                                                    .title(info.geteName())
                                                    .snippet(info.getpName()
                                                    )).setTag(info)
                                                    ;

                    googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Toast.makeText(BasicMap.this,"ID :"+postSnapshot.getKey(),Toast.LENGTH_LONG).show();
                        }
                    });
                    googleMap.setInfoWindowAdapter(new CustomInfoWindow());



                }
                myReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        myReference.addListenerForSingleValueEvent(mapListener);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }

   class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{
        private final View myMarkerView;

       CustomInfoWindow(){
           myMarkerView = getLayoutInflater().inflate(R.layout.custom_info_window,null);
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

       public void render(Marker marker,View view){
           TextView ename = ((TextView)view.findViewById(R.id.info_name));
           TextView place = ((TextView)view.findViewById(R.id.info_place));
           TextView joiners = ((TextView)view.findViewById(R.id.info_joiners));
           TextView dateAndTime = ((TextView)view.findViewById(R.id.info_time));
           ename.setText(marker.getTitle());
           place.setText(marker.getSnippet());
           MapInfo mapInfo =(MapInfo)marker.getTag();
           Integer partecipanti = mapInfo.getLikes();
           if(partecipanti!=null&&partecipanti!=1){
               joiners.setText(partecipanti+" Partecipanti");
           }
           if(partecipanti!=null&&partecipanti==1){
               joiners.setText(partecipanti+" Partecipante");
           }

           String  date = fromMillisToStringDate(mapInfo.getTime());
           String time = fromMillisToStringTime(mapInfo.getTime());
           dateAndTime.setText(date+ "   ~   "+time);


       }

       //da millisecondi a data
       protected String fromMillisToStringDate(Long time) {
           Date date = new Date(time);
           SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
           String[] splittedDate = format.format(date).split("/");
           return splittedDate[0]+" "+splittedDate[1];
       }

       //da millisecondi ad orario
       protected String fromMillisToStringTime(Long time){
           Date date = new Date(time);
           SimpleDateFormat format = new SimpleDateFormat("HH:mm");
           return format.format(date);
       }
   }





                                                            }
