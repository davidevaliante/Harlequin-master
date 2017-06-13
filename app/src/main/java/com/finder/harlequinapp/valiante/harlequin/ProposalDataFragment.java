package com.finder.harlequinapp.valiante.harlequin;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposalDataFragment extends Fragment {


    protected EditText title,description;
    protected SupportPlaceAutocompleteFragment autocCompleteCity;
    protected RadioRealButtonGroup anonGroup, argumentsGroup;
    protected RelativeLayout nextButton;

    public ProposalDataFragment() {
        // Required empty public constructor
    }

    public static ProposalDataFragment newInstance(){
        ProposalDataFragment newFrag = new ProposalDataFragment();
        return newFrag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_proposal_data,container,false);

        title = (EditText)rootView.findViewById(R.id.mTitle);
        description = (EditText)rootView.findViewById(R.id.mDescription);
        autocCompleteCity = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.place_autocomplete_city);
        anonGroup = (RadioRealButtonGroup)rootView.findViewById(R.id.anonGroup);
        argumentsGroup = (RadioRealButtonGroup)rootView.findViewById(R.id.argumentsGroup);
        nextButton = (RelativeLayout)rootView.findViewById(R.id.mNextButton);


        autocCompleteCity.setHint("Imposta la città");

        loadUserData();

        return rootView;
    }

    private void loadUserData(){
        SharedPreferences userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        String userCity = userData.getString("USER_CITY","NA");

        if(!userCity.equalsIgnoreCase("NA")){
            autocCompleteCity.setHint(userCity);
        }
    }

}
