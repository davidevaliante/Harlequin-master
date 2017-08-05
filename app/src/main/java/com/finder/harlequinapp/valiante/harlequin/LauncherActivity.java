package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.facebook.FacebookSdk;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import bolts.AppLinks;
import es.dmoral.toasty.Toasty;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LauncherActivity extends AppCompatActivity {


    AVLoadingIndicatorView avi;
    private TextView appName;
    private FirebaseAuth.AuthStateListener myAuthStateListener;
    private FirebaseAuth myAuth;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 700;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);



        Typeface steinerlight = Typeface.createFromAsset(getAssets(),"fonts/Steinerlight.ttf");
        myAuth = FirebaseAuth.getInstance();

        //AuthListener
        myAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {

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
                    final String current_city  = userData.getString("USER_CITY","NA");
                    //eventId = getIntent().getStringExtra("EVENT_ID");
                    final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Branch branch = Branch.getInstance();

                    branch.initSession(new Branch.BranchReferralInitListener(){
                        @Override
                        public void onInitFinished(JSONObject referringParams, BranchError error) {
                            if (error == null) {

                                String deepLinkEventId = referringParams.optString("EVENT_ID","NA");

                                //app aperta da deepLink
                                if(!deepLinkEventId.equalsIgnoreCase("NA")){
                                    eventId=deepLinkEventId;

                                    //città assente ?
                                    if(current_city.equalsIgnoreCase("NA") && eventId == null){
                                        sendUserToCitySelectOrLogin(id);
                                    }else {
                                        //l'app viene aperta da un deepLink
                                        if(eventId != null){
                                            sendUserToEventPage(id);
                                        }else{
                                            sendUserToUserPage(id);
                                        }
                                    }

                                //app lanciata dalla home o da una notifica admin
                                }else{
                                    //lanciata da notifica admin ?
                                    eventId=getIntent().getStringExtra("EVENT_ID");

                                    //città scelta ?
                                    if(current_city.equalsIgnoreCase("NA") && eventId == null){
                                        sendUserToCitySelectOrLogin(id);
                                    }else {
                                        if(eventId != null){
                                            sendUserToEventPage(id);
                                        }else{
                                            sendUserToUserPage(id);
                                        }
                                    }

                                }
                            } else {
                                Log.i("MyApp", error.getMessage());
                            }
                        }
                   }, getIntent().getData(), LauncherActivity.this);

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

    //se l'utente è prensente lo ma
    private void sendUserToCitySelectOrLogin(String id){
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
    }

    private void sendUserToEventPage(String id){
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
    }

    private void sendUserToUserPage(String id){
        FirebaseDatabase.getInstance().getReference().child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent toUserPage = new Intent(LauncherActivity.this, MainUserPage.class);
                    if(eventId!=null){
                        toUserPage.putExtra("EVENT_ID",eventId);
                    }
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


    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}
