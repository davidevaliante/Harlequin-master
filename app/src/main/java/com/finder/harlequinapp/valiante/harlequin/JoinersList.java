package com.finder.harlequinapp.valiante.harlequin;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class JoinersList extends Fragment {

    protected RecyclerView mRecyclerView;
    protected DatabaseReference joinersReference,eventListRef,targetUser;
    protected FirebaseRecyclerAdapter joinersAdapter;
    protected LinearLayoutManager mLinearLayoutManager;

    private String current_city="Isernia";

    public JoinersList() {
        // Required empty public constructor
    }


    //TODO se nel rpocesso di like pusham il nome invece di un valore boolean possiamoanche ordinare alfabeticamente i partecipanti

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_joiners_list, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_joiner);

        joinersReference = FirebaseDatabase.getInstance().getReference().child("Likes")
                                                                        .child("Events")
                                                                        .child(current_city)
                                                                        .child(((EventPage)getActivity())
                                                                        .eventId);
        joinersReference.keepSynced(true);
        targetUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.setHasFixedSize(true);

        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();


        joinersAdapter = new FirebaseRecyclerAdapter<Boolean,JoinersViewHolder>(
                Boolean.class,
                R.layout.joiners_card_thumb,
                JoinersViewHolder.class,
                joinersReference
        ) {
            @Override
            protected void populateViewHolder(final JoinersViewHolder viewHolder, Boolean model, int position) {
                final String joinerId = getRef(position).getKey();
                ValueEventListener userData = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean isMale,isSingle;
                        User myUser = dataSnapshot.getValue(User.class);
                        viewHolder.setNome(myUser.getUserName(),myUser.getUserSurname());
                        viewHolder.setCity(myUser.getUserCity());
                        String userSex = myUser.getUserGender();
                        String userRelationship = myUser.getUserRelationship();
                        viewHolder.setAvatar(myUser.getProfileImage(),getActivity());
                        viewHolder.setAge(myUser.getUserAge());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((EventPage)getActivity()).showProfileDialog(joinerId);
                            }
                        });



                        if(userRelationship.equalsIgnoreCase("Single")){
                            isSingle=true;
                            if(userSex.equalsIgnoreCase("Uomo")){
                                isMale = true;
                                viewHolder.setStatus(isSingle,isMale);
                                viewHolder.setSex(isMale,getActivity());
                               }
                            else{
                                isMale = false;
                                viewHolder.setStatus(isSingle,isMale);
                                viewHolder.setSex(isMale,getActivity());

                            }
                        }
                        else{
                            isSingle=false;
                            if(userSex.equalsIgnoreCase("Uomo")){
                                isMale = true;
                                viewHolder.setStatus(isSingle,isMale);
                                viewHolder.setSex(isMale,getActivity());


                            }
                            else{
                                isMale = false;
                                viewHolder.setStatus(isSingle,isMale);
                                viewHolder.setSex(isMale,getActivity());


                            }
                        }
                        targetUser.child(joinerId).removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                targetUser.child(joinerId).addValueEventListener(userData);
            }
        };

        mRecyclerView.setAdapter(joinersAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRecyclerView.setAdapter(null);
        joinersAdapter.cleanup();
    }


}
