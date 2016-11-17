package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserProfile extends AppCompatActivity {

    private String userName;
    private String userSurname;
    private String avatarPath,userCity;
    private CircularImageView userAvatar;
    private TextView profileUserName;
    private ImageButton button1;
    private String userId;
    private DatabaseReference userDataReference;
    private TextView ageView,cityView;
    private Integer userAge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        //elementi UI
        userAvatar = (CircularImageView) findViewById(R.id.profileUserAvatar);
        profileUserName = (TextView)findViewById(R.id.profileUserName);
        button1 = (ImageButton)findViewById(R.id.button1);
        cityView = (TextView)findViewById(R.id.cityView);
        ageView = (TextView)findViewById(R.id.ageView);
        //recupera extra dall'Intent
        userId = getIntent().getExtras().getString("TARGET_USER");



        userDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userDataReference.keepSynced(true);
        //recupera i dati rispettivamente all'ID fornito così che ogni pulsante debba fornire nell'Intent
        //solo l'ID dell'utente richiesto;
        userDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User selectedUser = dataSnapshot.getValue(User.class);
                //trova e setta il nome
                userName = selectedUser.getUserName();
                userSurname = selectedUser.getUserSurname();
                profileUserName.setText(userName+" "+userSurname);
                //trova e setta la città
                userCity = selectedUser.getUserCity();
                cityView.setText("Viene da : "+userCity);
                //trova e setta l'età
                userAge = selectedUser.getUserAge();
                ageView.setText("Età : "+userAge);
                //trova e setta l'immagine di profilo
                avatarPath = selectedUser.getProfileImage();
                //TODO aggiungere network Policy Offline
                Picasso.with(getApplicationContext()).load(avatarPath).into(userAvatar);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //prova pulsante
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UserProfile.this,"ok",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}


