package samplePackage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.finder.harlequinapp.valiante.harlequin.MainUserPage;
import com.finder.harlequinapp.valiante.harlequin.R;
import com.finder.harlequinapp.valiante.harlequin.User;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;

import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class EmailRegistration extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

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
        birthdate.setFocusable(false);
        birthdate.setClickable(true);

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
                    writeNewUser();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        // ...



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

        materialBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog
                        = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                        EmailRegistration.this,
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

        //quando il material Text Field viene aperto c'è una schermata di pop up con il picker
        birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog
                        = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                        EmailRegistration.this,
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

        //pulsante per il submit del profilo
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //controllo prima del submit
                if(checkIfIsSubmitable()){
                    progressDialog.setMessage("Creazione del profilo in corso");
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(mail.getText().toString().trim(),password.getText().toString().trim());
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

                Toasty.error(EmailRegistration.this,"Abilita le autorizzazione per accedere alla galleria",Toast.LENGTH_SHORT,true).show();

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

        File compressedImageFile = Compressor.getDefault(getApplication()).compressToFile(new File(mCropImageUri.getPath()));
        picReference.child(mCropImageUri.getLastPathSegment()+userName+userSurname).putFile(Uri.fromFile(compressedImageFile))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        profileImageUrl = taskSnapshot.getDownloadUrl();
                        if(profileImageUrl!=null) {
                            String userId = mAuth.getCurrentUser().getUid();
                            User newUser = new User(userName, userMail, userBirthdate, userCity, userSurname, profileImageUrl.toString(), userRel, userGender, "NA", "NA","no_token");
                            mReference.child(userId).setValue(newUser);
                            Intent userPageSwitch = new Intent(EmailRegistration.this, MainUserPage.class);
                            startActivity(userPageSwitch);
                            progressDialog.dismiss();
                            if(isMale) {
                                Toasty.success(EmailRegistration.this, "Benvenuto !", Toast.LENGTH_SHORT, true).show();
                            }else{
                                Toasty.success(EmailRegistration.this, "Benvenuta !", Toast.LENGTH_SHORT, true).show();

                            }

                        }else{
                            Toasty.error(EmailRegistration.this,"Controlla la tua connessione e riprova",Toast.LENGTH_SHORT,true).show();
                        }
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
            Toasty.error(EmailRegistration.this,"Riempi tutti i campi e riprova",Toast.LENGTH_SHORT,true).show();

        }

        //la password deve coincidere
        if(!TextUtils.equals(password.getText().toString().trim(),confirmedPassword.getText().toString().trim())){
            canSubmit = false;
            Toasty.error(EmailRegistration.this,"La password non corrisponde",Toast.LENGTH_SHORT,true).show();
        }

        //la password deve essere di almeno sei caratteri
        if(password.getText().toString().trim().length() < 6){
            Toasty.error(EmailRegistration.this,"La password deve contenere almeno sei caratteri",Toast.LENGTH_SHORT,true).show();
        }

        if(mCropImageUri==null || mCropImageUri.toString().length()==0){
            Toasty.error(EmailRegistration.this,"Seleziona un immagine di profilo",Toast.LENGTH_SHORT,true).show();
            canSubmit=false;
        }
     return canSubmit;
    }

    //per rendere maiuscola la prima lettera di una stringa
    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        materialBirthdate.expand();
        birthdate.setText("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
    }
}
