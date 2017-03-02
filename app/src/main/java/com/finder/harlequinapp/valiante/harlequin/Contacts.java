package com.finder.harlequinapp.valiante.harlequin;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;

public class Contacts extends Fragment {

    private ArrayList<String> names;
    private ArrayList<String> numbers;
    private RecyclerView contactsRecycler;
    private LinearLayoutManager mLinearLayoutManager;
    private ContactsAdapter mAdapter;



    public Contacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactsRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_contacts);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        contactsRecycler.setLayoutManager(mLinearLayoutManager);
        contactsRecycler.setHasFixedSize(true);


        this.names = ((EventPage)getActivity()).names;
        this.numbers = ((EventPage)getActivity()).numbers;
        mAdapter = new ContactsAdapter(names,numbers,getContext());
        //Toast.makeText(getContext(), "nome : "+names.get(0)+" numero : "+numbers.get(0), Toast.LENGTH_SHORT).show();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        contactsRecycler.setAdapter(mAdapter);


    }



}
