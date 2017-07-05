package com.finder.harlequinapp.valiante.harlequin;


import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposalFragment extends Fragment {


    protected RecyclerView pRecyclerView;
    protected FloatingActionButton addProposal;
    protected FirebaseRecyclerAdapter proposalAdapter;
    private DatabaseReference proposalRef,proposalUserLikeRef,userProposalLikeRef;
    protected String current_city;
    protected LinearLayoutManager proposalLayoutManager;
    private final String PROPOSAL_RECYCLER_LAYOUT = "RECYCLERVIEW_PROPOSAL_STATE";
    private Parcelable rcPropState;
    private Boolean processClick = true;



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

        current_city = userPrefs.getString("USER_CITY","NA");
        proposalRef = FirebaseDatabase.getInstance().getReference().child("Proposals").child(current_city);
        proposalRef.keepSynced(true);
        proposalUserLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Proposals");
        proposalUserLikeRef.keepSynced(true);
        userProposalLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("ProposalsUserLike");
        userProposalLikeRef.keepSynced(true);
        pRecyclerView.setHasFixedSize(true);
        proposalLayoutManager = new LinearLayoutManager(getActivity());

        proposalLayoutManager.setReverseLayout(true);
        proposalLayoutManager.setStackFromEnd(true);

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
                protected void populateViewHolder(final ProposalViewholder viewHolder, final Proposal model, final int position) {
                    Long currentTime = System.currentTimeMillis();
                    final String post_key = getRef(position).getKey();
                    final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    viewHolder.setTheme(model.getArgument(),getActivity());
                    viewHolder.setDescription(model.getDescription());
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setPeopleInterested(model.getLikes());
                    viewHolder.setPlacesNotified(model.getPlaces());
                    viewHolder.setElapsedTime(currentTime,model.getCreationTime());
                    viewHolder.setCreatorName(model.getAnonymous(),model.getCreator());
                    viewHolder.interestButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(processClick){
                                processClick=false;
                                //metodo che legge se il like è presente oppure no e chiama un metodo agiuntivo
                                //di conseguenza
                                likeCheckerForLikeProcess(user_id,post_key,viewHolder.interestButton,model.getArgument());
                            }

                        }
                    });


                    //listener per capire se la proposta ha già il like oppure no
                    ValueEventListener likeProposalListener = new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(user_id)) {
                                viewHolder.buttonInterest.setText("Sei già interessato a questa proposta");
                                viewHolder.likingTransition(model.argument,getActivity());
                                proposalRef.removeEventListener(this);
                            }else{
                                viewHolder.buttonInterest.setText("Mi interessa !");
                                viewHolder.dislikeTransition(model.argument,getActivity());
                                proposalRef.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    proposalUserLikeRef.child(post_key).addListenerForSingleValueEvent(likeProposalListener);


                }


            };

        }
        pRecyclerView.setAdapter(proposalAdapter);
        if (rcPropState != null) {
            pRecyclerView.getLayoutManager().onRestoreInstanceState(rcPropState);
        }

    }



    @Override
    public void onStop() {
        super.onStop();
        pRecyclerView.setAdapter(null);
        proposalAdapter.cleanup();
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

    //aggiunge il like e rende il pulsante nuovamente cliccabile
    private void addLikeToProposal (String userId, final String proposalId){
        String token = FirebaseInstanceId.getInstance().getToken();
        proposalUserLikeRef.child(proposalId).child(userId).setValue(token);
        userProposalLikeRef.child(userId).child(proposalId).setValue(true);
        //transaction
        proposalRef.child(proposalId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Proposal proposal = mutableData.getValue(Proposal.class);
                if(proposal == null){
                    return Transaction.success(mutableData);
                }
                proposal.likes++;
                mutableData.setValue(proposal);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                processClick=true;
            }
        });

    }
    //rimuove il like e rende il pulsante nuovamente cliccabile
    private void removeLikeToProposal (String userId, final String proposalId) {
        proposalUserLikeRef.child(proposalId).child(userId).removeValue();
        userProposalLikeRef.child(userId).child(proposalId).removeValue();
        //transaction
        proposalRef.child(proposalId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Proposal proposal = mutableData.getValue(Proposal.class);
                if(proposal == null){
                    return Transaction.success(mutableData);
                }
                proposal.likes--;
                mutableData.setValue(proposal);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                processClick=true;
            }
        });
    }

    private void likeCheckerForLikeProcess (final String userId, final String proposalId, final View target, final String argument){
        //listener per capire se la proposta ha già il like oppure no
        ValueEventListener likeProposalListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    //like già presente
                    removeLikeToProposal(userId,proposalId);
                    proposalRef.removeEventListener(this);
                }else{
                    //like non presente
                    addLikeToProposal(userId,proposalId);
                    proposalRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        proposalUserLikeRef.child(proposalId).addListenerForSingleValueEvent(likeProposalListener);
    }

    //restituisce int color in base all'argomento della proposta
    private Integer colorArgumentSwitcher(String argument){
        switch (argument){
            case "cocktail":
                return ContextCompat.getColor(getActivity(), R.color.cocktail_green);

            case "dance":
                return ContextCompat.getColor(getActivity(), R.color.dance_red);

            case "music":
                return ContextCompat.getColor(getActivity(), R.color.music_blue);

            case "party":
                return ContextCompat.getColor(getActivity(), R.color.party_orange);

            case "themed":
                return ContextCompat.getColor(getActivity(), R.color.themed_purple);

            default:
                return ContextCompat.getColor(getActivity(), R.color.colorPrimary);

        }

    }

}
