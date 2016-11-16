package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Calendar;


public class ChatRoom extends AppCompatActivity {

    private String chatId;
    private String chatName;

    private EditText messageBox;
    private Button   sendButton;
    private DatabaseReference chatReference, userReference;

    private String userName;
    private String userSurname;
    private String userAvatar;
    private String uid, userMessage;
    private RecyclerView mMessageList;
    private FirebaseRecyclerAdapter<ChatMessage,MessageViewHolder> mFirebaseRecyclerAdapter;
    private TextView chatRoomName;
    private CircularImageView toolBarAvatar;
    private Integer mHour, mMinutes;
    private Calendar mCurrentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);


        chatName = getIntent().getExtras().getString("CHAT_NAME");
        chatId = getIntent().getExtras().getString("EVENT_ID_FOR_CHAT");



        sendButton = (Button)findViewById(R.id.sendButton);
        messageBox = (EditText)findViewById(R.id.messageBox);
        mMessageList = (RecyclerView)findViewById(R.id.message_list);
        chatRoomName = (TextView)findViewById(R.id.hello);
        toolBarAvatar = (CircularImageView)findViewById(R.id.smallToolBarAvatar);


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);


        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(linearLayoutManager);
        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        chatRoomName.setText(chatName);







        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            uid = user.getUid();

        }
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userReference.keepSynced(true);

        //pulsante per inviare messaggio
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               userMessage = messageBox.getText().toString();
                //controlla che il messaggio non sia vuoto
                if(!userMessage.isEmpty()) {
                    mCurrentTime = Calendar.getInstance();
                    mHour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                    mMinutes = mCurrentTime.get(Calendar.MINUTE);
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            userName = dataSnapshot.getValue(User.class).getUserName();
                            userAvatar = dataSnapshot.getValue(User.class).getProfileImage();
                            userSurname = dataSnapshot.getValue(User.class).getUserSurname();
                            ChatMessage message = new ChatMessage(userMessage, userName + " " + userSurname,userAvatar,mHour,mMinutes);
                            chatReference.push().setValue(message);
                            messageBox.setText("");
                            mMessageList.smoothScrollToPosition(mFirebaseRecyclerAdapter.getItemCount());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }else{
                    Toast.makeText(ChatRoom.this,"Inserisci un messaggio",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }//[END] OnCreate


    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView cardUserName,cardUserMessage,cardMessageTime;
        CircularImageView smallMessageAvatar;
        public MessageViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            //elementi UI per ogni messaggio
            cardUserName = (TextView)mView.findViewById(R.id.card_message_name);
            cardUserMessage = (TextView)mView.findViewById(R.id.card_message_text);
            smallMessageAvatar = (CircularImageView)mView.findViewById(R.id.smallMessageAvatar);
            cardMessageTime = (TextView)mView.findViewById(R.id.cardMessageTime);
        }
        //metodi per visualizzare il contenuto dei messaggi
        public void setCardUserName(String userName){cardUserName.setText(userName);}
        public void setCardUserMessage(String userMessage){cardUserMessage.setText(userMessage);}
        public void setCardMessageTime (Integer hour, Integer minute){cardMessageTime.setText(hour+":"+minute);}

        //per caricare le immagini con Picasso senza dare bug serve spesso un context oltre alla stringa dell'url
        public void setMessageAvatar (final Context ctx, final String avatarUrl){
            Picasso.with(ctx)
                    .load(avatarUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(smallMessageAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(avatarUrl).into(smallMessageAvatar);
                        }
                    });
        }
    }
    protected void onStart() {
        super.onStart();

        chatReference = FirebaseDatabase.getInstance().getReference().child("Chat").child(chatId);
        chatReference.keepSynced(true);

        //per recuperare il nome di chi scrive dal database
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = dataSnapshot.getValue(User.class).getUserName();
                userSurname = dataSnapshot.getValue(User.class).getUserSurname();
                String userAvatar = dataSnapshot.getValue(User.class).getProfileImage();
                Picasso.with(getApplicationContext())
                        .load(userAvatar)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(toolBarAvatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });//[END] recupero nome del mittente

        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatMessage,MessageViewHolder>(

                ChatMessage.class,
                R.layout.single_message,
                MessageViewHolder.class,
                chatReference

        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, ChatMessage model, int position) {


                viewHolder.setCardUserName(model.getUserName());
                viewHolder.setCardUserMessage(model.getMessage());
                viewHolder.setMessageAvatar(getApplicationContext() , model.getMessageAvatar());
                viewHolder.setCardMessageTime(model.getHour(),model.getMinute());

            }
        };
        mMessageList.setAdapter(mFirebaseRecyclerAdapter);



    }
}
