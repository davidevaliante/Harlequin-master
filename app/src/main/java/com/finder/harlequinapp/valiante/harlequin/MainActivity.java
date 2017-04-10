package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

import static android.R.attr.data;


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
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private String TAG = "****FACEBOOK****";
    private StorageReference mStorage;
    private DatabaseReference mUserReference;
    private LoginResult facebookLoginResult ;
    private String userName ="";
    private String userSurname = "";
    private String userProfile = "";
    private String userLink = "";
    private String userGender = "";
    private TextView appName;
    private Integer MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    protected Snackbar mSnackbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //rende la statusbar completamente invisibile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }




        Typeface steinerlight = Typeface.createFromAsset(getAssets(),"fonts/Steinerlight.ttf");
        Typeface hero = Typeface.createFromAsset(getAssets(),"fonts/Hero.otf");

        //elementi dell'UI
        appName = (TextView) findViewById(R.id.app_name);
        mSignIn = (Button)findViewById(R.id.signIn);
        mSignUp = (Button)findViewById(R.id.signUp);
        mEmailField = (EditText)findViewById(R.id.emailField);
        mPasswordField = (EditText)findViewById(R.id.passwordField);
        mProgressDialog = new ProgressDialog(this);

        appName.setTypeface(steinerlight);
        mSignIn.setTypeface(hero);
        mSignUp.setTypeface(hero);


        //elementi Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_pictures");
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");


        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setTypeface(hero);
        //richiede i permessi di lettura a Facebook
        loginButton.setReadPermissions(
                 "email", "public_profile","user_birthday","user_location");

        //riceve le risposte dalla facebook SDK nell'activity
        callbackManager = CallbackManager.Factory.create();
        //pulsante per il login con facebook con un gestore di callback
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //onSuccess restituisce un token di accesso di facebook che viene passato al metodo
                handleFacebookAccessToken(loginResult.getAccessToken(),loginResult);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "facebook:onError", exception);
                Toast.makeText(MainActivity.this,"Accesso con Facebook fallito, riprova",Toast.LENGTH_SHORT).show();
            }
        });

        //Pulsante di Login per eMail e password
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //funzione di login per email e password
                signIn();
            }
        });
        //[END] Pulsante di Login

        //[START]pulsante registrazione
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUp = new Intent(MainActivity.this, EmailRegistration.class);
                startActivity(signUp);
            }
        });
        //[END]pulsante di registrazione

        //listener per il login in Firebase che aspetta risposte dal server
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //se viene passato un token di login valido
                if (firebaseAuth.getCurrentUser() != null ){
                    //inizializza un oggetto FirebaseUser per gestirlo
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    Log.v("MMMMMMMMMMMMMMMMMM", "onAuthStateChanged:signed_in:" + user.getUid());

                    //controlla che nel nodo "Users" ci sia l' ID utente
                    mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //se l'ID utente è presenta allora l'utente è correttamente registrato e viene mandato
                            //alla MainUserPage
                            if (dataSnapshot.hasChild(user.getUid())){
                                //TODO cambiato da UserPage a MainUserPage
                                Intent intent = new Intent(MainActivity.this,MainUserPage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                            //se invece l'id utente non è presente ma è presente un token di accesso di Facebook
                            else{
                                if(facebookLoginResult != null) {
                                    //crea un profilo momentaneo per il completamento del profilo
                                    getUserFromFacebook(facebookLoginResult, user);

                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }else{
                    Log.v("MMMMMMMMMMMMMMM", "onAuthStateChanged:signed_out");
                }
            }
        };
        //[END] Fine Auth Listener
    }//[FINE DI ONCREATE]

    //richiesta dati a facebook e creazione del placeholder profile
    private void getUserFromFacebook (final LoginResult loginResult, final FirebaseUser user){
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,gender,link");
        final User newUser= new User();

        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("***LoginResponse :",response.toString());

                        //try block per l'eccezione della lettura dei dati JSON
                        try {
                            //punta al nodo dei placeholder nel database
                            DatabaseReference placeholder = FirebaseDatabase.getInstance()
                                                                            .getReference()
                                                                            .child("placeholderProfile");
                            //legge i dati dalla risposta JSON
                            userGender =  response.getJSONObject().getString("gender");
                            userName=  response.getJSONObject().getString("first_name");
                            userSurname =  response.getJSONObject().getString("last_name");
                            userLink =  response.getJSONObject().getString("link");
                            Profile profile = Profile.getCurrentProfile();
                            String id = profile.getId();
                            String link = profile.getLinkUri().toString();
                            userProfile =  profile.getProfilePictureUri(200,200).toString();

                            //crea un oggetto utente con i dati appena recuperati
                            User facebookUser = new User (userName,"null","null","null",
                                                          userSurname,userProfile,"null",
                                                          genderFixer(userGender),userLink,"null");

                            //push il profilo placeholder utilizzando l'ID firebase come chiave
                            placeholder.child(user.getUid())
                                       .setValue(facebookUser)
                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                          @Override
                                          public void onComplete(@NonNull Task<Void> task) {
                                              //manda l'utente al completamento del profilo
                                              Intent completeProfile = new Intent(MainActivity.this, CompleteProfile.class);
                                              startActivity(completeProfile);
                                              Toast.makeText(MainActivity.this,
                                              "Completa la registrazione in pochi passi",
                                              Toast.LENGTH_SHORT).show();
                                          }
                                      });
                            Log.i("****Login"+ "FirstName", userName);
                            Log.i("****Login" + "LastName", userSurname);
                            Log.i("****Login" + "Gender", userGender);
                            Log.i("****Link",link);
                            Log.i("***Login"+"ProfilePic",userProfile );
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //metodo che scambia il token facebook per un token firebase
    private void handleFacebookAccessToken(AccessToken token, final LoginResult loginResult) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        //TODO creare l'utente
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "*********signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this,
                                           "Authentication failed.",
                                           Toast.LENGTH_SHORT).show();
                        }
                        if (task.isSuccessful()){
                            facebookLoginResult = loginResult;
                        }

                    }
                });
    }
    // [END auth_with_facebook]

    public String genderFixer (String gender){
        String fixedGender ="";
        if (gender.equalsIgnoreCase("female")){
            fixedGender = fixedGender+"Donna";
        }
        if (gender.equals("male")){
            fixedGender = fixedGender+"Uomo";
        }
        if (gender.isEmpty()){
            fixedGender = fixedGender+"Non specificato";
        }
        return fixedGender;
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

    //Metodo per loggare con email e password
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
                        Intent userPageSwitch = new Intent(MainActivity.this,MainUserPage.class);
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
