package com.finder.harlequinapp.valiante.harlequin;


import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;


/**
 * A simple {@link Fragment} subclass.
 */
public class ThirdPageRegistrationEmailFragment extends Fragment {

    private EditText mail, pass, confirmPass;
    private RelativeLayout submit;
    private  String editId;
    private DatabaseReference userReference;
    private StorageReference imageRef;
    private static String relationship,gender;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ProgressDialog dialog;

    public ThirdPageRegistrationEmailFragment() {
        // Required empty public constructor
    }

    public static ThirdPageRegistrationEmailFragment newInstance(){
        ThirdPageRegistrationEmailFragment newThirdFrag = new ThirdPageRegistrationEmailFragment();
        return newThirdFrag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_third_page_registration_email, container,false);

        mAuth = FirebaseAuth.getInstance();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        imageRef = FirebaseStorage.getInstance().getReference().child("Profile_pictures");

        editId = ((RegistrationEmail)getActivity()).editId;

        mail = (EditText)rootView.findViewById(R.id.userRegMail);
        pass = (EditText)rootView.findViewById(R.id.userRegPass);
        confirmPass = (EditText)rootView.findViewById(R.id.userRegConfirm);
        submit = (RelativeLayout)rootView.findViewById(R.id.submitReg);

        dialog = UbiquoUtils.defaultProgressBar("Attendere prego...",getActivity());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canGoNext()){
                    if(editId == null){
                        dialog.show();
                        SharedPreferences userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = userData.edit();
                        final String user_mail = mail.getText().toString().trim();
                        final String user_pass = pass.getText().toString().trim();
                        mAuth.createUserWithEmailAndPassword(user_mail,user_pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    editor.putString("USER_MAIL",user_mail);
                                    editor.putString("USER_PASS",user_pass);
                                    editor.apply();
                                   String uid = task.getResult().getUser().getUid();
                                    writeNewUser(uid);
                                }else{
                                    Toasty.error(getActivity(),"Ci sono stati problemi con la registrazione",Toast.LENGTH_SHORT,true).show();
                                }
                            }
                        });

                    }

                }
            }
        });

        return rootView;
    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;

        String user_mail = mail.getText().toString().trim();
        String user_pass = pass.getText().toString().trim();
        String user_confirm = confirmPass.getText().toString().trim();

        if(user_mail.isEmpty()){
            Toasty.error(getActivity(),"Inserisci una mail",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(user_pass.isEmpty() || user_pass.isEmpty() || user_pass.length()<7){
            Toasty.error(getActivity(),"Inserisci una password di almeno 7 caratteri",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(!user_pass.equals(user_confirm)){
            Toasty.error(getActivity(),"Le password non corrispondono",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        return canGoNext;
    }

    private void writeNewUser(final String uid){

        SharedPreferences pref = getActivity().getSharedPreferences("EMAIL_REG", Context.MODE_PRIVATE);
        final String name = pref .getString("USER_NAME","NA");
        final String surname = pref.getString("USER_SURNAME","NA");
        String image = pref.getString("IMAGE_PATH","NA");
        final String age = pref.getString("USER_BIRTHDAY","NA");
        final String user_mail = mail.getText().toString().trim();
        final String city = pref.getString("USER_CITY","NA");
        String pass = confirmPass.getText().toString();

        //GENDER
        Boolean isMale = pref.getBoolean("USER_ISMALE",true);
        if(isMale){
            gender="Uomo";
        }else{
            gender="Donna";
        }
        Boolean isSingle = pref.getBoolean("USER_ISSINGLE",true);

        //RELATIONSHIP
        if(isMale && !isSingle){
            relationship = "Impegnato";
        }

        if(!isMale && !isSingle){
            relationship = "Impegnata";
        }

        if(isMale && isSingle){
            relationship = "Single";
        }

        if(!isMale && isSingle){
            relationship = "Single";
        }

        //uri della cropped profile
        Uri avatarUri = Uri.parse(image);

        File compressedImageFile = Compressor.getDefault(getActivity()).compressToFile(new File(avatarUri.getPath()));
        Uri compressedFileUri = Uri.fromFile(compressedImageFile);

        imageRef.child(avatarUri.getLastPathSegment()+name+surname).putFile(compressedFileUri).addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String imagePath = taskSnapshot.getDownloadUrl().toString();
                    String token = FirebaseInstanceId.getInstance().getToken().toString();


                Long registrationDate = System.currentTimeMillis();
                    User newUser = new User(name,user_mail,age,city,surname,imagePath,relationship,gender,"default@facebook.com","NA",token,registrationDate,0L);
                    userReference.child(uid).setValue(newUser);
                    dialog.dismiss();
                    Intent userPage = new Intent(getActivity(), LauncherActivity.class);
                    startActivity(userPage);

            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(getActivity(),"Ci sono stati problemi durante l'UpLoad dell'immagine di profilo",Toast.LENGTH_SHORT,true).show();

            }
        });


    }
}
