package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 */
public class DialogCitySelector extends DialogFragment {

    private RecyclerView citySelector;
    private FirebaseRecyclerAdapter cityAdapter;
    private DatabaseReference cityReference;
    private static String current_city;
    private SharedPreferences userData;


    public DialogCitySelector() {
        // Required empty public constructor
    }

    public static DialogCitySelector newInstance(){
        DialogCitySelector newCitySelector = new DialogCitySelector();
        return newCitySelector;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        current_city  = userData.getString("USER_CITY","NA");
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_dialog_city_selector,container,false);

        citySelector = (RecyclerView)rootView.findViewById(R.id.cityPickerRecycler);
        citySelector.setLayoutManager(new LinearLayoutManager(getActivity()));
        citySelector.setHasFixedSize(true);

        cityReference = FirebaseDatabase.getInstance().getReference().child("CityMapByName");
        cityReference.keepSynced(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        cityAdapter = new FirebaseRecyclerAdapter<Object,CityPickerViewHolder>(Object.class,
                                                  R.layout.city_selector_card,
                                                  CityPickerViewHolder.class,
                                                  cityReference

                ) {
            @Override
            protected void populateViewHolder(CityPickerViewHolder viewHolder, final Object model, int position) {


                final String cityName = getRef(position).getKey();

                if(!current_city.equalsIgnoreCase("NA") && current_city.equalsIgnoreCase(cityName)){
                    viewHolder.check.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.matte_blue_check));
                }

                viewHolder.city.setText(cityName);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String currentString =((CitySelector)getActivity()).swappedCity;
                        userData.edit().putString("USER_CITY",cityName).apply();
                        Intent toUserPage = new Intent(getActivity(),MainUserPage.class);
                        startActivity(toUserPage);
                        dismiss();
                        getActivity().finish();
                    }
                });
            }


        };
        citySelector.setAdapter(cityAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        citySelector.setAdapter(null);
        cityAdapter.cleanup();
    }
}
