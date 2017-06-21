package com.finder.harlequinapp.valiante.harlequin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.florent37.materialtextfield.MaterialTextField;
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

import java.util.Random;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CompleteProfile extends AppCompatActivity implements com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener {

    protected EditText userCity,userAge;
    private Button submit;

    private FirebaseUser facebookUser;
    private String userId;
    private DatabaseReference facebookUserRef;
    private String gender,name,surname,profile,link;
    private RadioRealButton singleButton, engagedButton;
    private RadioRealButtonGroup group;
    private DatabaseReference placeholderRef;
    private Boolean isSingle = true;
    private CircularImageView avatar;
    private ProgressDialog mProgressBar;
    private MaterialRippleLayout submitRipple;
    private ValueEventListener placeHolderListener;
    protected MaterialTextField materialAgeField;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        materialAgeField = (MaterialTextField)findViewById(R.id.materialAgeField);
        userCity = (EditText)findViewById(R.id.cityField);
        userAge = (EditText)findViewById(R.id.ageField);
        singleButton = (RadioRealButton)findViewById(R.id.singleRadioButton);
        engagedButton = (RadioRealButton)findViewById(R.id.engagedRadioButton);
        group = (RadioRealButtonGroup)findViewById(R.id.group);
        avatar = (CircularImageView)findViewById(R.id.submitAvatar);
        submitRipple = (MaterialRippleLayout)findViewById(R.id.rippleSubmit);
        mProgressBar = new ProgressDialog(this);
        facebookUserRef = FirebaseDatabase.getInstance().getReference();
        facebookUser = FirebaseAuth.getInstance().getCurrentUser();
        submit = (Button)findViewById(R.id.completeProfileBtn);
        userId = facebookUser.getUid();
        placeholderRef = FirebaseDatabase.getInstance().getReference().child("placeholderProfile").child(userId);
        placeholderRef.keepSynced(true);
        userAge.setFocusable(false);
        userAge.setClickable(true);

        //i due metodi di seguito sono praticamente identici ma servono a causa dell'utilizzo del material textField
        //nell'UI
        materialAgeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

/*
                final DatePickerDialog mDatePicker = new DatePickerDialog(CompleteProfile.this, new DatePickerDialog.OnDateSetListener() {
                    //quando viene premuto "ok"..
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        year = selectedyear;
                        month = selectedmonth;
                        day = selectedday;

                        materialAgeField.toggle();
                       userAge.setText("" + day + "/" + (month+1) + "/" + year);


                    }
                }, year,month,day);

                mDatePicker.show();*/


                com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog
                        = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                        CompleteProfile.this,
                        2017,
                        1,
                        1
                );
                datePickerDialog.setAccentColor(Color.parseColor("#673AB7"));
                datePickerDialog.setCancelColor(Color.parseColor("#18FFFF"));
                datePickerDialog.vibrate(false);
                datePickerDialog.showYearPickerFirst(true);
                datePickerDialog.show(getSupportFragmentManager(),"DatepickerDialog");

            }
        });
        //colne metodo
        userAge.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog
                        = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                        CompleteProfile.this,
                        2017,
                        1,
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

                //controlla che i campi richiesti siano correttamente riempiti
                if(!userAge.getText().toString().isEmpty() || !userCity.getText().toString().isEmpty()) {
                    final String age = userAge.getText().toString().trim();
                    final String city = capitalize(userCity.getText().toString().trim());
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

                                //crea l'utente finale
                                User facebookUser = new User(name,"default@facebook.com",age,city,surname,profile,relationship,gender,
                                                             link,buildAnonName(fbUser),final_token);

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
                                //crea l'utente finale
                                User facebookUser = new User(name,"default@facebook.com",age,city,surname,profile,relationship,gender,
                                                             link,buildAnonName(fbUser),final_token);
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

    //callback per il DatePicker
    @Override
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        materialAgeField.expand();

        userAge.setText("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

    }
}
