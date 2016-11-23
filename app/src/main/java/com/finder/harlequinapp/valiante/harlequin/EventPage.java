package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.w3c.dom.Text;

public class EventPage extends AppCompatActivity {


    private ImageView eImageView;
    private TextView eEventTitle,eEventDescription;
    private Context ctx;
    private Button chat;
    private String eTitle;
    private String eventId, eDescription, eImage;
    private DatabaseReference myEventReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);

        //Elementi UI
        eImageView = (ImageView)findViewById(R.id.pEventImage);
        eEventTitle = (TextView) findViewById(R.id.pEventTitle);
        chat = (Button)findViewById(R.id.goToChat);
        eEventDescription = (TextView) findViewById(R.id.pEventDescription);

        //prende dati dall'Intent

        eventId = getIntent().getExtras().getString("EVENT_ID");


        myEventReference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventId);

        myEventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event myEvent = dataSnapshot.getValue(Event.class);
                eTitle = myEvent.getEventName();
                eDescription = myEvent.getDescription();
                eImage = myEvent.getEventImagePath();
                //li carica
                eEventTitle.setText(eTitle);
                eEventDescription.setText(eDescription);
                Picasso.with(ctx)
                        .load(eImage)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(eImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                //va bene così non deve fare nulla
                            }
                            @Override
                            public void onError() {
                                Picasso.with(ctx).load(eImage).into(eImageView);
                            }
                        });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        //manda alla chat inviando i dati fondamentali del canale
        chat.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
          Intent goChat = new Intent (EventPage.this,ChatRoom.class);
                 goChat.putExtra("CHAT_NAME",eTitle);
                 goChat.putExtra("EVENT_ID_FOR_CHAT", eventId);
          startActivity(goChat);
          }
        });

    }

    private void backToUserPage(){
        Intent goBack = new Intent (EventPage.this,UserPage.class);
        startActivity(goBack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
