package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
    private ImageButton button1;
    private String userId;
    private DatabaseReference userDataReference;
    private TextView ageView,cityView;
    private Integer userAge;
    private FirebaseRecyclerAdapter smallEventAdapter;
    private RecyclerView mEventList;
    private DatabaseReference favEventReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        //elementi UI
        userAvatar = (CircularImageView) findViewById(R.id.profileUserAvatar);
        profileUserName = (TextView)findViewById(R.id.profileUserName);
        button1 = (ImageButton)findViewById(R.id.button1);
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



        userDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userDataReference.keepSynced(true);
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


        //prova pulsante
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UserProfile.this,"ok",Toast.LENGTH_SHORT).show();
            }
        });
    }



    //View holder per l'immagine piccola dell'evento
    public static class SmallEventViewholder extends RecyclerView.ViewHolder{

        TextView smallEventName,smallEventTime,smallEventDate;
        CircularImageView smallEventAvatar;
        View sView;
        public SmallEventViewholder(View itemView) {
            super(itemView);
            sView = itemView;
            //elementi ui del thumbnail dell'evento
            smallEventTime = (TextView)sView.findViewById(R.id.sCardEventTime);
            smallEventDate = (TextView)sView.findViewById(R.id.sCardEventDate);
            smallEventName = (TextView)sView.findViewById(R.id.sCardEventName);
            smallEventAvatar = (CircularImageView)sView.findViewById(R.id.sCardEventAvatar);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        //adattatore per l'immagine piccola dell'evento

        favEventReference = FirebaseDatabase.getInstance().getReference().child("favList").child(userId);
        favEventReference.keepSynced(true);

        smallEventAdapter = new FirebaseRecyclerAdapter<Event,SmallEventViewholder>(

                Event.class,
                R.layout.small_event_preview,
                SmallEventViewholder.class,
                favEventReference
        ) {
            @Override
            protected void populateViewHolder(SmallEventViewholder viewHolder, Event model, int position) {
                viewHolder.setSmallEventName(model.getEventName());
                viewHolder.setSmallEventDate(model.getEventDate());
                viewHolder.setSmallEventTime(model.getEventTime());
                viewHolder.setSmallEventAvatar(getApplicationContext(),model.getEventImagePath());

            }
        };

        mEventList.setAdapter(smallEventAdapter);
    }

//adattatore per l'immagine piccola dell'evento


    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}


