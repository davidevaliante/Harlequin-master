package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


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

        //controlla se l'intent viene da una chat di gruppo o privata

        //Bundle dell'Intent
        chatName = getIntent().getExtras().getString("CHAT_NAME");
        chatId = getIntent().getExtras().getString("EVENT_ID_FOR_CHAT");

        //Elementi UI
        sendButton = (Button)findViewById(R.id.sendButton);
        messageBox = (EditText)findViewById(R.id.messageBox);
        mMessageList = (RecyclerView)findViewById(R.id.message_list);
        chatRoomName = (TextView)findViewById(R.id.hello);
        toolBarAvatar = (CircularImageView)findViewById(R.id.smallToolBarAvatar);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(linearLayoutManager);
        Toolbar toolbar = (Toolbar)findViewById(R.id.fav_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        chatRoomName.setText(chatName);

        //elementi fondamentali da inizializzare di Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            uid = user.getUid();
        }
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userReference.keepSynced(true);

        //TODO fixare un piccolo bug che non fa visualizzare bene l'ora in formato a 24 ore hai già fatto un fix nella pagina per creare evento
        //pulsante per inviare messaggio
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            userMessage = messageBox.getText().toString().trim();
            //controlla che il messaggio non sia vuoto
            if(!userMessage.isEmpty()) {
               mCurrentTime = Calendar.getInstance();
               mHour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
               mMinutes = mCurrentTime.get(Calendar.MINUTE);
                //cerca Nome, cognome ed avatar personale per mandare il messaggio
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    User senderUser = dataSnapshot.getValue(User.class);
                    userName = senderUser.getUserName();
                    userAvatar = senderUser.getProfileImage();
                    userSurname = senderUser.getUserSurname();
                    String correctHour = setCorrectTime(mHour,mMinutes);
                    ChatMessage message = new ChatMessage(userMessage, userName + " " + userSurname,userAvatar,uid,correctHour);
                    chatReference.push().setValue(message);
                    messageBox.setText("");
                    mMessageList.smoothScrollToPosition(mFirebaseRecyclerAdapter.getItemCount());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {/*null*/}
                });
            }else{
              Toast.makeText(ChatRoom.this,"Inserisci un messaggio",Toast.LENGTH_SHORT).show();
            }
            }
        });// END pulsante SEND MESSAGE

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
            smallMessageAvatar = (CircularImageView)mView.findViewById(R.id.smallChatAvatar);
            cardMessageTime = (TextView)mView.findViewById(R.id.cardMessageTime);
        }
        //metodi per visualizzare il contenuto dei messaggi
        public void setCardUserName(String userName){cardUserName.setText(userName);}
        public void setCardUserMessage(String userMessage){cardUserMessage.setText(userMessage);}
        public void setCardMessageTime (String messageTime){cardMessageTime.setText(messageTime);}

        //per caricare le immagini con Picasso senza dare bug serve spesso un context oltre alla stringa dell'url
        public void setMessageAvatar (final Context ctx, final String avatarUrl){
            Picasso.with(ctx)
                    .load(avatarUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(smallMessageAvatar, new Callback() {
                        @Override
                        public void onSuccess() {/*null*/}
                        @Override
                        public void onError() {Picasso.with(ctx).load(avatarUrl).into(smallMessageAvatar);
                        }
                    });
        }
    }

    protected void onStart() {
        super.onStart();

        chatReference = FirebaseDatabase.getInstance().getReference().child("Chat").child(chatId);
        chatReference.keepSynced(true);

        //per recuperare nome e cognome di chi scrive dal database per ogni messaggio della chat
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
        //recyclerAdapter per i messaggi
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
                viewHolder.setCardMessageTime(model.getMessageTime());
               //cambia i colori della chat bubble se riconosce l'ID di chi è loggato
                if(model.getUserId().equals(uid)){
                    viewHolder.mView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    viewHolder.smallMessageAvatar.setBorderColor(getResources().getColor(R.color.colorPrimaryLight));
                }

            }
        };
      //assegna l'adattatore appena definito alla recyclerView
      mMessageList.setAdapter(mFirebaseRecyclerAdapter);
    }//END di OnStart

    //per usare i font personalizzati
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public String setCorrectTime(int hour, int minute){
        String correctTime = hour+":"+minute;
        if (hour < 10 && minute <10){
            correctTime="0"+hour+":"+"0"+minute;
            return correctTime;
        }
        if(hour < 10 && minute >=10){
            correctTime="0"+hour+":"+minute;
            return correctTime;
        }
        if (hour >= 10 && minute < 10){
            correctTime=hour+":"+"0"+minute;
            return correctTime;
        }
        else return correctTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
