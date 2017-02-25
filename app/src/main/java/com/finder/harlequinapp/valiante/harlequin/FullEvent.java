package com.finder.harlequinapp.valiante.harlequin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FullEvent extends AppCompatActivity {

    String EVENT_ID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_event);

        //recupera l'ID dall'Intent che manda a quest'attività
        EVENT_ID = getIntent().getExtras().getString("EVENT_ID");
    }
}
