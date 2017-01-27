package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.facebook.FacebookSdk.getApplicationContext;


public class EventFragment extends Fragment {

    private DatabaseReference myDatabase, mDatabaseLike, mDatabaseFavourites;
    private FirebaseUser currentUser;
    private FirebaseRecyclerAdapter<Event,MainUserPage.MyEventViewHolder> firebaseRecyclerAdapter;
    private boolean mProcessLike = false;
    private RecyclerView recyclerView;
    private boolean isMale = true;
    private Snackbar snackBar;
    private ValueEventListener likeListener;
    private boolean hasLike = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), "LUL",Snackbar.LENGTH_SHORT);
        //per cambiare il background della snackbar
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        //UI
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



    public void onStart() {
        super.onStart();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, MainUserPage.MyEventViewHolder>(
                //dati relativi al modello di evento
                Event.class,
                R.layout.single__event,
                MainUserPage.MyEventViewHolder.class,
                myDatabase.child("Events").orderByChild("rLikes")
        ) {
            @Override
            protected void populateViewHolder(final MainUserPage.MyEventViewHolder viewHolder, final Event model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setEventName(model.getEventName());
                viewHolder.setEventImage(getApplicationContext(),model.getEventImagePath());
                viewHolder.setCreatorAvatar(getApplicationContext(),model.getCreatorAvatarPath());
                viewHolder.setLikes(model.getLikes());
                viewHolder.setCardDate(model.getEventDate());
                viewHolder.setCardTime(model.getEventTime());

                //per visualizzare correttamente i like
                ValueEventListener likeCheckerListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(post_key).hasChild(currentUser.getUid())){
                            viewHolder.setThumbDown();
                            mDatabaseLike.removeEventListener(this);
                        }else{
                            viewHolder.setThumbUp();
                            mDatabaseLike.removeEventListener(this);
                        }
                        mDatabaseLike.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mDatabaseLike.addValueEventListener(likeCheckerListener);


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
                        likeListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //se il tasto like è "spento"
                                if(mProcessLike) {
                                    //se l'utente è presente fra i like del rispettivo evento
                                    if (dataSnapshot.child(post_key).hasChild(MainUserPage.userId)) {
                                        FirebaseDatabase.getInstance().getReference()
                                                        .child("Likes")
                                                        .child(post_key)
                                                        .child(MainUserPage.userId).removeValue();
                                        FirebaseDatabase.getInstance().getReference()
                                                        .child("Events")
                                                        .child(post_key).runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                Event event = mutableData.getValue(Event.class);
                                                if(event == null){
                                                    return Transaction.success(mutableData);
                                                }

                                                //like utente maschio
                                                if (isMale){
                                                    event.likes--;
                                                    event.rLikes++;
                                                    event.maleFav--;
                                                    event.totalAge = event.totalAge - MainUserPage.userAge;
                                                    //maschio e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles--;
                                                    }

                                                    //maschio e impegnato
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged--;
                                                    }

                                                }
                                                //like utente donna
                                                if(!isMale){
                                                    event.likes--;
                                                    event.rLikes++;
                                                    event.femaleFav--;
                                                    event.totalAge = event.totalAge - MainUserPage.userAge;
                                                    //donna e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles--;
                                                    }

                                                    //donna e impegnata
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged--;
                                                    }
                                                }
                                                mutableData.setValue(event);
                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                removeLikeListener(FirebaseDatabase.getInstance().getReference()
                                                                                   .child("Likes"),likeListener);
                                                snackBar.setText("Evento rimosso dai preferiti");
                                                snackBar.show();
                                            }
                                        });

                                        mProcessLike = false;
                                        //se l'utente non è presente nei like dell'evento
                                    } else {
                                        FirebaseDatabase.getInstance().getReference()
                                                        .child("Likes")
                                                        .child(post_key)
                                                        .child(MainUserPage.userId).setValue(isMale);
                                        FirebaseDatabase.getInstance().getReference()
                                                        .child("Events")
                                                        .child(post_key).runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                Event event = mutableData.getValue(Event.class);
                                                //handler per nullpointer
                                                if(event == null){
                                                    return Transaction.success(mutableData);
                                                }

                                                //like utente maschio
                                                if (isMale){
                                                    event.likes++;
                                                    event.rLikes--;
                                                    event.maleFav++;
                                                    event.totalAge = event.totalAge + MainUserPage.userAge;
                                                    //maschio e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles++;
                                                    }
                                                    //maschio e impegnato
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged++;
                                                    }
                                                }
                                                //like utente donna
                                                if(!isMale){
                                                    event.likes++;
                                                    event.rLikes--;
                                                    event.femaleFav++;
                                                    event.totalAge = event.totalAge + MainUserPage.userAge;
                                                    //donna e single
                                                    if(MainUserPage.isSingle){
                                                        event.numberOfSingles++;
                                                    }
                                                    //donna e impegnata
                                                    if(!MainUserPage.isSingle){
                                                        event.numberOfEngaged++;
                                                    }
                                                }
                                                mutableData.setValue(event);
                                                return Transaction.success(mutableData);
                                            }
                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                removeLikeListener(FirebaseDatabase.getInstance().getReference()
                                                                                   .child("Likes"),likeListener);
                                                snackBar.setText("Evento aggiunto ai preferiti");
                                                snackBar.show();
                                            }
                                        });

                                        mProcessLike = false;
                                    }
                                }//[END]if mProcessLike
                            }//[END] DataSnapshot

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}

                        }; //[END] fine ValueEventListener

                        FirebaseDatabase.getInstance().getReference().child("Likes").addValueEventListener(likeListener);


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



    protected void removeLikeListener(DatabaseReference myReference, ValueEventListener myListener){
        myReference.removeEventListener(myListener);

    }



}
