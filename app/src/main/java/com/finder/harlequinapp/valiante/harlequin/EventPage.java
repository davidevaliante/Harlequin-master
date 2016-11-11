package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);


        eImageView = (ImageView)findViewById(R.id.pEventImage);
        eEventTitle = (TextView) findViewById(R.id.pEventTitle);
        eEventDescription = (TextView) findViewById(R.id.pEventDescription);




            //prende dati dall'Intent
            String eTitle = getIntent().getExtras().getString("EVENT_NAME");
            Toast.makeText(this,eTitle,Toast.LENGTH_LONG).show();
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












    }

    private void backToUserPage(){
        Intent goBack = new Intent (EventPage.this,UserPage.class);
        startActivity(goBack);
    }
}
