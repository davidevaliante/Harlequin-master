package com.finder.harlequinapp.valiante.harlequin;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
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

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class CreateEvent extends AppCompatActivity {

    private ImageButton eventImage;

    private EditText eventDescription;
    private EditText eventCreatorName;
    private EditText eventName;
    private EditText eventDate;
    private EditText eventTime;
    private Integer day,month,year,hour,minute;
    private DatabaseReference myDatabase;
    private Button submitEvent;
    private String userId, myusername,myusersurname;
    private Uri imageUri = null;
    private StorageReference firebaseStorage;
    private ProgressDialog mProgressBar;
    private Uri downloadUrl;
    private Uri cropImageResultUri;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String creatorAvatarPath;
    private static final int galleryRequest = 1;
    private byte[] byteImage;
    private RadioRealButtonGroup paymentGroup;
    private RadioRealButton freeButton, paymentButton;
    private boolean isFree = true;
    private EditText price;
    private LinearLayout priceLayout;
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 1;
    private Button geoButton;


    //TODO settare l'image cropper in modo che rientri perfettamente nella cardView
    //TODO implementare assolutamente onAuthStateListener per fixare database reference


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        //Elementi UI
        eventDescription = (EditText) findViewById(R.id.eDescription);

        eventName = (EditText) findViewById(R.id.eName);
        eventImage = (ImageButton) findViewById(R.id.eventImage);
        eventDate = (EditText) findViewById(R.id.eventDate);
        eventTime = (EditText) findViewById(R.id.eventTime);
        submitEvent = (Button)findViewById(R.id.submitButton);
        paymentGroup = (RadioRealButtonGroup)findViewById(R.id.costGroup);
        freeButton = (RadioRealButton)findViewById(R.id.freeRadioButton);
        paymentButton = (RadioRealButton)findViewById(R.id.payRadioButton);
        price = (EditText)findViewById(R.id.priceText);
        priceLayout = (LinearLayout)findViewById(R.id.priceLayout);
        priceLayout.setVisibility(GONE);
        geoButton = (Button)findViewById(R.id.geoButton);

        
        //Inizializzazione di Firebase per recuperare la directory in base all'uid
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //può ritornare null senza problemi
        myDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance().getReference();



        userId = user.getUid();

        //Setta la barra di caricamento
        mProgressBar = new ProgressDialog(this);

        //[START inizializzazione Database]
        DatabaseReference mReference =  FirebaseDatabase.getInstance().getReference();
        //[END  inizializzazione Database]

        //[SETTA AUTOMATICAMENTE IL NOME DEL CREATORE RITROVANDOLO NEL DATABASE]

        //fa selezionare un immagine dalla galleria
        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryRequest);
            }
        });

        //roba del calendario per la data
        Calendar mcurrentDate = Calendar.getInstance();
        year = mcurrentDate.get(Calendar.YEAR);
        month = mcurrentDate.get(Calendar.MONTH);
        day = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        eventDate.setText("" + day + "/" + (month+1) + "/" + year);
        //roba del calendario per l'orario
        Calendar mcurrentTime = Calendar.getInstance();
        hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mcurrentTime.get(Calendar.MINUTE);
        eventTime.setText(setCorrectTime(hour,minute));

        //radio button per evento a pagamento o gratuito :
        paymentGroup.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if(position == 0){
                    isFree = true;
                    priceLayout.setVisibility(GONE);
                }
                if(position == 1){
                    isFree = false;
                    priceLayout.setVisibility(VISIBLE);
                }
            }
        });


        //fa scegliere la data ed edita il field corrispettivo
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
                    //quando viene premuto "ok"..
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        year = selectedyear;
                        month = selectedmonth;
                        day = selectedday;

                        eventDate.setText("" + day + "/" + (month+1) + "/" + year);

                    }
                }, year, month, day);
                mDatePicker.show();
            }


        });

        //fa scegliere l'orario
        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEvent.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        eventTime.setText( setCorrectTime(selectedHour,selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Seleziona l'orario");
                mTimePicker.show();

            }
        });


        //t.TODO aggiungere condizioni minime per il posting
        //SubmitEvent button
        submitEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeEvent();
            }
        });

    }//[END ON CREATE]


    //formattazione dell'orario in modo corretto
    public String setCorrectTime(int hour, int minute){
        String correctTime = hour+":"+minute;
        if (hour < 10 && minute <10){
            correctTime="0"+hour+":"+"0"+minute;
            return correctTime;
        }
        if(hour < 10 && minute >=10){
            correctTime="0"+hour+":"+minute;
            return correctTime;
        }
        if (hour >= 10 && minute < 10){
            correctTime=hour+":"+"0"+minute;
            return correctTime;
        }
        else return correctTime;
    }
    //[END]setCorrectTime


    //Scrive l'evento nel database
    private void writeEvent (){

        mProgressBar.setMessage("Caricamento in corso");
        mProgressBar.show();


        //variabili necessarie a scrivere l'evento
        final String userEventName = eventName.getText().toString().trim();
        final String userCreatorName = myusername+" "+myusersurname;
        final String userDescriptionName = eventDescription.getText().toString().trim();
        final String userEventDate = eventDate.getText().toString();
        final String userEventTime = eventTime.getText().toString();
        final Integer likes =0;
        final Integer rlikes =0;


        Bitmap bitmap = eventImage.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();



        myDatabase.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                creatorAvatarPath = user.getProfileImage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //crea un filepath per l'immagine nello storage
        final StorageReference eventImagePath = firebaseStorage.child("Event_Images").child(imageUri.getLastPathSegment());


        //prova ad eseguire l'upload
        eventImagePath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                downloadUrl = taskSnapshot.getDownloadUrl();
                //crea l'evento dettagliato
                Event newEvent = new Event(userEventName,userCreatorName,userDescriptionName,userEventDate,userEventTime,
                        userId,downloadUrl.toString(),creatorAvatarPath,likes,rlikes);
                //crea una nuova referenza con un nuovo ID nel database
                DatabaseReference newEventReference = myDatabase.child("Events").push();
                //recupera l'Id appena creato da usare per far si che venga assegnato anche al microEvent
                String newPostPushId = newEventReference.getKey();
                //inserisce l'Event nel database
                newEventReference.setValue(newEvent);

                mProgressBar.dismiss();
                finish();
                System.exit(0);
            }
        });

     }


    //[START] IMMAGINE EVENTO gestione del selezionatore della foto e del cropper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==galleryRequest && resultCode==RESULT_OK){
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropWindowSize(250,250)

                    //.setAspectRatio(1,1); setta delle impostazioni per il crop
                    //TODO studiare meglio la riga di sopra andando sul gitHub wiki che hai salvato fra i preferiti
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropImageResultUri = result.getUri();
                eventImage.setImageURI(cropImageResultUri);
                eventImage.setDrawingCacheEnabled(true);
                eventImage.buildDrawingCache();


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    
}//[END CreateEvent.class]
