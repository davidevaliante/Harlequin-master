package com.finder.harlequinapp.valiante.harlequin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ChatList extends AppCompatActivity {

    private MaterialRippleLayout clHomeButton,clProfileButton,clMessageButton;
    private RecyclerView chatList;
    private DatabaseReference chatReference,usersReference;
    private FirebaseUser currentUser;
    private FirebaseRecyclerAdapter pChatAdapter;
    private Toolbar clToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        clHomeButton = (MaterialRippleLayout)findViewById(R.id.rippleHomeChatlist);
        clProfileButton = (MaterialRippleLayout)findViewById(R.id.rippleProfileChatList);
        clMessageButton = (MaterialRippleLayout)findViewById(R.id.rippleMessageChatList);
        chatList = (RecyclerView)findViewById(R.id.chat_list);
        clToolbar = (Toolbar)findViewById(R.id.cl_toolbar);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatList.setHasFixedSize(true);
        chatList.setLayoutManager(linearLayoutManager);
        setSupportActionBar(clToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);;

        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        //onclickListener per la toolbar in basso
        clHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //nul
            }
        });
        clProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //null
            }
        });
        clMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //null
            }
        });
    }

    public static class ThumbnailViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView chatName;
        CircularImageView userAvatar;

        public ThumbnailViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            chatName = (TextView)mView.findViewById(R.id.ctNomeUtente);
            userAvatar = (CircularImageView)mView.findViewById(R.id.smallChatAvatar);
        }

        //implementare i metodi per popolare la recyclerview
        public void setChatName(String userName){
            chatName.setText(""+userName);
        }
        public void setAvatar (final String avatarUrl){
            Picasso.with(mView.getContext())
                   .load(avatarUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                   .into(userAvatar, new Callback() {
                @Override
                public void onSuccess() {
                    //va bene così non deve fare nulla
                }
                @Override
                public void onError() {
                    Picasso.with(mView.getContext()).load(avatarUrl).into(userAvatar);
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        chatReference = FirebaseDatabase.getInstance().getReference().child(currentUser.getUid());
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReference.keepSynced(true);


    }

}
