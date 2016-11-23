package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserProfile extends AppCompatActivity {

    private String userName;
    private String userSurname;
    private String avatarPath,userCity;
    private CircularImageView userAvatar;
    private TextView profileUserName;
    private ImageButton privateChat;
    private String userId;
    private DatabaseReference userDataReference;
    private TextView ageView,cityView;
    private Integer userAge;
    private FirebaseRecyclerAdapter smallEventAdapter;
    private RecyclerView mEventList;
    private DatabaseReference favEventReference, myDatabase,mDatabaseFavourites;
    private DatabaseReference eventDatabaseReference, mDatabaseLike;
    private Boolean mProcessLike = false;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        //elementi UI
        userAvatar = (CircularImageView) findViewById(R.id.profileUserAvatar);
        profileUserName = (TextView)findViewById(R.id.profileUserName);
        privateChat = (ImageButton)findViewById(R.id.private_chat_btn);
        cityView = (TextView)findViewById(R.id.cityView);
        ageView = (TextView)findViewById(R.id.ageView);
        mEventList = (RecyclerView)findViewById(R.id.smallEventList);
        //recupera extra dall'Intent
        userId = getIntent().getExtras().getString("TARGET_USER");

        //roba di Firebase
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mEventList.setHasFixedSize(true);
        mEventList.setLayoutManager(linearLayoutManager);


        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("favList");
        userDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userDataReference.keepSynced(true);
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //recupera i dati rispettivamente all'ID fornito così che ogni pulsante debba fornire nell'Intent
        //solo l'ID dell'utente richiesto;
        userDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User selectedUser = dataSnapshot.getValue(User.class);
                //trova e setta il nome
                userName = selectedUser.getUserName();
                userSurname = selectedUser.getUserSurname();
                profileUserName.setText(userName+" "+userSurname);
                //trova e setta la città
                userCity = selectedUser.getUserCity();
                cityView.setText("Viene da : "+userCity);
                //trova e setta l'età
                userAge = selectedUser.getUserAge();
                ageView.setText("Età : "+userAge);
                //trova e setta l'immagine di profilo
                avatarPath = selectedUser.getProfileImage();
                //TODO aggiungere network Policy Offline
                Picasso.with(getApplicationContext()).load(avatarPath).into(userAvatar);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


       //Avvia una chat privata
       privateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent pChat = new Intent(UserProfile.this,PrivateChat.class);
                FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String starterUserName = dataSnapshot.getValue(User.class).getUserName();
                        String starterUserSurname = dataSnapshot.getValue(User.class).getUserSurname();
                        String privateChatName = starterUserName+" "+starterUserSurname;
                        pChat.putExtra("CHAT_NAME",privateChatName);
                        pChat.putExtra("EVENT_ID_FOR_CHAT",userId);
                        startActivity(pChat);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {/*null*/}
                });

            }
        });
    }



    //View holder per l'immagine piccola dell'evento
    public static class SmallEventViewholder extends RecyclerView.ViewHolder{

        ImageButton smallFavButton;
        TextView smallEventName,smallEventTime,smallEventDate;
        CircularImageView smallEventAvatar;
        View sView;
        TextView likeView;
        public SmallEventViewholder(View itemView) {
            super(itemView);
            sView = itemView;
            //elementi ui del thumbnail dell'evento
            smallEventTime = (TextView)sView.findViewById(R.id.sCardEventTime);
            smallEventDate = (TextView)sView.findViewById(R.id.sCardEventDate);
            smallEventName = (TextView)sView.findViewById(R.id.sCardEventName);
            smallEventAvatar = (CircularImageView)sView.findViewById(R.id.sCardEventAvatar);
            smallFavButton = (ImageButton)sView.findViewById(R.id.sFavouriteButton);
            likeView = (TextView)sView.findViewById(R.id.smallLikeView);
        }
        //metodi per settare la roba dell'evento
        public void setSmallEventName (String eventName){smallEventName.setText(eventName);}
        public void setSmallEventTime (String eventTime){smallEventTime.setText(eventTime);}
        public void setSmallEventDate (String eventDate){smallEventDate.setText(eventDate);}
        public void setSmallEventAvatar (final Context ctx, final String avatarUrl){
            Picasso.with(ctx)
                    .load(avatarUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(smallEventAvatar, new Callback() {
                        @Override
                        public void onSuccess() {/*null*/}
                        @Override
                        public void onError() {Picasso.with(ctx).load(avatarUrl).into(smallEventAvatar);
                        }
                    });
        }
        public void setSmallEventLike (Integer likes){likeView.setText(""+likes);}
        public void setEmptyStar (){
            smallFavButton.setImageResource(R.drawable.vector_empty_star24);
        }
        public void setFullStar (){smallFavButton.setImageResource(R.drawable.vector_full_star24);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //adattatore per l'immagine piccola dell'evento

        favEventReference = FirebaseDatabase.getInstance().getReference().child("favList").child(userId);
        favEventReference.keepSynced(true);

        smallEventAdapter = new FirebaseRecyclerAdapter<Event, SmallEventViewholder>(

                Event.class,
                R.layout.small_event_preview,
                SmallEventViewholder.class,
                favEventReference
        )
        {
            @Override
            protected void populateViewHolder(final SmallEventViewholder viewHolder, Event model, int position) {

                final String sEventKey = getRef(position).getKey();
                viewHolder.setSmallEventName(model.getEventName());
                viewHolder.setSmallEventDate(model.getEventDate());
                viewHolder.setSmallEventTime(model.getEventTime());
                viewHolder.setSmallEventAvatar(getApplicationContext(), model.getEventImagePath());
                viewHolder.setSmallEventLike(model.getLikes());



                //imposta il giusto pulsante per i favourite
                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(sEventKey).hasChild(currentUser.getUid())){
                            viewHolder.setFullStar();
                        }else{
                            viewHolder.setEmptyStar();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                //permette di vedere l'evento in dettaglio
                viewHolder.sView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent toEventPage = new Intent(UserProfile.this, EventPage.class);
                        String sEventId = favEventReference.child(sEventKey).getKey();
                        Toast.makeText(UserProfile.this, "" + sEventId, Toast.LENGTH_SHORT).show();
                        toEventPage.putExtra("EVENT_ID", sEventId);
                        startActivity(toEventPage);
                    }
                });

                //Onclick per il pulsante like
                viewHolder.smallFavButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;
                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //se il tasto like è "spento"
                                if(mProcessLike) {
                                    //se l'utente è presente fra i like del rispettivo evento
                                    if (dataSnapshot.child(sEventKey).hasChild(currentUser.getUid())) {
                                        mDatabaseLike.child(sEventKey).child(currentUser.getUid()).removeValue();
                                        myDatabase.child("Events").child(sEventKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                current_likes--;
                                                current_rlikes++;
                                                myDatabase.child("Events").child(sEventKey).child("likes").setValue(current_likes);
                                                myDatabase.child("Events").child(sEventKey).child("rLikes").setValue(current_rlikes);
                                                //rimuove l'evento dai favoriti dell'utente
                                                myDatabase.child("favList").child(currentUser.getUid()).child(sEventKey).removeValue();
                                                Toast.makeText(UserProfile.this,"Evento rimosso dai preferiti",Toast.LENGTH_LONG).show();
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {/*niente*/}
                                        });
                                        mProcessLike = false;
                                        //se l'utente non è presente nei like dell'evento
                                    } else {
                                        mDatabaseLike.child(sEventKey).child(currentUser.getUid()).setValue("RandomValue");
                                        myDatabase.child("Events").child(sEventKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                current_likes++;
                                                current_rlikes--;
                                                myDatabase.child("Events").child(sEventKey).child("likes").setValue(current_likes);
                                                myDatabase.child("Events").child(sEventKey).child("rLikes").setValue(current_rlikes);
                                                //aggiunge l'evento ai favoriti dell'utente
                                                myDatabase.child("favList").child(currentUser.getUid()).child(sEventKey).setValue(dataSnapshot.getValue(Event.class));
                                                mProcessLike = false;
                                                Toast.makeText(UserProfile.this,"Evento aggiunto ai preferiti",Toast.LENGTH_LONG).show();

                                                mDatabaseFavourites.child(currentUser.getUid()).child(sEventKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                        Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                        current_likes++;
                                                        current_rlikes--;
                                                        mDatabaseFavourites.child(currentUser.getUid()).child(sEventKey).child("likes").setValue(current_likes);
                                                        mDatabaseFavourites.child(currentUser.getUid()).child(sEventKey).child("rLikes").setValue(current_rlikes);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {/*null*/}
                                        });

                                        mProcessLike = false;
                                    }
                                }//[END]if mProcessLike
                            }//[END] DataSnapshot

                            @Override
                            public void onCancelled(DatabaseError databaseError) {/*null*/}
                        }); //[END] fine ValueEventListener
                    }
                }); //[END] fine OnClickListener




            }
        };

        mEventList.setAdapter(smallEventAdapter);
    }//[END] OnStart

    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smallEventAdapter.cleanup();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}