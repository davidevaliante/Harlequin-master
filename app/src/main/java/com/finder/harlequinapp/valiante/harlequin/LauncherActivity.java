package com.finder.harlequinapp.valiante.harlequin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent toLogin = new Intent(LauncherActivity.this,MainActivity.class);
            startActivity(toLogin);
            finish();
        }
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent toUserPage = new Intent(LauncherActivity.this,MainUserPage.class);
            startActivity(toUserPage);
            finish();
        }
    }
}
