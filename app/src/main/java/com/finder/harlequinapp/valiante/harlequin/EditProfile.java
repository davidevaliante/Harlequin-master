package com.finder.harlequinapp.valiante.harlequin;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOError;
import java.util.Calendar;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;

public class EditProfile extends AppCompatActivity {

    private EditText changeName,changeSurname,changeCity,changeBirthday;
    private RadioRealButton change_single,change_engaged;
    private RadioRealButtonGroup relGroup;
    private MaterialTextField materialName;
    private Button submitChanges;
    private DatabaseReference userReference, myDatabase;
    private ValueEventListener mUserDataListener;
    private String userId;
    private SharedPreferences userData;
    private SharedPreferences.Editor editor;
    private User userClass, changedUser;
    private String myuserName;
    private Boolean isMale,isSingle;
    private Integer userAge;
    private String userRelationship;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userData = getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        editor = userData.edit();

        myDatabase = FirebaseDatabase.getInstance().getReference();
        changeName = (EditText)findViewById(R.id.changeName);
        changeSurname = (EditText)findViewById(R.id.changeSurname);
        changeCity = (EditText)findViewById(R.id.changeCity);
        changeBirthday = (EditText)findViewById(R.id.changeBirthday);
        relGroup = (RadioRealButtonGroup)findViewById(R.id.changeRel);
        change_single = (RadioRealButton)findViewById(R.id.changeSingle);
        change_engaged = (RadioRealButton)findViewById(R.id.changeEngaged);
        submitChanges = (Button)findViewById(R.id.sumbit_changes);

        loadUserData();

        relGroup.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if(position == 0){
                   isSingle = true;
                }
                else{
                    isSingle = false;
                }
            }
        });

        changeBirthday.setOnClickListener(new View.OnClickListener() {
            Integer year = 1995;
            Integer month = 0;
            Integer day = 1;
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(EditProfile.this, new DatePickerDialog.OnDateSetListener() {
                    //quando viene premuto "ok"..
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        year = selectedyear;
                        month = selectedmonth;
                        day = selectedday;

                        changeBirthday.setText("" + day + "/" + (month+1) + "/" + year);


                    }
                }, year,month,day);
                mDatePicker.show();

            }
        });

        submitChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isSubmitable()){
                    if(isSingle){
                        userRelationship = "Single";
                    }
                    else{
                        if(userClass.getUserGender().equalsIgnoreCase("Uomo")) {
                            userRelationship = "Impegnato";
                        }else{
                            userRelationship = "Impegnata";
                        }
                    }
                    changedUser = new User(capitalize(changeName.getText().toString().trim()),
                                            userClass.getUserEmail(),
                                            changeBirthday.getText().toString(),
                                            capitalize(changeCity.getText().toString().trim()),
                                            capitalize(changeSurname.getText().toString().trim()),
                                            userClass.getProfileImage(),
                                            userRelationship,
                                            userClass.getUserGender(),
                                            userClass.getFacebookProfile(),
                                            userClass.getAnonymousName(),
                                            userClass.getUserToken());
                    myDatabase.child("Users").child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            myDatabase.child("Users").child(userId).setValue(changedUser);
                        }
                    });
                    Toast.makeText(EditProfile.this, "Profilo salvato !", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(EditProfile.this, "Riempi tutti i campi e riprova", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    protected Boolean isSubmitable(){
        boolean canSubmit = false;
        try{
            if(isEmpty(changeName) || isEmpty(changeSurname) || isEmpty(changeCity) || isEmpty(changeBirthday)){
                //non può essere inoltrato
            }
            else{
                canSubmit = true;
            }
        }
        catch(IOError e){

        }finally{
            return canSubmit;
        }
    }

    protected boolean isEmpty(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 0;
    }


    protected void loadUserData()
    {
        //prende l'utente in firebase
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //se l'utente  correttamente loggato
        if(currentUser !=null) {
            userId = currentUser.getUid();
            if (!userId.isEmpty()) {
                userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            }
        }

        //listener per caricare dalla toolBar
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User myuser = dataSnapshot.getValue(User.class);
                userClass = myuser;
                myuserName = myuser.getUserName();
                final String relationshipStatus = myuser.getUserRelationship();
                final String avatarUrl = myuser.getProfileImage();
                String userName = myuser.getUserName()+" "+myuser.getUserSurname();
                String userCity = myuser.getUserCity();
                String userGender = myuser.getUserGender();
                changeCity.setText(userCity);
                changeName.setText(myuserName);
                changeSurname.setText(myuser.getUserSurname());
                changeBirthday.setText(myuser.getUserAge());

                editor.putString("USER_NAME",myuserName);
                editor.putString("USER_CITY",userCity);


                //controlla il sesso

                if(userGender.equalsIgnoreCase("Uomo")){
                    //va bene così isMale è settato di default su maschio
                    editor.putBoolean("IS_MALE", true);
                }
                if(userGender.equalsIgnoreCase("Donna")){
                    editor.putBoolean("IS_MALE", false);
                    isMale = false;
                }

                //imposta situazione sentimentale
                if(relationshipStatus.equalsIgnoreCase("Impegnato")
                        || relationshipStatus.equalsIgnoreCase("Impegnata")){
                    isSingle = false;

                    editor.putBoolean("IS_SINGLE",false);
                }else{
                    editor.putBoolean("IS_SINGLE",true);
                }

                //imposta situazione sentimentale
                if(relationshipStatus.equalsIgnoreCase("Single")){
                    isSingle = true;

                    editor.putBoolean("IS_SINGLE",true);
                }else{
                    editor.putBoolean("IS_SINGLE",false);
                }

                userAge = getAge(myuser.getUserAge());
                editor.putInt("USER_AGE",userAge);
                editor.putString("USER_ID",dataSnapshot.getKey());
                editor.commit();
                myDatabase.child("Users").child(userId).removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //reference per renderlo removibile
        mUserDataListener = userDataListener;
        //preleva nome dall'Auth personalizzando l'actionBar e i dati principali dell'utente
        myDatabase.child("Users").child(userId).addValueEventListener(userDataListener);
    }

    //calcola l'età da String a Integer
    public Integer getAge (String birthdate){
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

    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

}
