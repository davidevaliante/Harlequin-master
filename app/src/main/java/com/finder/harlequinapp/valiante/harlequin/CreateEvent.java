package com.finder.harlequinapp.valiante.harlequin;


import android.app.DatePickerDialog;
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
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private static final int galleryRequest = 1;


    //TODO aggiungere immagine selezionata al database







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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myDatabase = FirebaseDatabase.getInstance().getReference();





        //[START inizializzazione Database]
        DatabaseReference mReference =  FirebaseDatabase.getInstance().getReference();
        //[END  inizializzazione Database]

        //[SETTA AUTOMATICAMENTE IL NOME DEL CREATORE RITROVANDOLO NEL DATABASE]
        userId = user.getUid();
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
        //[END]

        //[START] SELECTIMAGE fa selezionare un immagine dalla galleria
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
        month = mcurrentDate.get(Calendar.MONTH) + 1;
        day = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        eventDate.setHint("" + day + "/" + month + "/" + year);
        //roba del calendario per l'orario
        Calendar mcurrentTime = Calendar.getInstance();
        hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mcurrentTime.get(Calendar.MINUTE);
        eventTime.setText(setCorrectTime(hour,minute));



        //[START] DATEPICKER fa scegliere la data ed edita il field corrispettivo
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        year = selectedyear;
                        month = selectedmonth + 1;
                        day = selectedday;

                        eventDate.setText("" + day + "/" + month + "/" + year);

                    }
                }, year, month, day);
                mDatePicker.setTitle("Seleziona una data");
                mDatePicker.show();
            }


        });
        //[END DATEPICKER]

        //[START] TIMEPICKER
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
        //[END] TIMEPICKER


        //[START]SubmitEvent button
        submitEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeEvent();
            }
        });
        //[END]SUBMIT EVENT


    }//[END ON CREATE]


    //[START] Metodo per la corretta formattazione dell'orario ()senza di questo il datepicker restituiva
    //ad esempio 0:9 per indicare le 00:09
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

    //[START] Scrive l'evento nel database
    private void writeEvent (){
        String userEventName = eventName.getText().toString().trim();
        String userCreatorName = myusername+" "+myusersurname;
        String userDescriptionName = eventDescription.getText().toString().trim();
        String userEventDate = eventDate.getText().toString();
        String userEventTime = eventTime.getText().toString();

        Event newEvent = new Event(userEventName,userCreatorName,userDescriptionName,userEventDate,userEventTime,userId);
        myDatabase.child("Events").child(userCreatorName).child(userEventName).setValue(newEvent);
    }
    //[END] scrive l'evento nel database

    //[START] IMMAGINE EVENTO gestione del selezionatore della foto e del cropper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if(requestCode==galleryRequest && resultCode==RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropResultSize(100,100)
                    .setMaxCropResultSize(750,500)
                    .setMinCropWindowSize(0,0)

                    //.setAspectRatio(1,1); setta delle impostazioni per il crop
                    //TODO studiare meglio la riga di sopra andando sul gitHub wiki che hai salvato fra i preferiti
                    .start(this);


            eventImage.setImageURI(imageUri);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                eventImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //[END] SELEZIONATORE E CROPPER IMMAGINE



}//[END CreateEvent.class]
