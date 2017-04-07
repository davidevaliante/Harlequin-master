package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class About extends AppCompatActivity {

    private TextView fbDavide, fbIlaria,ilariaPhone,davidePhone,mailDavide,mailIlaria;
    private final static String davideUrl = "https://www.facebook.com/allsunday.jesus";
    private final static String ilariaUrl = "https://www.facebook.com/Bloody.Tallulah.ily.fili.derba?fref=ts";
    private final static String[] davideMail = {"davide@ubiquo.cloud"};
    private final static String[] ilariaMail = {"ilaria@ubiquo.cloud"};

    PackageManager mPackageManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        fbDavide = (TextView)findViewById(R.id.fbDavide);
        fbIlaria = (TextView)findViewById(R.id.fbIlaria);
        ilariaPhone = (TextView)findViewById(R.id.ilariaPhone);
        davidePhone = (TextView)findViewById(R.id.davidePhone);
        mailIlaria = (TextView)findViewById(R.id.ilariaMail);
        mailDavide = (TextView)findViewById(R.id.davideMail);
        mPackageManager = getPackageManager();

        fbDavide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fb = newFacebookIntent(mPackageManager,davideUrl);
                startActivity(fb);
            }
        });

        davidePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + davidePhone.getText().toString().trim()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            }
        });

        mailDavide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeEmail(davideMail);
            }
        });

        fbIlaria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fb = newFacebookIntent(mPackageManager,ilariaUrl);
                startActivity(fb);
            }
        });

        ilariaPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ilariaPhone.getText().toString().trim()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        mailIlaria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeEmail(ilariaMail);
            }
        });
    }

    //per la libreria Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    //se facebook non è installato restituisce il link per aprirlo dal browser
    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public void composeEmail(String[] addresses) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


}
