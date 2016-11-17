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
    private String eventId;


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
        eTitle = getIntent().getExtras().getString("EVENT_NAME");
        eventId = getIntent().getExtras().getString("EVENT_ID");
        String eDescription = getIntent().getExtras().getString("EVENT_DESCRIPTION");
        final String eImage = getIntent().getExtras().getString("EVENT_IMAGE_URL");
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
}
