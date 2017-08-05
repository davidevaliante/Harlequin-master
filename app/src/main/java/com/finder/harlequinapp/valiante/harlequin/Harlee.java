package com.finder.harlequinapp.valiante.harlequin;


import android.app.Application;
import android.support.v7.app.AppCompatDelegate;


import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class Harlee extends Application{

    @Override
    public void onCreate() {
        super.onCreate();




        //libreria per i memory leaks
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Fabric.with(this, new Crashlytics());
        Branch.getAutoInstance(this);


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Hero.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);



    }
}
