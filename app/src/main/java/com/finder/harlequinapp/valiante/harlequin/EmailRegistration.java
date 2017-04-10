package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bruce.pickerview.LoopScrollListener;
import com.bruce.pickerview.LoopView;
import com.bruce.pickerview.popwindow.DatePickerPopWin;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;

public class EmailRegistration extends AppCompatActivity {

    private EditText name, surname, mail, password, confirmedPassword, city, birthdate;
    private Button submit;
    private MaterialTextField materialBirthdate;
    private ImageButton imagePicker;
    private static final int GALLERY_REQUEST_CODE = 1;
    private Uri mCropImageUri;
    private FirebaseAuth mAuth;
    private DatabaseReference mReference;
    private Boolean isSingle = true;
    private Boolean isMale = true;
    private RadioRealButtonGroup relGroup, genderGroup;
    private StorageReference picReference;
    private Uri profileImageUrl;
    private ProgressDialog progressDialog;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private final static String TAG = "AuthState: ";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_registration);

        //elementi UI
        name = (EditText)findViewById(R.id.profileName);
        surname = (EditText)findViewById(R.id.profileSurname);
        mail = (EditText)findViewById(R.id.profileMail);
        password = (EditText)findViewById(R.id.profilePassword);
        confirmedPassword = (EditText)findViewById(R.id.profilePasswordConfirm);
        city = (EditText)findViewById(R.id.profileCity);
        birthdate = (EditText)findViewById(R.id.profileBirthday);
        submit = (Button) findViewById(R.id.submit_profile);
        materialBirthdate = (MaterialTextField)findViewById(R.id.materialBirthdate);
        imagePicker = (ImageButton)findViewById(R.id.profileProfile);
        relGroup = (RadioRealButtonGroup) findViewById(R.id.relGroup);
        genderGroup = (RadioRealButtonGroup) findViewById(R.id.genderGroup);
        progressDialog = new ProgressDialog(EmailRegistration.this);

        //endpoint Firebase
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference().child("Users");
        picReference = FirebaseStorage.getInstance().getReference().child("Profile_pictures");

        //listener per il login con Firebase
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        // ...

        //Builder per il datepicker a scorrimento
        final DatePickerPopWin pickerPopWin = new DatePickerPopWin.Builder(EmailRegistration.this, new DatePickerPopWin.OnDatePickedListener() {
            @Override
            public void onDatePickCompleted(int year, int month, int day, String dateDesc) {
                birthdate.setText(day+"/"+month+"/"+year);

            }
        }).textConfirm("Conferma") //text of confirm button
        .textCancel("Annulla") //text of cancel button
        .btnTextSize(16) // button text size
        .viewTextSize(25)// pick view text size
        .colorCancel(Color.parseColor("#999999")) //color of cancel button
        .colorConfirm(ContextCompat.getColor(this,R.color.colorPrimary))//color of confirm button
        .minYear(1950) //min year in loop
        .maxYear(2005) // max year in loop
        .showDayMonthYear(true) // shows like dd mm yyyy (default is false)
        .dateChose("1995/01/01") // date chose when init popwindow
        .build();

        //per scegliere il sesso
        genderGroup.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                switch(position){
                    case 0:
                        isMale=true;
                        break;
                    case 1:
                        isMale=false;
                        break;
                    default:
                        isMale=true;
                }
            }
        });

        relGroup.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                switch(position){
                    case 0:
                        isSingle=true;
                        break;
                    case 1:
                        isSingle=false;
                        break;
                    default:
                        isSingle=true;
                }
            }
        });

        //quando il material Text Field viene aperto c'è una schermata di pop up con il picker
        birthdate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                pickerPopWin.showPopWin(EmailRegistration.this);
                //rimuove immediatamente il focus per evitare che venga editato
                birthdate.clearFocus();
            }
        });

        //pulsante per il submit del profilo
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //controllo prima del submit
                if(checkIfIsSubmitable()){
                    //pusha il profilo
                    writeNewUser();
                }
                //gli eventuali errori vengono gia segnalati dal metodo di check
                else{
                    Toast.makeText(EmailRegistration.this, "problem", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent gallery = new Intent();
                    gallery.setAction(Intent.ACTION_GET_CONTENT);
                    gallery.setType("image/*");
                    startActivityForResult(gallery,GALLERY_REQUEST_CODE);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Handle dell'immagine selezionata
        if(requestCode==GALLERY_REQUEST_CODE && resultCode==RESULT_OK){
            //Uri dell'immagine scelta
            mCropImageUri = data.getData();
            //inizia la cropImageActivity
            CropImage.activity(mCropImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setBorderLineColor(ContextCompat.getColor(this,R.color.colorPrimary))
                    .setAspectRatio(1,1)
                    .start(this);
        }

        //Handle del risultato di CropImage
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mCropImageUri = result.getUri();
                imagePicker.setImageURI(mCropImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }



    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
}

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    protected void writeNewUser(){
        progressDialog.setMessage("Creazione in corso");
        progressDialog.show();
        final String userName = capitalize(name.getText().toString().trim());
        final String userSurname = capitalize(surname.getText().toString().trim());
        final String userBirthdate = birthdate.getText().toString().trim();
        final String userMail = mail.getText().toString().trim();
        final String userPassword = password.getText().toString().trim();
        String userConfirm = confirmedPassword.getText().toString().trim();
        final String userCity = capitalize(city.getText().toString().trim());
        final String userGender;
        final String userRel;
        String avatar = mCropImageUri.toString();

        //setta il sesso
        if(isMale){
            userGender="Uomo";
        }else{
            userGender="Donna";
        }

        //setta situazione sentimentale a seconda del sesso
        if(isSingle){
            userRel="Single";
        }else{
            if(isMale){
               userRel="Impegnato";
            }else{
                userRel="Impegnata";
            }
        }

        //inizio upLoad immagine
        mAuth.createUserWithEmailAndPassword(userMail,userPassword)
             .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     picReference.child(mCropImageUri.getLastPathSegment()+userName+userSurname).putFile(mCropImageUri)
                             .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                 @Override
                                 public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                     profileImageUrl = taskSnapshot.getDownloadUrl();
                                     if(profileImageUrl!=null) {
                                         String userId = mAuth.getCurrentUser().getUid();
                                         User newUser = new User(userName, userMail, userBirthdate, userCity, userSurname, profileImageUrl.toString(), userRel, userGender, "NA", "NA");
                                         mReference.child(userId).setValue(newUser);
                                         Intent userPageSwitch = new Intent(EmailRegistration.this, MainUserPage.class);
                                         startActivity(userPageSwitch);
                                         progressDialog.dismiss();
                                         Toast.makeText(EmailRegistration.this, "Registrazione effettuata !", Toast.LENGTH_SHORT).show();

                                     }else{
                                         Toast.makeText(EmailRegistration.this, "Controlla la tua connessione e riprova", Toast.LENGTH_SHORT).show();
                                     }
                                 }

                             });
                 }
             });





    }

    //controlla se tutti i campi sono stati riempiti correttamente e se le password coincidono
    protected boolean checkIfIsSubmitable(){
        //di base il metodo restituisce true, anche solo una condizione può far fallire il metodo
        boolean canSubmit = true;
        //tutti i campi devono essere riempiti
        if(name.getText().toString().trim().length()==0              ||
           surname.getText().toString().trim().length()==0           ||
           mail.getText().toString().trim().length()==0              ||
           password.getText().toString().trim().length()==0          ||
           confirmedPassword.getText().toString().trim().length()==0 ||
           city.getText().toString().trim().length()==0              ||
           birthdate.getText().toString().trim().length()==0         ||
           city.getText().toString().trim().length()==0){
            canSubmit = false;
            Toast.makeText(this, "Riempi tutti i campi e riprova", Toast.LENGTH_SHORT).show();
        }

        //la password deve coincidere
        if(!TextUtils.equals(password.getText().toString().trim(),confirmedPassword.getText().toString().trim())){
            canSubmit = false;
            Toast.makeText(this, "La password non corrisponde", Toast.LENGTH_SHORT).show();
        }

        //la password deve essere di almeno sei caratteri
        if(password.getText().toString().trim().length() < 6){
            Toast.makeText(this, "La password deve essere di almeno sei caratteri", Toast.LENGTH_SHORT).show();
        }

        if(mCropImageUri==null || mCropImageUri.toString().length()==0){
            Toast.makeText(this, "Immagine di profilo mancante", Toast.LENGTH_SHORT).show();
            canSubmit=false;
        }
     return canSubmit;
    }

    //per rendere maiuscola la prima lettera di una stringa
    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
