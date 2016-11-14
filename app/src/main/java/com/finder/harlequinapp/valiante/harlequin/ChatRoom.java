package com.finder.harlequinapp.valiante.harlequin;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class ChatRoom extends AppCompatActivity {

    private String chatId;
    private String chatName;
    private TextView chatTitle;
    private EditText messageBox;
    private Button   sendButton;
    private DatabaseReference chatReference, userReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String userName;
    private String userSurname;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatId = getIntent().getExtras().getString("CHAT_NAME");
        chatName = getIntent().getExtras().getString("EVENT_ID_FOR_CHAT");

        chatTitle = (TextView)findViewById(R.id.chatTitle);
        sendButton = (Button)findViewById(R.id.sendButton);
        messageBox = (EditText)findViewById(R.id.messageBox);
        chatTitle.setText(chatId);

        chatReference = FirebaseDatabase.getInstance().getReference().child("Chat");


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            uid = user.getUid();

        }
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userMessage = messageBox.getText().toString();
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userName = dataSnapshot.getValue(User.class).getUserName();
                        userSurname = dataSnapshot.getValue(User.class).getUserSurname();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                ChatMessage message = new ChatMessage(userMessage,userName+" "+userSurname);
                chatReference.child(chatName).push().setValue(message);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = dataSnapshot.getValue(User.class).getUserName();
                userSurname = dataSnapshot.getValue(User.class).getUserSurname();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
