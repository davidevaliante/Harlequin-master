package com.finder.harlequinapp.valiante.harlequin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CompleteProfile extends AppCompatActivity {

    private EditText userCity,userAge,userRelationship;
    private Button submit;
    private FirebaseAuth mAuth;
    private FirebaseUser facebookUser;
    private String userId;
    private DatabaseReference facebookUserRef;
    private String gender,name,surname,profile,link;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        userCity = (EditText)findViewById(R.id.cityField);
        userAge = (EditText)findViewById(R.id.ageField);
        userRelationship = (EditText)findViewById(R.id.relationshipField);

        facebookUserRef = FirebaseDatabase.getInstance().getReference();
        facebookUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = facebookUser.getUid();




         submit = (Button)findViewById(R.id.completeProfileBtn);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String age = userAge.getText().toString().trim();
                final String city = userCity.getText().toString().trim();
                final String relationship = userRelationship.toString().trim();

                FirebaseDatabase.getInstance().getReference().child("placeholderProfile").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User fbUser = dataSnapshot.getValue(User.class);
                        gender = fbUser.getUserGender();
                        name = fbUser.getUserName();
                        surname = fbUser.getUserSurname();
                        profile = fbUser.getProfileImage();
                        link = fbUser.getFacebookProfile();
                        User facebookUser = new User (name,"default@facebook.com",age,city,surname,profile,relationship,gender,link);
                        facebookUserRef.child("Users").child(userId).setValue(facebookUser);
                        FirebaseDatabase.getInstance().getReference().child("placeholderProfile").child(userId).removeValue();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });



    }
}
