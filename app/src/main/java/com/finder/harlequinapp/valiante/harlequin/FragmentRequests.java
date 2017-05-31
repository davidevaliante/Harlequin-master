package com.finder.harlequinapp.valiante.harlequin;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
public class FragmentRequests extends Fragment {

    private DatabaseReference pendingRequestReference,usersReference ;
    private String user_id;
    private RecyclerView pendingRecycler;
    private FirebaseRecyclerAdapter pendingAdapter;

    public FragmentRequests() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        pendingRequestReference = FirebaseDatabase.getInstance().getReference().child("PendingRequest").child(user_id);
        pendingRequestReference.keepSynced(true);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View requests = inflater.inflate(R.layout.fragment_fragment_requests, container, false);
        pendingRecycler = (RecyclerView)requests.findViewById(R.id.pendingRecyclerView);
        pendingRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        pendingRecycler.setHasFixedSize(true);
        return requests;
    }

    @Override
    public void onStart() {
        super.onStart();

        pendingAdapter = new FirebaseRecyclerAdapter<PendingFollowingRequest,JoinersViewHolder>(
                PendingFollowingRequest.class,
                R.layout.joiners_card_thumb,
                JoinersViewHolder.class,
                pendingRequestReference
        ) {
            @Override
            protected void populateViewHolder(final JoinersViewHolder viewHolder, PendingFollowingRequest model, int position) {
                final String followerId = getRef(position).getKey();
                viewHolder.acceptBtn.setVisibility(View.VISIBLE);
                viewHolder.sex.setVisibility(View.INVISIBLE);


                ValueEventListener userData = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean isMale, isSingle;
                        final User myUser = dataSnapshot.getValue(User.class);
                        viewHolder.setNome(myUser.getUserName(), myUser.getUserSurname());
                        viewHolder.setCity(myUser.getUserCity());
                        String userSex = myUser.getUserGender();
                        String userRelationship = myUser.getUserRelationship();
                        viewHolder.setAvatar(myUser.getProfileImage(), getActivity());
                        viewHolder.setAge(myUser.getUserAge());

                        viewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UbiquoUtils.acceptRequestMethod(followerId,myUser.getUserToken(),myUser.getUserName(),getActivity());
                            }
                        });

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ValueEventListener userToken = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String token = (String) dataSnapshot.getValue();
                                        ((UserProfile) getActivity()).showProfileDialog(followerId, token);
                                        FirebaseDatabase.getInstance().getReference().child("Token").child(followerId).child("user_token").removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                };
                                FirebaseDatabase.getInstance().getReference().child("Token").child(followerId).child("user_token").addValueEventListener(userToken);


                            }
                        });


                        if (userRelationship.equalsIgnoreCase("Single")) {
                            isSingle = true;
                            if (userSex.equalsIgnoreCase("Uomo")) {
                                isMale = true;
                                viewHolder.setStatus(isSingle, isMale);
                                viewHolder.setSex(isMale, getActivity());
                            } else {
                                isMale = false;
                                viewHolder.setStatus(isSingle, isMale);
                                viewHolder.setSex(isMale, getActivity());

                            }
                        } else {
                            isSingle = false;
                            if (userSex.equalsIgnoreCase("Uomo")) {
                                isMale = true;
                                viewHolder.setStatus(isSingle, isMale);
                                viewHolder.setSex(isMale, getActivity());


                            } else {
                                isMale = false;
                                viewHolder.setStatus(isSingle, isMale);
                                viewHolder.setSex(isMale, getActivity());


                            }
                        }
                        usersReference.child(followerId).removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                usersReference.child(followerId).addValueEventListener(userData);
            }

        };

        pendingRecycler.setAdapter(pendingAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        pendingRecycler.setAdapter(null);
        pendingAdapter.cleanup();
    }
}
