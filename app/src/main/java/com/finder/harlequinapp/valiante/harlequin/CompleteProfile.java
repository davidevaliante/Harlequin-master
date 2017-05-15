package com.finder.harlequinapp.valiante.harlequin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Random;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CompleteProfile extends AppCompatActivity {

    private EditText userCity,userAge;
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
    private MaterialTextField materialAgeField;



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

        //i due metodi di seguito sono praticamente identici ma servono a causa dell'utilizzo del material textField
        //nell'UI
        materialAgeField.setOnClickListener(new View.OnClickListener() {
            Integer year = 1995;
            Integer month = 0;
            Integer day = 1;
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(CompleteProfile.this, new DatePickerDialog.OnDateSetListener() {
                    //quando viene premuto "ok"..
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        year = selectedyear;
                        month = selectedmonth;
                        day = selectedday;
                        materialAgeField.toggle();
                       userAge.setText("" + day + "/" + (month+1) + "/" + year);


                    }
                }, year,month,day);
                mDatePicker.show();

            }
        });
        //colne metodo
        userAge.setOnClickListener(new View.OnClickListener() {
            Integer year = 1995;
            Integer month = 0;
            Integer day = 1;
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(CompleteProfile.this, new DatePickerDialog.OnDateSetListener() {
                    //quando viene premuto "ok"..
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        year = selectedyear;
                        month = selectedmonth;
                        day = selectedday;
                        userAge.setText("" + day + "/" + (month+1) + "/" + year);


                    }
                }, year,month,day);
                mDatePicker.show();

            }
        });

        //selezionatore situazione sentimentale
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
                                //crea l'utente finale
                                User facebookUser = new User(name,
                                                            "default@facebook.com",
                                                            age,
                                                            city,
                                                            surname,
                                                            profile,
                                                            relationship,
                                                            gender,
                                                            link,
                                                            buildAnonName(fbUser),
                                                            "no_token");
                                //lo inserisce nel database
                                facebookUserRef.child("Users").child(userId).setValue(facebookUser)
                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                 //rimuove il placeholder
                                                FirebaseDatabase.getInstance().getReference()
                                                                              .child("placeholderProfile")
                                                                              .child(userId)
                                                                              .removeValue();
                                                mProgressBar.dismiss();
                                                startActivity(toUserPaage);
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
                                //crea l'utente finale
                                User facebookUser = new User(name,
                                                            "default@facebook.com",
                                                            age,
                                                            city,
                                                            surname,
                                                            profile,
                                                            relationship,
                                                            gender,
                                                            link,
                                                            buildAnonName(fbUser),
                                                            "no_token");
                                //lo inserisce nel database
                                facebookUserRef.child("Users").child(userId).setValue(facebookUser)
                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                 //rimuove il placeholder
                                               FirebaseDatabase.getInstance().getReference()
                                                                             .child("placeholderProfile")
                                                                             .child(userId)
                                                                             .removeValue();
                                               mProgressBar.dismiss();
                                               startActivity(toUserPaage);
                                                   }
                                               });

                            }
                         placeholderRef.removeEventListener(this);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    placeholderRef.addValueEventListener(submitListener);
                }else{
                    Toast.makeText(CompleteProfile.this,"Riempi tutti i campi e riprova",Toast.LENGTH_SHORT).show();
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


                Picasso.with(CompleteProfile.this)
                        .load(fbUser.getProfileImage())
                        .into(avatar);
                if(fbUser.getUserGender().equalsIgnoreCase("female")){
                    singleButton.setButtonImage(R.drawable.single_female);
                    engagedButton.setText("Impegnata");
                }
                placeholderRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        placeholderRef.addValueEventListener(placeHolderListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        placeholderRef.removeEventListener(placeHolderListener);
    }

    private Integer getAge (String birthdate){
        //estrae i numeri dalla stringa
        String parts [] = birthdate.split("/");
        //li casta in interi
        Integer day = Integer.parseInt(parts[0]);
        Integer month = Integer.parseInt(parts[1]);
        Integer year = Integer.parseInt(parts[2]);

        //oggetto per l'anno di nascita
        Calendar dob = Calendar.getInstance();
        //oggetto per l'anno corrente
        Calendar today = Calendar.getInstance();

        //setta anno di nascita in formato data
        dob.set(year,month,day);
        //calcola l'anno
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        //controlla che il giorno attuale sia minore del giorno del compleanno
        //nel caso in cui fosse vero allora il compleanno non è ancora passato e il conteggio degli anni viene diminuito
        if (today.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        //restituisce l'età sotto forma numerica utile per calcolare l'età media dei partecipanti ad un evento
        return age;

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


}
