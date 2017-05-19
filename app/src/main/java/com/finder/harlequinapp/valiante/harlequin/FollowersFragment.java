package com.finder.harlequinapp.valiante.harlequin;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
public class FollowersFragment extends Fragment {

    private RecyclerView recyclerFollowers;
    private FirebaseRecyclerAdapter followersAdapter;
    private DatabaseReference followersReference,usersReference;

    public FollowersFragment() {
        // Required empty public constructor
    }

    //restituisce un istanza di questo fragment con il titolo fornito
    public static FollowersFragment getInstance(String title){
        FollowersFragment fra = new FollowersFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        fra.setArguments(bundle);
        return fra;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        followersReference = FirebaseDatabase.getInstance().getReference().child("Followers").child(((UserProfile)getActivity()).userId);
        followersReference.keepSynced(true);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);
        //addDummyFollowers(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View followers = inflater.inflate(R.layout.fragment_followers, container, false);
        recyclerFollowers = (RecyclerView)followers.findViewById(R.id.followersRecyclerView);
        recyclerFollowers.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerFollowers.setHasFixedSize(true);

        return followers;
    }

    @Override
    public void onStart() {
        super.onStart();


        followersAdapter = new FirebaseRecyclerAdapter<Boolean,JoinersViewHolder>(
                Boolean.class,
                R.layout.joiners_card_thumb,
                JoinersViewHolder.class,
                followersReference
        ) {
            @Override
            protected void populateViewHolder(final JoinersViewHolder viewHolder, Boolean model, int position) {
                final String followerId = getRef(position).getKey();


                    ValueEventListener userData = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean isMale, isSingle;
                            User myUser = dataSnapshot.getValue(User.class);
                            viewHolder.setNome(myUser.getUserName(), myUser.getUserSurname());
                            viewHolder.setCity(myUser.getUserCity());
                            String userSex = myUser.getUserGender();
                            String userRelationship = myUser.getUserRelationship();
                            viewHolder.setAvatar(myUser.getProfileImage(), getActivity());
                            viewHolder.setAge(myUser.getUserAge());

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

        recyclerFollowers.setAdapter(followersAdapter);
    }

    public void addDummyFollowers(final String targetUser){
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot id : dataSnapshot.getChildren()){
                    FirebaseDatabase.getInstance().getReference().child("Followers").child(targetUser).child(id.getKey()).setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        followersAdapter.cleanup();
        recyclerFollowers.setAdapter(null);
    }
}
