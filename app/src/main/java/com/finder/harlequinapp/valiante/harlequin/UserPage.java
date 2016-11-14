package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import org.w3c.dom.Text;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.R.attr.duration;


public class UserPage extends AppCompatActivity {

    private Button settings;
    private Button addEventButton;
    private Button logOutButton;
    private DatabaseReference myDatabase, mDatabaseLike;
    private FirebaseUser currentUser;
    private FirebaseRecyclerAdapter<Event,EventViewHolder> firebaseRecyclerAdapter;
    private CircularImageView avatar;
    private CircularImageView cardAvatar,cardLike,cardInfo;
    private KenBurnsView kvb;
    private Context context;
    private boolean mProcessLike = false;




    private RecyclerView mEventList;

    //TODO serve un ordinamento temporale per i post. per il momento avviene in maniera alfabetica


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);



        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        myDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();




        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        kvb = (KenBurnsView)findViewById(R.id.CardViewImage);
        final TextView hiUser = (TextView)findViewById(R.id.hello);

        final Typeface steinerLight = Typeface.createFromAsset(getAssets(),"fonts/Steinerlight.ttf");




        //preleva nome dall'Auth personalizzando l'actionBar
        myDatabase.child("Users").child(currentUser.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User myuser = dataSnapshot.getValue(User.class);
                            String myusername = myuser.getUserName();

                            hiUser.setText("Ciao " + myusername);


                            //imposta l'avatar
                            final String avatarUrl = myuser.getProfileImage();
                            Picasso.with(UserPage.this)
                                .load(avatarUrl)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(avatar, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        //va bene così
                                    }
                                    @Override
                                    public void onError() {
                                        //se non è in memoria cerca di caricarla
                                        Picasso.with(UserPage.this)
                                                .load(avatarUrl)
                                                .into(avatar);
                                        //TODO Ha bisogno di prima scaricare l'immagine in un placeholder e poi trasformarla in
                                        //drawable per usarla nel metodo di sotto
                            //toolbar.setOverflowIcon(Picasso.with(UserPage.this).load(avatarUrl));;
                                    }
                                });

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
        avatar = (CircularImageView)findViewById(R.id.smallToolBarAvatar) ;

        //inizializza il recyclerView per visualizzare dal database
        mEventList = (RecyclerView) findViewById(R.id.event_list);
        mEventList.setHasFixedSize(true);
        //inizializza il layout manager per il recyclerView
        mEventList.setLayoutManager(new LinearLayoutManager(this));
        /*Il recyclerView ha bisogno di un layoutManager(che va solo inizializzato ed un view holder*/

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toastText();
            }
        });

    }//[END] FINE DI ONCREATE

    private void toastText() {
        Toast.makeText(UserPage.this,"bravo !",Toast.LENGTH_LONG).show();
    }


    //Crea il ViewHolder per il recyclerView
    public static class EventViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircularImageView cardLike,cardProfile,cardInfo;
        TextView cardLikes;

        //costruttore del View Holder personalizzato
        public EventViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

            cardLike = (CircularImageView)mView.findViewById(R.id.CardLike);
            cardProfile = (CircularImageView)mView.findViewById(R.id.smallAvatar);
            cardInfo    = (CircularImageView)mView.findViewById(R.id.CardInfo);
            cardLikes = (TextView)mView.findViewById(R.id.cardLikeCounter);

        }

        public void setThumbUp (){
            cardLike.setImageResource(R.drawable.thumb24);
        }

        public void setThumbDown (){
            cardLike.setImageResource(R.drawable.thumb_down24);
        }

        public void setLikes (Integer likes){
            cardLikes.setText(""+likes);

        }


        public void setCreatorAvatar (final Context avatarctx , final String creatorAvatarPath){
            Picasso.with(avatarctx)
                    .load(creatorAvatarPath)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(cardProfile, new Callback() {
                        @Override
                        public void onSuccess() {
                            //va bene così non deve fare nulla
                        }
                        @Override
                        public void onError() {
                            Picasso.with(avatarctx).load(creatorAvatarPath).into(cardProfile);
                        }
                    });
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
        public void setEventImage (final Context ctx, final String eventImagePath){
            final ImageView event_image = (ImageView)mView.findViewById(R.id.CardViewImage);

            Picasso.with(ctx)
                    .load(eventImagePath)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(event_image, new Callback() {
                @Override
                public void onSuccess() {
                    //va bene così non deve fare nulla
                }
                @Override
                public void onError() {
                    Picasso.with(ctx).load(eventImagePath).into(event_image);
                }
            });
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
            protected void populateViewHolder(final EventViewHolder viewHolder, final Event model, final int position) {

                final String post_key = getRef(position).getKey();
                viewHolder.setEventName(model.getEventName());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setEventImage(getApplicationContext(),model.getEventImagePath());
                viewHolder.setCreatorAvatar(getApplicationContext(),model.getCreatorAvatarPath());
                viewHolder.setLikes(model.getLikes());

                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(post_key).hasChild(currentUser.getUid())){
                            viewHolder.setThumbDown();

                        }else{
                            viewHolder.setThumbUp();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




                //OnClick per il profilo del creatore
                viewHolder.cardProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(UserPage.this,"profilo creatore evento numero :"+position,
                                Toast.LENGTH_LONG).show();
                    }
                });

                //OnClick per la finestra completa dell'evento
                viewHolder.cardInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(UserPage.this,"pagina dell'evento numero :"+position,
                                Toast.LENGTH_LONG).show();
                    }
                });


                //Onclick per il pulsante like
                viewHolder.cardLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;
                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //se il tasto like è "spento"
                                    if(mProcessLike) {
                                        //se l'utente è presente fra i like del rispettivo evento
                                        if (dataSnapshot.child(post_key).hasChild(currentUser.getUid())) {
                                            mDatabaseLike.child(post_key).child(currentUser.getUid()).removeValue();
                                            myDatabase.child("Events").child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                    current_likes--;
                                                    myDatabase.child("Events").child(post_key).child("likes").setValue(current_likes);

                                                    Toast.makeText(UserPage.this,"Evento rimosso dai preferiti",Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            mProcessLike = false;
                                            //se l'utente non è presente nei like dell'evento
                                        } else {
                                            mDatabaseLike.child(post_key).child(currentUser.getUid()).setValue("RandomValue");
                                            myDatabase.child("Events").child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                    current_likes++;
                                                    myDatabase.child("Events").child(post_key).child("likes").setValue(current_likes);

                                                    mProcessLike = false;
                                                    Toast.makeText(UserPage.this,"Evento aggiunto ai preferiti",Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            mProcessLike = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                    }
                });

                //Onclick per il view generalizzato
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        String name = model.getEventName();

                        Intent goToEventPage = new Intent (UserPage.this,EventPage.class);
                        goToEventPage.putExtra("EVENT_NAME",model.getEventName());
                        goToEventPage.putExtra("EVENT_DESCRIPTION",model.getDescription());
                        goToEventPage.putExtra("EVENT_IMAGE_URL",model.getEventImagePath());
                        startActivity(goToEventPage);

                    }
                });

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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}