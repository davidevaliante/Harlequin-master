package com.finder.harlequinapp.valiante.harlequin;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FollowingFragment extends Fragment {

    DatabaseReference followingReference, userReference;
    RecyclerView recyclerFollowing;
    FirebaseRecyclerAdapter followingAdapter;
    String userId;
    Boolean ownProfile;

    public FollowingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ownProfile = ((UserProfile)getActivity()).ownProfile;
        if(ownProfile){
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else{
            userId = ((UserProfile)getActivity()).userId;
        }
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        followingReference = FirebaseDatabase.getInstance().getReference().child("Following").child(userId);
        followingReference.keepSynced(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View followingView = inflater.inflate(R.layout.fragment_following, container, false);
        recyclerFollowing = (RecyclerView)followingView.findViewById(R.id.followingRecyclerView);
        recyclerFollowing.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerFollowing.setHasFixedSize(true);


        return followingView;
    }

    @Override
    public void onStart() {
        super.onStart();

        followingAdapter = new FirebaseRecyclerAdapter<Boolean,JoinersViewHolder>(
                Boolean.class,
                R.layout.joiners_card_thumb,
                JoinersViewHolder.class,
                followingReference
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
                                ValueEventListener userToken = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String token = (String)dataSnapshot.getValue();
                                        ((UserProfile)getActivity()).showProfileDialog(joinerId,token);
                                        FirebaseDatabase.getInstance().getReference().child("Token").child(joinerId).child("user_token").removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                };
                                FirebaseDatabase.getInstance().getReference().child("Token").child(joinerId).child("user_token").addValueEventListener(userToken);



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
                        userReference.child(joinerId).removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                userReference.child(joinerId).addValueEventListener(userData);
            }
        };

        recyclerFollowing.setAdapter(followingAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerFollowing.setAdapter(null);
        followingAdapter.cleanup();
    }
}
