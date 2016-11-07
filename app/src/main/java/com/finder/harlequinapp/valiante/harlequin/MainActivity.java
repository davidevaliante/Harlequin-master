package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends Activity {

    private Button mSignIn;
    private Button mSignUp;
    private TextView mHarleeFont;
    private ImageView mymainLogo;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mdescriptionText;
    private String userEmailString;
    private String userPasswordString;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    //TODO devo aggiungere il lock nella UserPage per evitare che l'utente torni in questa pagina
    //TODO il lock dopo aver fatto il login adesso funziona ma solo se si spamma il pulsante indietro, va migliorato

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
                userEmailString = mEmailField.getText().toString();
                userPasswordString = mPasswordField.getText().toString();

                if(!TextUtils.isEmpty(userEmailString) && !TextUtils.isEmpty(userPasswordString)){

                Intent userPageSwitch = new Intent(MainActivity.this,Registration.class);
                userPageSwitch.putExtra("userEmail",userEmailString);
                userPageSwitch.putExtra("userPassword",userPasswordString);
                startActivity(userPageSwitch);
                }else{
                    Toast.makeText(MainActivity.this,"Inserisci dati validi",Toast.LENGTH_LONG).show();
                }
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


        userEmailString = mEmailField.getText().toString();
        userPasswordString = mPasswordField.getText().toString();

        if(!TextUtils.isEmpty(userEmailString) || !TextUtils.isEmpty(userPasswordString)) {


            mAuth.signInWithEmailAndPassword(userEmailString, userPasswordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "LogIn fallito", Toast.LENGTH_SHORT).show();
                    }
                    else if(task.isSuccessful()){


                        Intent userPageSwitch = new Intent(MainActivity.this,UserPage.class);
                        startActivity(userPageSwitch);

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
