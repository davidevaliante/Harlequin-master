package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;


public class UserPage extends AppCompatActivity {

    private Button settings;
    private Button addEventButton;
    private Button logOutButton;
    private DatabaseReference myDatabase;
    private FirebaseUser currentUser;
    private FirebaseRecyclerAdapter<Event,EventViewHolder> firebaseRecyclerAdapter;

    private RecyclerView mEventList;

    //TODO serve un ordinamento temporale per i post. per il momento avviene in maniera alfabetica


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        myDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        setContentView(R.layout.activity_user_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //preleva nome dall'Auth personalizzando l'actionBar
        myDatabase.child("Users").child(currentUser.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User myuser = dataSnapshot.getValue(User.class);
                        String myusername = myuser.getUserName();
                        getSupportActionBar().setTitle("Ciao " + myusername);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserPage.this, "Fallimento", Toast.LENGTH_LONG).show();

                    }
                }
         );
        //[END] nome personalizzato

        logOutButton = (Button) findViewById(R.id.logOutButton);
        settings = (Button) findViewById(R.id.action_settings);

        //inizializza il recyclerView per visualizzare dal database
        mEventList = (RecyclerView) findViewById(R.id.event_list);
        mEventList.setHasFixedSize(true);
        //inizializza il layout manager per il recyclerView
        mEventList.setLayoutManager(new LinearLayoutManager(this));
        /*Il recyclerView ha bisogno di un layoutManager(che va solo inizializzato ed un view holder*/


        //TODO implementare la vista blog
    }//[END] FINE DI ONCREATE


    //Crea il ViewHolder per il recyclerView
    public static class EventViewHolder extends RecyclerView.ViewHolder{

        View mView;
        //costruttore del View Holder personalizzato
        public EventViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setEventName (String eventName){
            TextView event_name = (TextView)mView.findViewById(R.id.CardViewTitle);
            event_name.setText(eventName);
        }
        public void setDescription (String description){
            TextView event_desc = (TextView)mView.findViewById(R.id.CardViewDescription);
            event_desc.setText(description);
        }
        //importantissimo rivedere bene
        public void setEventImage (Context ctx, String eventImagePath){
            ImageView event_image = (ImageView)mView.findViewById(R.id.CardViewImage);
            Picasso.with(ctx).load(eventImagePath).into(event_image);
        }
    }


    protected void onStart() {
        super.onStart();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(
                Event.class,
                R.layout.single__event,
                EventViewHolder.class,
                myDatabase.child("Events")
        ) {
            @Override
            protected void populateViewHolder(EventViewHolder viewHolder, Event model, int position) {
                viewHolder.setEventName(model.getEventName());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setEventImage(getApplicationContext(),model.getEventImagePath());

            }
        };

        mEventList.setAdapter(firebaseRecyclerAdapter);

    }


    public void createEvent(View view) {
        Intent switchToCreateEvent = new Intent(UserPage.this, CreateEvent.class);
        startActivity(switchToCreateEvent);
    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent startingPage = new Intent(UserPage.this, MainActivity.class);
        startActivity(startingPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(UserPage.this, "Implementare pagina impostazioni", Toast.LENGTH_LONG).show();
                return true;
            case R.id.logOutButton:
                logOut();
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }
}