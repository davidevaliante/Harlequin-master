package com.finder.harlequinapp.valiante.harlequin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ChatFragment extends Fragment {

    private RecyclerView chatRecycler;
    private FirebaseUser currentUser;
    private DatabaseReference chatReference,usersReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        chatRecycler = (RecyclerView)inflater.inflate(R.layout.chat_fragment_layout, container, false);
        chatRecycler.setHasFixedSize(true);
        chatRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        return chatRecycler;
    }


    @Override
    public void onStart() {
        super.onStart();

            chatReference = FirebaseDatabase.getInstance().getReference().child(currentUser.getUid());
            usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
            chatReference.keepSynced(true);


    }
}
