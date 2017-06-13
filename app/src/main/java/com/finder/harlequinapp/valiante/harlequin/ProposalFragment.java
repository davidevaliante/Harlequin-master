package com.finder.harlequinapp.valiante.harlequin;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposalFragment extends Fragment {


    protected RecyclerView pRecyclerView;
    protected FloatingActionButton addProposal;
    protected FirebaseRecyclerAdapter proposalAdapter;

    public ProposalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_proposal, container, false);

        pRecyclerView = (RecyclerView)rootView.findViewById(R.id.proposalRecycler);
        addProposal = (FloatingActionButton)rootView.findViewById(R.id.addProposalFab);
        pRecyclerView.setHasFixedSize(true);
        pRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        pRecyclerView.getItemAnimator().setChangeDuration(0);

        addProposal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addProposal = new Intent(getActivity(),CreateProposal.class);
                startActivity(addProposal);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(pRecyclerView.getAdapter() == null){


        }

    }
}
