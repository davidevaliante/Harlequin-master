package com.finder.harlequinapp.valiante.harlequin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CompleteProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {



    private FirebaseUser facebookUser;
    private String userId;
    private DatabaseReference facebookUserRef;
    private String gender,name,surname,profile,link,cityName;
    private RadioRealButton singleButton, engagedButton;
    private RadioRealButtonGroup group;
    private DatabaseReference placeholderRef;
    private Boolean isSingle = true;
    private CircularImageView avatar;
    private ProgressDialog mProgressBar;
    private MaterialRippleLayout submitRipple;
    private ValueEventListener placeHolderListener;
    protected SupportPlaceAutocompleteFragment city;
    protected TextView birthday, policyButton;
    protected Geocoder mGeocoder;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        policyButton = (TextView)findViewById(R.id.policyButton);
        birthday = (TextView)findViewById(R.id.facebookBirthday);
        city = (SupportPlaceAutocompleteFragment)getSupportFragmentManager().findFragmentById(R.id.facebook_autocomplete_city);
        singleButton = (RadioRealButton)findViewById(R.id.singleRadioButton);
        engagedButton = (RadioRealButton)findViewById(R.id.engagedRadioButton);
        group = (RadioRealButtonGroup)findViewById(R.id.group);
        avatar = (CircularImageView)findViewById(R.id.submitAvatar);
        submitRipple = (MaterialRippleLayout)findViewById(R.id.rippleSubmit);
        mProgressBar = new ProgressDialog(this);
        facebookUserRef = FirebaseDatabase.getInstance().getReference();
        facebookUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = facebookUser.getUid();
        placeholderRef = FirebaseDatabase.getInstance().getReference().child("placeholderProfile").child(userId);
        placeholderRef.keepSynced(true);


        mGeocoder = new Geocoder(this, Locale.getDefault());


        AutocompleteFilter filter =
                new AutocompleteFilter.Builder().setCountry("IT").build();
        city.setFilter(filter);
        city.setHint("Cerca la tua città");

        city.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                try {
                    cityName = getCityNameByCoordinates(place.getLatLng().latitude,place.getLatLng().longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                    cityName = "NA";
                }
            }

            @Override
            public void onError(Status status) {

            }
        });

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog
                        = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                        CompleteProfile.this,
                        2017,
                        0,
                        1
                );
                datePickerDialog.setAccentColor(Color.parseColor("#673AB7"));
                datePickerDialog.setCancelColor(Color.parseColor("#18FFFF"));
                datePickerDialog.vibrate(false);
                datePickerDialog.showYearPickerFirst(true);
                datePickerDialog.show(getSupportFragmentManager(),"DatepickerDialog");
            }
        });


        //selezionatore situazione sentimentale default = true
        group.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {

                if(position == 0){
                    isSingle = true;
                }
                if(position== 1){
                    isSingle = false;
                }


            }
        });

        //scrive l'utente di Facebook nel database
        submitRipple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Log.d("City value : ",cityName);
                //controlla che i campi richiesti siano correttamente riempiti
                if(!birthday.getText().toString().isEmpty() && cityName != null) {
                    final String age = birthday.getText().toString().trim();
                    final String city = cityName;
                    SharedPreferences prefs = getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE);
                    prefs.edit().putString("USER_CITY",city).apply();
                    final Intent toUserPaage = new Intent(CompleteProfile.this, MainUserPage.class);
                    mProgressBar.setMessage("Attendere prego");
                    mProgressBar.show();


                    //se tutti i campi richiesti sono stati compilati allora reperisce i dati dal profilo Placeholder
                    //e finalizza la registrazione creando un nuovo utente e cancellando il placeholder ad operazione conclusa
                    //poi manda l'utente alla MainUserPage
                    ValueEventListener submitListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //prende l'utente dal placeHolder
                            User fbUser = dataSnapshot.getValue(User.class);
                            //se è single
                            if (isSingle) {
                                String relationship = "Single";
                                gender = fbUser.getUserGender();
                                name = fbUser.getUserName();
                                surname = fbUser.getUserSurname();
                                profile = fbUser.getProfileImage();
                                link = fbUser.getFacebookProfile();
                                //token FCM
                                String messaging_token = FirebaseInstanceId.getInstance().getToken();
                                String final_token;
                                if (messaging_token.isEmpty()){
                                    final_token = messaging_token;
                                }else{
                                    final_token = "no_token";
                                }
                                FirebaseDatabase.getInstance().getReference().child("Tokens").child(userId).child("user_token").setValue(final_token);
                                Long registrationDate = System.currentTimeMillis();

                                //crea l'utente finale
                                User facebookUser = new User(name,"default@facebook.com",age,city,surname,profile,relationship,gender,
                                                             link,buildAnonName(fbUser),final_token,registrationDate,0L);

                                //lo inserisce nel database con un CompleteListener
                                facebookUserRef.child("Users").child(userId).setValue(facebookUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    //rimuove il placeholder
                                        FirebaseDatabase.getInstance().getReference().child("placeholderProfile").child(userId).removeValue();

                                        mProgressBar.dismiss();
                                        startActivity(toUserPaage);
                                        Toasty.success(CompleteProfile.this,"Registrazione effettuata !", Toast.LENGTH_SHORT, true).show();
                                    }
                                });

                            }
                            //se è impegnato
                            if (!isSingle) {
                                String relationship = isEngagedFixer(fbUser);
                                gender = fbUser.getUserGender();
                                name = fbUser.getUserName();
                                surname = fbUser.getUserSurname();
                                profile = fbUser.getProfileImage();
                                link = fbUser.getFacebookProfile();
                                //token FCM
                                String messaging_token = FirebaseInstanceId.getInstance().getToken();
                                String final_token;
                                if (messaging_token.isEmpty()){
                                    final_token = messaging_token;
                                }else{
                                    final_token = "no_token";
                                }
                                FirebaseDatabase.getInstance().getReference().child("Tokens").child(userId).child("user_token").setValue(final_token);

                                Long registrationDate = System.currentTimeMillis();

                                //crea l'utente finale
                                User facebookUser = new User(name,"default@facebook.com",age,city,surname,profile,relationship,gender,
                                                             link,buildAnonName(fbUser),final_token,registrationDate,0L);
                                //lo inserisce nel database
                                facebookUserRef.child("Users").child(userId).setValue(facebookUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //rimuove il placeholder
                                    FirebaseDatabase.getInstance().getReference().child("placeholderProfile").child(userId).removeValue();
                                    mProgressBar.dismiss();
                                    startActivity(toUserPaage);
                                    Toasty.success(CompleteProfile.this,"Registrazione effettuata !", Toast.LENGTH_SHORT, true).show();

                                    }
                                });

                            }
                          //rimuove il listener dopo aver completato la registrazione
                          placeholderRef.removeEventListener(this);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        Toasty.error(CompleteProfile.this,"Verifica la tua connessione e riprova",Toast.LENGTH_SHORT,true).show();
                        }
                    };
                    placeholderRef.addValueEventListener(submitListener);
                }else{
                    Toasty.error(CompleteProfile.this,"Riempi tutti i campi e riprova",Toast.LENGTH_SHORT,true).show();
                }
            }
        });

        policyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                PrivacyFragment privacyFragment = new PrivacyFragment().newInstance();
                privacyFragment.show(fm,"privcy_frag");
            }
        });
    }//[FINE DI ONCREATE]

    @Override
    protected void onStart() {
        super.onStart();
        //recupera i dati dal profilo placeholder
        placeHolderListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User fbUser = dataSnapshot.getValue(User.class);
                //carica l'immagine appena reperita da Facebook
                Picasso.with(CompleteProfile.this)
                            .load(fbUser.getProfileImage())
                            .into(avatar);
                //setta il sesso boolean e l'icona in base a Facebook
                if(fbUser.getUserGender().equalsIgnoreCase("female")){
                    singleButton.setButtonImage(R.drawable.single_female);
                    engagedButton.setText("Impegnata");
                }
                placeholderRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toasty.error(CompleteProfile.this,"C'è stato un problema nel caricare i tuoi dati da Facebook",Toast.LENGTH_SHORT,true).show();
            }
        };
        placeholderRef.addValueEventListener(placeHolderListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        placeholderRef.removeEventListener(placeHolderListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String isEngagedFixer (User user){
        String status=null;
        if (user.getUserGender().equalsIgnoreCase("Uomo")){
            status = "Impegnato";

        }

        if (user.getUserGender().equalsIgnoreCase("Donna")){
            status = "Impegnata";
        }
        return status;
    }

    //helper method per creare un nome anonimo
    private String buildAnonName (User anonUser){
        String anonName = "Anonymous";
        Random randomizer = new Random(System.currentTimeMillis());
        Integer anonLetter = anonUser.getUserName().length();
        anonName = anonName+(10000+randomizer.nextInt(10000)+anonLetter);
        return anonName;

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            Log.d("City : " ,addresses.get(0).getLocality());
            return addresses.get(0).getLocality();
        }
        return null;
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Integer fixedMonth = monthOfYear+1;
        birthday.setText(dayOfMonth+"/"+fixedMonth+"/"+year);
    }
}
