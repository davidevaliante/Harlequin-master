package com.finder.harlequinapp.valiante.harlequin;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;


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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;

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
    private static final int galleryRequest = 1;


    //TODO settare l'image cropper in modo che rientri perfettamente nella cardView
    //TODO implementare assolutamente onAuthStateListener per fixare database reference


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        //Elementi UI
        eventDescription = (EditText) findViewById(R.id.eDescription);
        eventCreatorName = (EditText) findViewById(R.id.cName);
        eventName = (EditText) findViewById(R.id.eName);
        eventImage = (ImageButton) findViewById(R.id.eventImage);
        eventDate = (EditText) findViewById(R.id.eventDate);
        eventTime = (EditText) findViewById(R.id.eventTime);
        submitEvent = (Button)findViewById(R.id.submitButton);

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
         mReference.child("Users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                        User myuser = dataSnapshot.getValue(User.class);
                        myusername = myuser.getUserName();
                        myusersurname = myuser.getUserSurname();
                       eventCreatorName.setText("Creato da : "+myusername+" "+myusersurname);
                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {
                      Toast.makeText(CreateEvent.this,"Fallimento",Toast.LENGTH_LONG).show();
                      }
                });

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

        //crea un filepath per l'immagine nello storage
        StorageReference eventImagePath = firebaseStorage.child("Event_Images").child(imageUri.getLastPathSegment());

        //prova ad eseguire l'upload
        eventImagePath.putFile(cropImageResultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                downloadUrl = taskSnapshot.getDownloadUrl();
                Event newEvent = new Event(userEventName,userCreatorName,userDescriptionName,userEventDate,userEventTime,userId,downloadUrl.toString());
                //inserisce i dati nel database
                myDatabase.child("Events").push().setValue(newEvent);
                Intent backToUserPage = new Intent(CreateEvent.this,UserPage.class);
                startActivity(backToUserPage);
                mProgressBar.dismiss();
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


            eventImage.setImageURI(imageUri);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropImageResultUri = result.getUri();
                eventImage.setImageURI(cropImageResultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




}//[END CreateEvent.class]
