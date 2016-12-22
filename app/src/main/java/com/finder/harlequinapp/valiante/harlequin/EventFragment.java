package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.flaviofaria.kenburnsview.KenBurnsView;
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

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.facebook.FacebookSdk.getApplicationContext;


public class EventFragment extends Fragment {


    private Button settings;
    private Button addEventButton;
    private Button logOutButton;
    private DatabaseReference myDatabase, mDatabaseLike, mDatabaseFavourites;
    private FirebaseUser currentUser;
    private FirebaseRecyclerAdapter<Event,UserPage.EventViewHolder> firebaseRecyclerAdapter;
    private CircularImageView avatar;
    private CircularImageView cardAvatar,cardLike,cardInfo;
    private KenBurnsView kvb;
    private Context context;
    private boolean mProcessLike = false;
    private RecyclerView recyclerView;
    private ImageButton homeButton,messageButton;
    private MaterialRippleLayout rippleProfile,rippleHome,rippleMessage;
    private boolean isMale = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("favList");
        myDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = (RecyclerView)inflater.inflate(R.layout.event_fragment_layout, container,false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return recyclerView;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircularImageView cardProfile;
        TextView cardLikes,cardDate,cardTime;
        ImageButton cardLike,chatRoomBtn;

        //costruttore del View Holder personalizzato
        public EventViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            //Elementi UI per la carta evento
            cardLike = (ImageButton)mView.findViewById(R.id.CardLike);
            cardProfile = (CircularImageView)mView.findViewById(R.id.smallAvatar);
            chatRoomBtn = (ImageButton)mView.findViewById(R.id.chatRoomBtn);
            cardLikes = (TextView)mView.findViewById(R.id.cardLikeCounter);
            cardDate = (TextView)mView.findViewById(R.id.cardDay);
            cardTime = (TextView)mView.findViewById(R.id.cardTime);

        }
        //metodi necessari per visualizzare dinamicamente i dati di ogni EventCard
        public void setThumbUp (){
            cardLike.setImageResource(R.drawable.vector_empty_star24);
        }
        public void setThumbDown (){
            cardLike.setImageResource(R.drawable.vector_full_star24);
        }
        //TODO fa vedere i like correnti, da migliorare
        public void setLikes (Integer likes){cardLikes.setText("Partecipanti: "+likes); }
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
        /*
        public void setDescription (String description){
            TextView event_desc = (TextView)mView.findViewById(R.id.CardViewDescription);
            event_desc.setText(description);
        }*/
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
        public void setCardDate (String eventDate){
            cardDate.setText("Data : "+eventDate);
        }
        public void setCardTime (String eventTime) { cardTime.setText("Orario : "+eventTime);}
    }//[END]eventViewHolder

    public void onStart() {
        super.onStart();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, UserPage.EventViewHolder>(
                //dati relativi al modello di evento
                Event.class,
                R.layout.single__event,
                UserPage.EventViewHolder.class,
                myDatabase.child("Events").orderByChild("rLikes")
        ) {
            @Override
            protected void populateViewHolder(final UserPage.EventViewHolder viewHolder, final Event model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setEventName(model.getEventName());
                viewHolder.setEventImage(getApplicationContext(),model.getEventImagePath());
                viewHolder.setCreatorAvatar(getApplicationContext(),model.getCreatorAvatarPath());
                viewHolder.setLikes(model.getLikes());
                viewHolder.setCardDate(model.getEventDate());
                viewHolder.setCardTime(model.getEventTime());

                //imposta il giusto pulsante per il like al caricamento dell'activity
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

                //OnClick per la chatRoom dell'evento
                viewHolder.chatRoomBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String event_id= myDatabase.child("Events").child(post_key).getKey();
                        Intent smallGoChat = new Intent (getContext(),ChatRoom.class);
                        smallGoChat.putExtra("CHAT_NAME",model.getEventName());
                        smallGoChat.putExtra("EVENT_ID_FOR_CHAT", event_id);
                        startActivity(smallGoChat);
                    }
                });

                //OnClick per il profilo del creatore
                viewHolder.cardProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent goToProfile = new Intent (getContext(),UserProfile.class);
                        String creatorId = model.getCreatorId();
                        goToProfile.putExtra("TARGET_USER", creatorId);
                        startActivity(goToProfile);
                    }
                });
                //OnClick per la finestra completa dell'evento


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
                                                Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                current_likes--;
                                                current_rlikes++;
                                                if(isMale){
                                                    Integer current_males = dataSnapshot.getValue(Event.class).getMaleFav();
                                                    current_males--;
                                                    myDatabase.child("Events").child(post_key).child("maleFav").setValue(current_males);
                                                }
                                                if(!isMale){
                                                    Integer current_females = dataSnapshot.getValue(Event.class).getFemaleFav();
                                                    current_females--;
                                                    myDatabase.child("Events").child(post_key).child("maleFav").setValue(current_females);
                                                }
                                                myDatabase.child("Events").child(post_key).child("likes").setValue(current_likes);
                                                myDatabase.child("Events").child(post_key).child("rLikes").setValue(current_rlikes);
                                                //rimuove l'evento dai favoriti dell'utente
                                                myDatabase.child("favList").child(currentUser.getUid()).child(post_key).removeValue();
                                                /*
                                                Toast.makeText(UserPage.this,"Evento rimosso dai preferiti",Toast.LENGTH_LONG).show();
                                                mDatabaseFavourites.child(currentUser.getUid()).child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                        Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                        current_likes--;
                                                        current_rlikes++;
                                                        mDatabaseFavourites.child(currentUser.getUid()).child(post_key).child("likes").setValue(current_likes);
                                                        mDatabaseFavourites.child(currentUser.getUid()).child(post_key).child("rLikes").setValue(current_rlikes);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                                */
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {/*niente*/}
                                        });

                                        mProcessLike = false;
                                        //se l'utente non è presente nei like dell'evento
                                    } else {
                                        mDatabaseLike.child(post_key).child(currentUser.getUid()).setValue(isMale);
                                        myDatabase.child("Events").child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                if(isMale){
                                                    Integer current_males = dataSnapshot.getValue(Event.class).getMaleFav();
                                                    current_males++;
                                                    myDatabase.child("Events").child(post_key).child("maleFav").setValue(current_males);
                                                }
                                                if(!isMale){
                                                    Integer current_females = dataSnapshot.getValue(Event.class).getFemaleFav();
                                                    current_females++;
                                                    myDatabase.child("Events").child(post_key).child("maleFav").setValue(current_females);
                                                }
                                                current_likes++;
                                                current_rlikes--;
                                                myDatabase.child("Events").child(post_key).child("likes").setValue(current_likes);
                                                myDatabase.child("Events").child(post_key).child("rLikes").setValue(current_rlikes);
                                                //aggiunge l'evento ai favoriti dell'utente
                                                myDatabase.child("favList").child(currentUser.getUid()).child(post_key).setValue(dataSnapshot.getValue(Event.class));
                                                mProcessLike = false;
                                                Toast.makeText(getContext(),"Evento aggiunto ai preferiti",Toast.LENGTH_LONG).show();

                                                mDatabaseFavourites.child(currentUser.getUid()).child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                        Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                        current_likes++;
                                                        current_rlikes--;
                                                        mDatabaseFavourites.child(currentUser.getUid()).child(post_key).child("likes").setValue(current_likes);
                                                        mDatabaseFavourites.child(currentUser.getUid()).child(post_key).child("rLikes").setValue(current_rlikes);
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

                //Onclick per il view generalizzato
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String event_id= myDatabase.child("Events").child(post_key).getKey();
                        Intent goToEventPage = new Intent (getContext(),EventPage.class);
                        goToEventPage.putExtra("EVENT_NAME",model.getEventName());
                        goToEventPage.putExtra("EVENT_DESCRIPTION",model.getDescription());
                        goToEventPage.putExtra("EVENT_IMAGE_URL",model.getEventImagePath());
                        goToEventPage.putExtra("EVENT_ID",event_id);
                        startActivity(goToEventPage);
                    }
                });// END onclick view generalizzato


            }// END populateViewHolder
        };// END firebaseRecyclerAdapter

        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }// END OnStart

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

    //per usare i font personalizzati

}
