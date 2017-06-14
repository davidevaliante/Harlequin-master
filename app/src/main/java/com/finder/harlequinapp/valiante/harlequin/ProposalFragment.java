package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposalFragment extends Fragment {


    protected RecyclerView pRecyclerView;
    protected FloatingActionButton addProposal;
    protected FirebaseRecyclerAdapter proposalAdapter;
    private DatabaseReference proposalRef;
    protected String current_city;
    protected LinearLayoutManager proposalLayoutManager;
    private final String PROPOSAL_RECYCLER_LAYOUT = "RECYCLERVIEW_PROPOSAL_STATE";
    private Parcelable rcPropState;



    public ProposalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_proposal, container, false);

        pRecyclerView = (RecyclerView)rootView.findViewById(R.id.proposalRecycler);
        addProposal = (FloatingActionButton)rootView.findViewById(R.id.addProposalFab);

        SharedPreferences userPrefs = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);

        current_city = userPrefs.getString("USER_CITY","Isernia");
        proposalRef = FirebaseDatabase.getInstance().getReference().child("Proposals").child(current_city);
        proposalRef.keepSynced(true);
        pRecyclerView.setHasFixedSize(true);
        proposalLayoutManager = new LinearLayoutManager(getActivity());

        proposalLayoutManager.setReverseLayout(true);
        proposalLayoutManager.setStackFromEnd(true);
        if(rcPropState !=null) {
            proposalLayoutManager.onRestoreInstanceState(rcPropState);
        }
        pRecyclerView.setLayoutManager(proposalLayoutManager);
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

            proposalAdapter = new FirebaseRecyclerAdapter<Proposal,ProposalViewholder>(

                    Proposal.class,
                    R.layout.proposal_card_layout,
                    ProposalViewholder.class,
                    proposalRef.orderByChild("creationTime")) {

                @Override
                protected void populateViewHolder(ProposalViewholder viewHolder, Proposal model, int position) {
                    Long currentTime = System.currentTimeMillis();
                    viewHolder.setTheme(model.getArgument(),getActivity());
                    viewHolder.setDescription(model.getDescription());
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setPeopleInterested(model.getLikes());
                    viewHolder.setPlacesNotified(model.getPlaces());
                    viewHolder.setElapsedTime(currentTime,model.getCreationTime());

                }


            };

        }
        pRecyclerView.setAdapter(proposalAdapter);
        if (rcPropState != null) {
            pRecyclerView.getLayoutManager().onRestoreInstanceState(rcPropState);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PROPOSAL_RECYCLER_LAYOUT, proposalLayoutManager.onSaveInstanceState());
        rcPropState = outState.getParcelable(PROPOSAL_RECYCLER_LAYOUT);
    }

    @Override
    public void onResume() {
        super.onResume();
        pRecyclerView.setAdapter(proposalAdapter);
        if(rcPropState !=null) {
            proposalLayoutManager.onRestoreInstanceState(rcPropState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        pRecyclerView.setAdapter(null);
        proposalAdapter.cleanup();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pRecyclerView.setAdapter(null);
        proposalAdapter.cleanup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pRecyclerView.setAdapter(null);
        proposalAdapter.cleanup();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(rcPropState !=null) {
            proposalLayoutManager.onRestoreInstanceState(rcPropState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
