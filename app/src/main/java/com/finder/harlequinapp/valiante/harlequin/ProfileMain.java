package com.finder.harlequinapp.valiante.harlequin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ProfileMain extends AppCompatActivity {

    protected static String userId;
    protected static Boolean ownProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_main);

        userId = getIntent().getExtras().getString("USER_ID");
        ownProfile = getIntent().getExtras().getBoolean("OWN_PROFILE");
    }
}
