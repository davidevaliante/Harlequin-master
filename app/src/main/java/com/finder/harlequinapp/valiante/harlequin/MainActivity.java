package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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


public class MainActivity extends Activity {

    private Button mSignIn;
    private Button mSignUp;
    private EditText mEmailField;
    private EditText mPasswordField;
    private String userEmailString;
    private String userPasswordString;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ProgressDialog mProgressDialog;

    //TODO customizzare la actionBar
    //TODO eseguire l'upgrade nel gradle delle librerie cardview,design e appcompatv-7


    //prova commit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //elementi dell'UI
        mSignIn = (Button)findViewById(R.id.signIn);
        mSignUp = (Button)findViewById(R.id.signUp);
        mEmailField = (EditText)findViewById(R.id.emailField);
        mPasswordField = (EditText)findViewById(R.id.passwordField);
        mProgressDialog = new ProgressDialog(this);

        //elementi Firebase
        mAuth = FirebaseAuth.getInstance();



        //[START] Pulsante di Login
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        //[END] Pulsante di Login

        //[START]pulsante registrazione
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registrationIntent = new Intent ( MainActivity.this, Registration.class);
                registrationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(registrationIntent);
            }
        });
        //[END]pulsante di registrazione


        //[START] inizializza l'ascoltatore per il corretto Login
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    Log.v("MMMMMMMMMMMMMMMMMM", "onAuthStateChanged:signed_in:" + user.getUid());

                    Intent intent = new Intent(MainActivity.this,UserPage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }else{
                    // User is signed out
                    Log.v("MMMMMMMMMMMMMMM", "onAuthStateChanged:signed_out");
                }
            }
        };
        //[END] Fine Auth Listener
    }

    //inizializza l'authlistener al nostro riferimento di classe per l'autenticazione
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
    //interrompe l'Authlistener dal riferimento di classe
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    //[START] Metodo per loggare con email e password
    public void signIn(){

        mProgressDialog.setMessage("Eseguendo l'accesso");
        mProgressDialog.show();

        userEmailString = mEmailField.getText().toString();
        userPasswordString = mPasswordField.getText().toString();

        if(!TextUtils.isEmpty(userEmailString) || !TextUtils.isEmpty(userPasswordString)) {

            //barra di dialogo per il login


            mAuth.signInWithEmailAndPassword(userEmailString, userPasswordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "LogIn fallito", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();


                    }
                    else if(task.isSuccessful()){


                        Intent userPageSwitch = new Intent(MainActivity.this,UserPage.class);
                        startActivity(userPageSwitch);
                        mProgressDialog.dismiss();


                    }
                }
            });
        }else{

            Toast.makeText(MainActivity.this,"Riempi tutti i campi",Toast.LENGTH_SHORT).show();
        }

    }
    //[END] Login method


}
//[END] MainActivity.class
