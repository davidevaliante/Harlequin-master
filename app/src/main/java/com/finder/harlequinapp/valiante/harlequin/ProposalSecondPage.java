package com.finder.harlequinapp.valiante.harlequin;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposalSecondPage extends Fragment {


    public ProposalSecondPage() {
        // Required empty public constructor
    }

    public static ProposalSecondPage newInstance(){
        ProposalSecondPage newFrag = new ProposalSecondPage();
        return newFrag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_proposal_second_page,container,false);

        return rootView;
    }

}
