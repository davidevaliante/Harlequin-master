package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Registration extends Activity {

    private String userName;
    private String userSurname;
    private int userAge;
    private String userCity;
    private EditText mUserName;
    private EditText mUserSurname;
    private EditText mUserCity;
    private EditText mUserAge;
    private EditText mUserEmail;
    private EditText mUserPassword;
    private EditText mUserPasswordConfirmed;
    private FirebaseAuth myAuth;
    private FirebaseAuth.AuthStateListener myAuthListener;
    private String TAG = "TAAAAAAAAAAAAAAAAAAAAG";
    private DatabaseReference myDatabase;
    private FirebaseUser myUser;
    private String userEmail, userPassword,userPasswordConfirm;


    //TODO implementare l'immagine di profilo

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //UI utente
        mUserName = (EditText) findViewById(R.id.userName);
        mUserSurname = (EditText) findViewById(R.id.userSurname);
        mUserCity = (EditText) findViewById(R.id.userCity);
        mUserAge = (EditText) findViewById(R.id.userAge);
        mUserEmail = (EditText)findViewById(R.id.userEmail);
        mUserPassword = (EditText)findViewById(R.id.userPassword);
        mUserPasswordConfirmed = (EditText)findViewById(R.id.userPasswordConfirm);
        Button registration = (Button) findViewById(R.id.regButton);

        //Riferimento al root del database di Firebase
        myDatabase = FirebaseDatabase.getInstance().getReference();

        //Riferimento al sistema di autenticazione di Firebase
        myAuth = FirebaseAuth.getInstance();


        //[START] authListener
        myAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser myUser = firebaseAuth.getCurrentUser();
                    if (myUser != null) {
                        // User is signed in
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + myUser.getUid());
                    } else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                    }

                }
        };
        //[END] authListener

        //[START] Button for Signing Up
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //dati di registrazione utente letti dagli EditText
                userAge = Integer.parseInt(mUserAge.getText().toString());
                userCity = mUserCity.getText().toString().trim();
                userName = mUserName.getText().toString().trim();
                userSurname = mUserSurname.getText().toString().trim();
                userPassword = mUserPassword.getText().toString().trim();
                userPasswordConfirm = mUserPasswordConfirmed.getText().toString().trim();
                userEmail = mUserEmail.getText().toString().trim();

                //Controlla prima di tutto che le password combacino
                if(userPassword.equals(userPasswordConfirm)) {

                    //controlla che tutti i fields siano compilati correttamente
                    if (!TextUtils.isEmpty(userCity) && !TextUtils.isEmpty(userName) &&
                            !TextUtils.isEmpty(userSurname)) {
                        //crea un nuovo utente con mail e password
                        myAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                              .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                              @Override
                              public void onComplete(@NonNull Task<AuthResult> task) {

                                  Log.v(TAG, "createUserWithEmail: onComplete : " + task.isSuccessful());
                                  if (!task.isSuccessful()) {
                                  Toast.makeText(Registration.this, "Registrazione fallita", Toast.LENGTH_LONG).show();
                                  } else {
                                  myUser = FirebaseAuth.getInstance().getCurrentUser();
                                  writeNewUser(userName, userEmail, userAge, userCity, userSurname);
                                  Toast.makeText(Registration.this, "Registrazione effettuata", Toast.LENGTH_LONG).show();
                                  Intent userPageSwitch = new Intent(Registration.this, UserPage.class);
                                  startActivity(userPageSwitch);
                                      finish();
                                  }


                              }
                        });
                    } else {
                        Toast.makeText(Registration.this, "Tutti i campi sono obbligatori", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Registration.this,"La password non corrisponde", Toast.LENGTH_LONG).show();
                }
            }
        });
        //[END] Registration Button implementation
    }


    //[START] Scrive Utente nel database
    private void writeNewUser ( String Name, String Email,int age, String City, String Surname){

        if(myUser!=null){
            userEmail = mUserEmail.getText().toString().trim();
            userCity = mUserCity.getText().toString();
            userName = mUserName.getText().toString();
            userSurname = mUserSurname.getText().toString();
            userAge = Integer.parseInt(mUserAge.getText().toString());
            String userId = myUser.getUid();


        User user = new User(userName,userEmail,userAge,userCity,userSurname);
        myDatabase.child("Users").child(userId).setValue(user);
        }
        else if(myUser==null){
            Toast.makeText(Registration.this,"Errore, Account non creato",Toast.LENGTH_LONG).show();
        }

    }//[END]

    @Override
    public void onStart() {
        super.onStart();
        myAuth.addAuthStateListener(myAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (myAuthListener != null) {
            myAuth.removeAuthStateListener(myAuthListener);
        }
    }


}
//[END]REGISTRATION.class