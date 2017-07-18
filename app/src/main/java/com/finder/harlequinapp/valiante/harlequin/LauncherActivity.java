package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.games.event.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LauncherActivity extends AppCompatActivity {


    AVLoadingIndicatorView avi;
    private TextView appName;
    private FirebaseAuth.AuthStateListener myAuthStateListener;
    private FirebaseAuth myAuth;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Typeface steinerlight = Typeface.createFromAsset(getAssets(),"fonts/Steinerlight.ttf");
        myAuth = FirebaseAuth.getInstance();

        //AuthListener
        myAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                /*Utente non loggato (nuovo utente oppure utente che ha effettuato il log-out
                da mandare alla pagina di registrazione/login */
                if(FirebaseAuth.getInstance().getCurrentUser() == null){

                    Intent toLogin = new Intent(LauncherActivity.this,MainActivity.class);
                    startActivity(toLogin);
                    finish();

                }

/*
                Utente loggato, va mandato alla MainUserPage della sua città
*/

                if(FirebaseAuth.getInstance().getCurrentUser() != null){
                    SharedPreferences userData = getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE);

                    //default registrato durante la registrazione
                    String current_city  = userData.getString("USER_CITY","NA");
                    final String eventId = getIntent().getStringExtra("EVENT_ID");
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();



                    //se non è già stata scelta una città
                    if(current_city.equalsIgnoreCase("NA") && eventId == null){
                        FirebaseDatabase.getInstance().getReference().child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    Intent toCitySelector = new Intent(LauncherActivity.this, CitySelector.class);
                                    startActivity(toCitySelector);
                                    finish();
                                }else{
                                    FirebaseAuth.getInstance().signOut();
                                    Intent toLogin = new Intent(LauncherActivity.this,MainActivity.class);
                                    startActivity(toLogin);
                                    finish();                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }else {
                        if(eventId != null){
                            Log.d("Event id : ",eventId);

                            Log.d("User logged with Id : ", firebaseAuth.getCurrentUser().getUid());

                            FirebaseDatabase.getInstance().getReference().child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        Intent toEventPage = new Intent(LauncherActivity.this,MainUserPage.class);
                                        toEventPage.putExtra("EVENT_ID",eventId);
                                        startActivity(toEventPage);
                                        finish();
                                    }else{
                                        FirebaseAuth.getInstance().signOut();
                                        Intent toLogin = new Intent(LauncherActivity.this,MainActivity.class);
                                        startActivity(toLogin);
                                        finish();                                }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }else{
                            Log.d("event_id is null","null");

                            FirebaseDatabase.getInstance().getReference().child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        Intent toUserPage = new Intent(LauncherActivity.this, MainUserPage.class);
                                        startActivity(toUserPage);
                                        finish();
                                    }else{
                                        FirebaseAuth.getInstance().signOut();
                                        Intent toLogin = new Intent(LauncherActivity.this,MainActivity.class);
                                        startActivity(toLogin);
                                        finish();                                }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    }

                }
            }
        };


        avi = (AVLoadingIndicatorView)findViewById(R.id.avi_loader_launcher);
        appName = (TextView)findViewById(R.id.launcherAppName);
        appName.setTypeface(steinerlight);
        avi.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                myAuth.addAuthStateListener(myAuthStateListener);



            }
        },SPLASH_TIME_OUT);

    }

    //per la libreria Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myAuth.removeAuthStateListener(myAuthStateListener);
    }
}
