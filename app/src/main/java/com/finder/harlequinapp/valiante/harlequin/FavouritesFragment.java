package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import static com.facebook.FacebookSdk.getApplicationContext;

public class FavouritesFragment extends Fragment {

    private MaterialRippleLayout ufHomeButton,  ufMessageButton,ufEventButton;
    private RecyclerView favouriteEvents;
    private FirebaseUser currentUser;
    private Toolbar favToolbar;
    private DatabaseReference favouritesListRef;
    private String userId;
    private FirebaseRecyclerAdapter favouritesEventAdapter;
    private Snackbar mSnackbar;
    private Boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike, myDatabase,mDatabaseFavourites;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.favourites_fragment_layout, container, false);

        favouriteEvents = (RecyclerView)rootView.findViewById(R.id.favourite_recycler);
        favouriteEvents.setHasFixedSize(true);
        favouriteEvents.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.keepSynced(true);
        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("favList");


        //inizializzazione di Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();

        //si assicura che l'utente sia loggato ed inizializza una referenza al database che punta
        //ai preferiti ell'utente
        if (userId.isEmpty()){

        }
        else{
            favouritesListRef = FirebaseDatabase.getInstance().getReference().child("favList").child(userId);
        }
        return rootView;
    }


    public static class FavouritesViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircularImageView eventAvatar;
        TextView eventName,eventDate,eventTime;
        ImageButton cardLikeButton;

        public FavouritesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            eventAvatar = (CircularImageView)mView.findViewById(R.id.fav_event_avatar);
            eventName   = (TextView)mView.findViewById(R.id.fav_event_name);
            eventDate   = (TextView)mView.findViewById(R.id.fav_event_date);
            eventTime   = (TextView)mView.findViewById(R.id.fav_event_time);
            cardLikeButton = (ImageButton)mView.findViewById(R.id.cardLikeButton);

        }

        public void setAvatar (final Context avatarctx, final String avatarUrl){
            Picasso.with(avatarctx)
                    .load(avatarUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(eventAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            //va bene così non deve fare nulla
                        }
                        @Override
                        public void onError() {
                            Picasso.with(avatarctx).load(avatarUrl).into(eventAvatar);
                        }
                    });

        }
        public void setName (String name){
            eventName.setText(name);
        }

        public void setTime (String time){
            eventTime.setText(time);
        }

        public void setEventDate (String date){
            eventDate.setText(date);
        }
    }
    //[FINE FavouritesEventViewHolder

    @Override
    public void onStart() {
        super.onStart();

        //TODO ogni fragment non prende l'adapter dal fragment stesso ma dalla classe di riferimento
        favouritesEventAdapter = new FirebaseRecyclerAdapter<Event, UserFavourites.FavouritesViewHolder>(
                Event.class,
                R.layout.single_fav_event,
                UserFavourites.FavouritesViewHolder.class,
                favouritesListRef.orderByChild("eventName")
        ) {
            @Override
            protected void populateViewHolder(UserFavourites.FavouritesViewHolder viewHolder, Event model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setAvatar(getApplicationContext(),model.getEventImagePath());
                viewHolder.setEventDate(model.getEventDate());
                viewHolder.setTime(model.getEventTime());
                viewHolder.setName(model.getEventName());

                viewHolder.cardLikeButton.setOnClickListener(new View.OnClickListener() {
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
                                                if(MainUserPage.isMale){
                                                    Integer current_males = dataSnapshot.getValue(Event.class).getMaleFav();
                                                    current_males--;
                                                    myDatabase.child("Events").child(post_key).child("maleFav").setValue(current_males);
                                                }
                                                if(!MainUserPage.isMale){
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
                                        mDatabaseLike.child(post_key).child(currentUser.getUid()).setValue(MainUserPage.isMale);
                                        myDatabase.child("Events").child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Integer current_likes = dataSnapshot.getValue(Event.class).getLikes();
                                                Integer current_rlikes = dataSnapshot.getValue(Event.class).getrLikes();
                                                if(MainUserPage.isMale){
                                                    Integer current_males = dataSnapshot.getValue(Event.class).getMaleFav();
                                                    current_males++;
                                                    myDatabase.child("Events").child(post_key).child("maleFav").setValue(current_males);
                                                }
                                                if(!MainUserPage.isMale){
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
                                                Toast.makeText(getApplicationContext(),"Evento aggiunto ai preferiti",Toast.LENGTH_LONG).show();

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
            }




        };
        favouriteEvents.setAdapter(favouritesEventAdapter);
    }
}
