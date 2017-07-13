package com.finder.harlequinapp.valiante.harlequin;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class PrivacyFragment extends DialogFragment {

    private Button close;

    public PrivacyFragment() {
        // Required empty public constructor
    }

    public static PrivacyFragment newInstance(){
        PrivacyFragment privacyFragment = new PrivacyFragment();
        return privacyFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_privacy, container, false);

        close = (Button)rootView.findViewById(R.id.privacy_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }

}
