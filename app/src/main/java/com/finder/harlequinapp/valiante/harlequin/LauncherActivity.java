package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.wang.avi.AVLoadingIndicatorView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LauncherActivity extends AppCompatActivity {


    AVLoadingIndicatorView avi;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        avi = (AVLoadingIndicatorView)findViewById(R.id.avi_loader_launcher);
        avi.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        },SPLASH_TIME_OUT);

    }

    //per la libreria Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
