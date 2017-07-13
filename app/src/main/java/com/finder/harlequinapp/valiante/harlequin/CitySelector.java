package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CitySelector extends AppCompatActivity {

    private DatabaseReference cityReference;
    private TextView currentCitySentence,swapperText;
    private RelativeLayout swapper;
    private RelativeLayout confirm;
    protected String swappedCity;
    private SharedPreferences userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_selector);

        userData = getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        String current_city  = userData.getString("USER_CITY","NA");

        cityReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic");
        cityReference.keepSynced(true);

        currentCitySentence = (TextView)findViewById(R.id.currentCityDisplayer);
        swapper = (RelativeLayout) findViewById(R.id.citySwapper);
        swapperText = (TextView)findViewById(R.id.citySwapperTextView);
        confirm = (RelativeLayout)findViewById(R.id.confirmCitySwap);

        setCurrentCityIntoTextView(current_city);

        //Annulla
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String current_city = userData.getString("USER_CITY","NA");
                if(current_city.equalsIgnoreCase("NA")){
                    finish();
                }else{
                    Intent toUserPage = new Intent(CitySelector.this,MainUserPage.class);
                    startActivity(toUserPage);
                    finish();
                }
            }
        });

        swapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragManager = getSupportFragmentManager();
                DialogCitySelector newSelector = DialogCitySelector.newInstance();
                newSelector.show(fragManager,"city_picker_dialog");
            }
        });




    }

    protected void setCurrentCityIntoTextView(String current_city){
        if(current_city == null || current_city.equalsIgnoreCase("NA")){
            currentCitySentence.setText("Scegli una città che ti interessa per continuare");
            swapperText.setText("Scegli città");
        }else{
            currentCitySentence.setText("Sei attualmente connesso su "+current_city+", vuoi cambiare la città ?");
            swapperText.setText("Cambia città");
        }

    }
}
