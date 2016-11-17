package com.finder.harlequinapp.valiante.harlequin;

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

public class UserProfile extends AppCompatActivity {

    private String userName;
    private String avatarPath;
    private CircularImageView userAvatar;
    private TextView profileUserName;
    private ImageButton button1;
    private String userId;
    private DatabaseReference userDataReference;
   

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        //elementi UI
        userAvatar = (CircularImageView) findViewById(R.id.profileUserAvatar);
        profileUserName = (TextView)findViewById(R.id.profileUserName);
        button1 = (ImageButton)findViewById(R.id.button1);
        //recupera extra dall'Intent
        userId = getIntent().getExtras().getString("MY_USER");



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
                profileUserName.setText(userName);
                //trova e setta l'immagine di profilo
                avatarPath = selectedUser.getProfileImage();
                Picasso.with(getApplicationContext()).load(avatarPath).into(userAvatar);
                Toast.makeText(UserProfile.this,""+avatarPath,Toast.LENGTH_LONG).show();
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


}


