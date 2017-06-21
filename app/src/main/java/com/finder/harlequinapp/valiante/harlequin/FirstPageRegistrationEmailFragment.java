package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstPageRegistrationEmailFragment extends Fragment {


    private RelativeLayout imagePicker,nextButton;
    private CircularImageView circularImageView;
    private ImageView camera;
    private EditText name,surname;
    protected TextView birthday;
    private static int GALLERY_REQUEST_CODE = 1;
    private String editId;

    public FirstPageRegistrationEmailFragment() {
        // Required empty public constructor
    }

    public static FirstPageRegistrationEmailFragment newInstance(){
        FirstPageRegistrationEmailFragment newFrag = new FirstPageRegistrationEmailFragment();
        return newFrag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_first_page_registration_email,container,false);


        imagePicker = (RelativeLayout)rootView.findViewById(R.id.pickerLayout);
        nextButton = (RelativeLayout)rootView.findViewById(R.id.nextButtonLayout);
        circularImageView = (CircularImageView)rootView.findViewById(R.id.imagePicker);
        camera = (ImageView) rootView.findViewById(R.id.cameraPicker);
        name = (EditText)rootView.findViewById(R.id.emailName);
        surname = (EditText)rootView.findViewById(R.id.emailSurname);
        birthday = (TextView) rootView.findViewById(R.id.emailBirthday);

        //stringa inizializzata a seconda se si è in edit mode o in registrazione normale
        editId = ((RegistrationEmail)getActivity()).editId;


        //circularImageView.setVisibility(View.GONE);


        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog
                        = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                        ((RegistrationEmail)getActivity()),
                        2017,
                        0,
                        1
                );
                datePickerDialog.setAccentColor(Color.parseColor("#673AB7"));
                datePickerDialog.setCancelColor(Color.parseColor("#18FFFF"));
                datePickerDialog.vibrate(false);
                datePickerDialog.showYearPickerFirst(true);
                datePickerDialog.show(getChildFragmentManager(),"DatepickerDialog");
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

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RegistrationEmail)getActivity()).registrationViewPager.setCurrentItem(1,true);
               /* if(editId == null){
                    if(canGoNext()){
                        saveUserDataInRegistrationPreferences();
                        ((RegistrationEmail)getActivity()).registrationViewPager.setCurrentItem(1,true);

                    }
                }else{
                    if(canEditNext()){
                        saveUserDataInEditPreferences();
                        ((RegistrationEmail)getActivity()).registrationViewPager.setCurrentItem(1,true);

                    }
                }*/
            }
        });




        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //handle galleria normale
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setBorderLineColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }

        //handle cropper
        //Handle del risultato di CropImage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                circularImageView.setVisibility(View.VISIBLE);
                circularImageView.setImageURI(result.getUri());
                circularImageView.setBorderColor(R.color.colorAccent);

                //a seconda se si è in edit mode o in registrazione normale salva l'uri nelle rispettive SP
                if(editId == null) {
                    //selezionata l'immagine salva l'uri come string nelle sharedPreferences della registrazione normale
                    SharedPreferences prefs = getActivity().getSharedPreferences("EMAIL_REG", Context.MODE_PRIVATE);
                    prefs.edit().putString("IMAGE_PATH", result.getUri().toString()).apply();
                }else{
                    SharedPreferences prefs = getActivity().getSharedPreferences("EDIT_REG", Context.MODE_PRIVATE);
                    prefs.edit().putString("IMAGE_PATH", result.getUri().toString()).apply();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;
        SharedPreferences prefs = getActivity().getSharedPreferences("EMAIL_REG", Context.MODE_PRIVATE);
        String userImage = prefs.getString("IMAGE_PATH","NA");
        String userName = name.getText().toString().trim();
        String userSurname = surname.getText().toString().trim();
        String userBirthday = birthday.getText().toString().trim();

        if(userName.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il nome per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(userSurname.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il cognome per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(userBirthday.isEmpty()){
            Toasty.error(getActivity(),"Inserisci la tua data di nascita per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(userImage.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Scegli l'immagine del profilo", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        return canGoNext;
    }

    private Boolean canEditNext(){
        Boolean canGoNext = true;
        SharedPreferences prefs = getActivity().getSharedPreferences("EDIT_REG", Context.MODE_PRIVATE);
        String userImage = prefs.getString("IMAGE_PATH","NA");
        String userName = name.getText().toString().trim();
        String userSurname = surname.getText().toString().trim();
        String userBirthday = birthday.getText().toString().trim();

        if(userName.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il nome per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(userSurname.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il cognome per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(userBirthday.isEmpty()){
            Toasty.error(getActivity(),"Inserisci la tua data di nascita per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(!userImage.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Scegli l'immagine del profilo", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        return canGoNext;
    }

    private void saveUserDataInRegistrationPreferences(){
        SharedPreferences prefs = getActivity().getSharedPreferences("EMAIL_REG", Context.MODE_PRIVATE);
        SharedPreferences.Editor regEditor = prefs.edit();
        String userName = name.getText().toString().trim();
        String userSurname = surname.getText().toString().trim();
        String userBirthday = birthday.getText().toString().trim();
        regEditor.putString("USER_NAME",userName);
        regEditor.putString("USER_SURNAME",userSurname);
        regEditor.putString("USER_BIRTHDAY",userBirthday);
        regEditor.apply();

        UbiquoUtils.printPreferences(prefs);
    }

    private void saveUserDataInEditPreferences(){
        SharedPreferences prefs = getActivity().getSharedPreferences("EDIT_REG", Context.MODE_PRIVATE);
        SharedPreferences.Editor editEditor = prefs.edit();
        String userName = name.getText().toString().trim();
        String userSurname = surname.getText().toString().trim();
        String userBirthday = birthday.getText().toString().trim();
        editEditor.putString("USER_NAME",userName);
        editEditor.putString("USER_SURNAME",userSurname);
        editEditor.putString("USER_BIRTHDAY",userBirthday);
        editEditor.apply();

        UbiquoUtils.printPreferences(prefs);

    }

}
