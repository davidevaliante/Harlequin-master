package com.finder.harlequinapp.valiante.harlequin;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.github.jorgecastilloprz.listeners.FABProgressListener;


import static com.finder.harlequinapp.valiante.harlequin.R.id.fabProgressCircle;


public class Registration extends Activity {

    //variabili di classe

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
    private String userName, userSurname,userEmail, userPassword,userPasswordConfirm,profileImage;
    private TextInputLayout inputsurname,inputname,inputcity,inputage,inputpass,inputpassconfirm,inputmail;
    private FloatingActionButton registration;
    private CircularImageView mImageButton;
    private final static int GALLERY_REQUEST = 1;
    private Uri imageUri, cropImageResultUri, downloadUrl;

    private FABProgressCircle fabProgressCircle;





    //TODO impedire all'utente di tornare alla pagina di registrazione e alla main activity

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
        mImageButton = (CircularImageView)findViewById(R.id.imageButton);
        registration = (FloatingActionButton)findViewById(R.id.regButton);
        fabProgressCircle = (FABProgressCircle)findViewById(R.id.fabProgressCircle);

        //Inizializzazione dei TextInputLayout
        inputname = (TextInputLayout)findViewById(R.id.input_layout_name);
        inputsurname = (TextInputLayout)findViewById(R.id.input_layout_surname);
        inputcity = (TextInputLayout)findViewById(R.id.input_layout_city);
        inputage = (TextInputLayout)findViewById(R.id.input_layout_age);
        inputmail = (TextInputLayout)findViewById(R.id.input_layout_mail);
        inputpass = (TextInputLayout)findViewById(R.id.input_layout_password);
        inputpass= (TextInputLayout)findViewById(R.id.input_layout_password);
        inputpassconfirm = (TextInputLayout)findViewById(R.id.input_layout_passwordconfirm);

        //inizializza i textchangedListener agli editText
        mUserName.addTextChangedListener(new MyTextWatcher(mUserName));
        mUserSurname.addTextChangedListener(new MyTextWatcher(mUserSurname));
        mUserCity.addTextChangedListener(new MyTextWatcher(mUserCity));
        mUserAge.addTextChangedListener(new MyTextWatcher(mUserAge));
        mUserEmail.addTextChangedListener(new MyTextWatcher(mUserEmail));
        mUserPassword.addTextChangedListener(new MyTextWatcher(mUserPassword));
        mUserPassword.addTextChangedListener(new MyTextWatcher(mUserPasswordConfirmed));

        //Riferimento al root del database di Firebase
        myDatabase = FirebaseDatabase.getInstance().getReference();
        final StorageReference profilePictures = FirebaseStorage.getInstance().getReference().child("Profile_pictures");
        //Riferimento al sistema di autenticazione di Firebase
        myAuth = FirebaseAuth.getInstance();

        myAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
              myUser = firebaseAuth.getCurrentUser();
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

        //Pulsante Registrazione
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //controlla che tutti i campi richiesti siano pieni
                if (!validateName()) {return;
                }
                if (!validateSurname()) {return;
                }
                if (!validateCity()) {return;
                }
                if (!validateAge()) {return;
                }
                if (!validateEmail()) {return;
                }
                if (!validatePassword()) {return;
                }

                //dati di registrazione utente letti dagli EditText
                userAge = Integer.parseInt(mUserAge.getText().toString());
                userCity = mUserCity.getText().toString().trim();
                userName = mUserName.getText().toString().trim();
                userSurname = mUserSurname.getText().toString().trim();
                userPassword = mUserPassword.getText().toString().trim();
                userPasswordConfirm = mUserPasswordConfirmed.getText().toString().trim();
                userEmail = mUserEmail.getText().toString().trim();
                profileImage = cropImageResultUri.toString();

                //Controlla prima di tutto che le password combacino
                if(userPassword.equals(userPasswordConfirm)) {
                    if(profileImage != null){
                    //se tutto va bene, allora barra utente
                      fabProgressCircle.show();
                        //crea un nuovo utente con mail e password
                      myAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                      .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {
                          Log.v(TAG, "createUserWithEmail: onComplete : " + task.isSuccessful());
                          if (!task.isSuccessful()) {
                          Toast.makeText(Registration.this, "Registrazione fallita", Toast.LENGTH_LONG).show();
                          } else {
                          //caricafoto profilo nello storage
                          profilePictures.child(imageUri.getLastPathSegment()).putFile(cropImageResultUri)
                          .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          //se l'Upload è andato a buon fine conserva l'url di download nella
                          // variabile di classe
                          myUser = FirebaseAuth.getInstance().getCurrentUser();
                          downloadUrl = taskSnapshot.getDownloadUrl();
                               if(downloadUrl!=null){
                                     writeNewUser(userName, userEmail, userAge, userCity, userSurname,downloadUrl.toString());
                                         fabProgressCircle.beginFinalAnimation();
                                         Toast.makeText(Registration.this, "Registrazione effettuata", Toast.LENGTH_LONG).show();
                                         Intent userPageSwitch = new Intent(Registration.this, UserPage.class);
                                         startActivity(userPageSwitch);
                               }
                               if(downloadUrl==null){
                                      Toast.makeText(Registration.this, "Registrazione fallita", Toast.LENGTH_LONG).show();
                               }
                          }
                          });
                          }
                      }
                      });
                    }else{
                       Toast.makeText(Registration.this,"Scegli l'immagine di profilo",Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(Registration.this,"La password non corrisponde", Toast.LENGTH_LONG).show();
                }
            }
        });
        //[END] Registration Button implementation

        //Image selection Button
        /*mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profilePicture = new Intent(Intent.ACTION_GET_CONTENT);
                profilePicture.setType("image/*");
                startActivityForResult(profilePicture, GALLERY_REQUEST);
                //OnActivity result sta sotto
            }
        });*/


    }//[END] di onCreate

    public void selectImage(View view){
        Intent profilePicture = new Intent(Intent.ACTION_GET_CONTENT);
        profilePicture.setType("image/*");
        startActivityForResult(profilePicture, GALLERY_REQUEST);
    }


    private void writeNewUser ( String Name, String Email,int age, String City, String Surname, String Image){

        if(myUser!=null){
            //dati necessari a registrare l'utente
            userEmail = Email;
            userCity = City;
            userName = Name;
            userSurname = Surname;
            userAge = age;
            profileImage = Image;
            String userId = myUser.getUid();

            //Crea un nuovo User con i dati appena reperiti
            User user = new User(userName,userEmail,"inserire età",userCity,userSurname,profileImage,"non specificato","non specificato","non specificato");
            //scrive il nuovo utente nel database usando l'ID
            myDatabase.child("Users").child(userId).setValue(user);
        } //TODO da controlla questo warning
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
    //Risultato immagine galleria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK){
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setBorderLineColor(ContextCompat.getColor(this,R.color.colorPrimary))
                    .setAspectRatio(1,1)
                    //.setAspectRatio(1,1); setta delle impostazioni per il crop
                    //TODO studiare meglio la riga di sopra andando sul gitHub wiki che hai salvato fra i preferiti
                    .start(this);
            mImageButton.setImageURI(imageUri);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropImageResultUri = result.getUri();
                mImageButton.setImageURI(cropImageResultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Classe per il controllo dei dati da immettere nel database
    private class MyTextWatcher implements TextWatcher {
        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.userName:
                    validateName();
                    break;
                case R.id.userSurname:
                    validateSurname();
                    break;
                case R.id.userCity:
                    validateCity();
                    break;
                case R.id.userAge:
                    validateAge();
                    break;
                case R.id.userEmail:
                    validateEmail();
                    break;
                case R.id.userPassword:
                    validatePassword();
                    break;

            }
        }
    }//[END]MyTextWatcher

    private boolean validateName() {
        if (mUserName.getText().toString().trim().isEmpty()) {
            inputname.setError("Inserisci il tuo nome");
            mUserName.requestFocus();
            return false;
        } else {
            inputname.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateSurname() {
        if (mUserSurname.getText().toString().trim().isEmpty()) {
            inputsurname.setError("Inserisci il tuo cognome");
            mUserSurname.requestFocus();
            return false;
        } else {
            inputsurname.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateCity() {
        if (mUserCity.getText().toString().trim().isEmpty()) {
            inputcity.setError("Inserisci la tua città");
            mUserCity.requestFocus();
            return false;
        } else {
            inputcity.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateAge() {
        if (mUserAge.getText().toString().trim().isEmpty()) {
            inputage.setError("Inserisci la tua età");
            mUserAge.requestFocus();
            return false;
        } else {
            inputage.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail() {
        if (mUserEmail.getText().toString().trim().isEmpty()) {
            inputmail.setError("Inserisci la tua mail");
            mUserEmail.requestFocus();
            return false;
        } else {
            inputmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword() {
        if (mUserPassword.getText().toString().trim().isEmpty()) {
            inputpass.setError("Inserisci una password");
            mUserPassword.requestFocus();
            return false;
        } else {
            inputpass.setErrorEnabled(false);
        }
        return true;
    }

}
//[END]REGISTRATION.class