package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class UserFavourites extends AppCompatActivity {

    private MaterialRippleLayout ufHomeButton,  ufMessageButton,ufEventButton;
    private RecyclerView favouriteEvents;
    private FirebaseUser currentUser;
    private Toolbar favToolbar;
    private DatabaseReference favouritesListRef;
    private String userId;
    private FirebaseRecyclerAdapter favouritesEventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favourites);

        ufHomeButton = (MaterialRippleLayout)findViewById(R.id.fav_rippleHome);
        ufMessageButton = (MaterialRippleLayout)findViewById(R.id.fav_rippleMessage);
        ufEventButton = (MaterialRippleLayout)findViewById(R.id.fav_rippleProfile);
        favouriteEvents = (RecyclerView)findViewById(R.id.fav_event_list);
        favToolbar = (Toolbar)findViewById(R.id.fav_tool_bar);


        favouriteEvents.setHasFixedSize(true);
        favouriteEvents.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(favToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //inizializzazione di Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();

        //si assicura che l'utente sia loggato ed inizializza una referenza al database che punta
        //ai preferiti ell'utente
        if (userId.isEmpty()){
            finish();
        }
        else{
            favouritesListRef = FirebaseDatabase.getInstance().getReference().child("favList").child(userId);
        }

    }//[FINE DI ONCREATE]

    public static class FavouritesViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircularImageView eventAvatar;
        TextView eventName,eventDate,eventTime;

        public FavouritesViewHolder(View itemView) {
              super(itemView);
            mView = itemView;
            eventAvatar = (CircularImageView)mView.findViewById(R.id.fav_event_avatar);
            eventName   = (TextView)mView.findViewById(R.id.fav_event_name);
            eventDate   = (TextView)mView.findViewById(R.id.fav_event_date);
            eventTime   = (TextView)mView.findViewById(R.id.fav_event_time);
        }

        private void setAvatar (final Context avatarctx, final String avatarUrl){
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
        private void setName (String name){
            eventName.setText(name);
        }

        private void setTime (String time){
            eventTime.setText(time);
        }

        private void setEventDate (String date){
            eventDate.setText(date);
        }
    }
    //[FINE FavouritesEventViewHolder


    @Override
    protected void onStart() {
        super.onStart();

        favouritesEventAdapter = new FirebaseRecyclerAdapter<Event, FavouritesViewHolder>(
                Event.class,
                R.layout.single_fav_event,
                FavouritesViewHolder.class,
                favouritesListRef.orderByChild("eventName")
        ) {
            @Override
            protected void populateViewHolder(FavouritesViewHolder viewHolder, Event model, int position) {
                viewHolder.setAvatar(getApplicationContext(),model.getEventImagePath());
                viewHolder.setEventDate(model.getEventDate());
                viewHolder.setTime(model.getEventTime());
                viewHolder.setName(model.getEventName());


            }


        };
        favouriteEvents.setAdapter(favouritesEventAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
