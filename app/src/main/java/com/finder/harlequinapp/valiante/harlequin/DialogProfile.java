package com.finder.harlequinapp.valiante.harlequin;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.RelativeLayout;
import android.widget.TextView;


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


import java.util.Calendar;

public class DialogProfile extends DialogFragment {

    TextView name,city,age,relationship;
    CircularImageView avatar;
    DatabaseReference userReference,eventReference;
    ImageButton facebook;
    PackageManager mPackageManager;
    private FirebaseRecyclerAdapter dialogAdapter;
    private String uid;
    private String current_city = "Isernia";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RelativeLayout rLayout;
    private Button exit;



    public DialogProfile (){

    }

    public static DialogProfile newInstance(String userId){
        DialogProfile frag = new DialogProfile();
        Bundle args = new Bundle();

        args.putString("USER_ID",userId);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_dialog_profile,container);

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        eventReference = FirebaseDatabase.getInstance().getReference().child("Likes").child("Users");
        mPackageManager = getContext().getPackageManager();
        uid = getArguments().getString("USER_ID","some userID");
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.rw);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        city = (TextView)view.findViewById(R.id.dialogCity);
        age = (TextView)view.findViewById(R.id.dialogAge);
        avatar = (CircularImageView) view.findViewById(R.id.dialogProfile);
        name  = (TextView)view.findViewById(R.id.dialogName);
        relationship = (TextView)view.findViewById(R.id.dialogRel);
        facebook = (ImageButton)view.findViewById(R.id.fb_btn);
        exit = (Button)view.findViewById(R.id.dialogExit);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User myuser = dataSnapshot.getValue(User.class);
                name.setText(myuser.getUserName()+ " "+myuser.getUserSurname());

                Picasso.with(getContext())
                        .load(myuser.getProfileImage())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(avatar, new Callback() {
                            @Override
                            public void onSuccess() {
                                //va bene così non deve fare nulla
                            }
                            @Override
                            public void onError() {
                                Picasso.with(getContext()).load(myuser.getProfileImage()).into(avatar);
                            }
                        });

                final String facebookProfile = myuser.getFacebookProfile();
                String userCity = myuser.getUserCity();
                String userAge = getAge(myuser.getUserAge())+" anni";
                String relation = myuser.getUserRelationship();
                String userGender = myuser.getUserGender();

                relationship.setText(relation);
                city.setText(userCity);
                age.setText(userAge);
                if(userGender.equalsIgnoreCase("Donna")){
                    exit.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.shaded_female));
                    name.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.bottom_female_line));
                }


                if(facebookProfile!=null) {

                    facebook.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent fb = newFacebookIntent(mPackageManager, facebookProfile);
                            startActivity(fb);
                        }
                    });
                }
                userReference.child(uid).removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userReference.child(uid).addValueEventListener(userListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        dialogAdapter = new FirebaseRecyclerAdapter<Boolean,DialogEventViewHolder>(
                Boolean.class,
                R.layout.square_event,
                DialogEventViewHolder.class,
                eventReference.child(uid)
                ){
            @Override
            public void populateViewHolder(final DialogEventViewHolder viewHolder, Boolean model, int position) {

                final String posy_key = getRef(position).getKey();
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                            DynamicData data = dataSnapshot.getValue(DynamicData.class);
                                viewHolder.setTitle(data.geteName());
                                viewHolder.setAvatar(getContext(),data.getiPath());

                        viewHolder.square_title.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent toEvent = new Intent(getContext(),EventPage.class);
                                toEvent.putExtra("EVENT_ID",dataSnapshot.getKey());
                                startActivity(toEvent);
                            }
                        });

                        FirebaseDatabase.getInstance().getReference()
                                .child("Events")
                                .child("Dynamic")
                                .child(current_city)
                                .child(posy_key).removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                FirebaseDatabase.getInstance().getReference()
                                              .child("Events")
                                              .child("Dynamic")
                                              .child(current_city)
                                              .child(posy_key).addValueEventListener(eventListener);
            }
        };
        recyclerView.setAdapter(dialogAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerView.setAdapter(null);
        dialogAdapter.cleanup();
    }

    //metodo per costruire correttamente l'intent per aprire la pagina di facebook
    //se facebook non è installato restituisce il link per aprirlo dal browser
    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    //calcola l'età da String a Integer
    public Integer getAge (String birthdate){
        //estrae i numeri dalla stringa
        String parts [] = birthdate.split("/");
        //li casta in interi
        Integer day = Integer.parseInt(parts[0]);
        Integer month = Integer.parseInt(parts[1]);
        Integer year = Integer.parseInt(parts[2]);

        //oggetto per l'anno di nascita
        Calendar dob = Calendar.getInstance();
        //oggetto per l'anno corrente
        Calendar today = Calendar.getInstance();

        //setta anno di nascita in formato data
        dob.set(year,month,day);
        //calcola l'anno
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        //controlla che il giorno attuale sia minore del giorno del compleanno
        //nel caso in cui fosse vero allora il compleanno non è ancora passato e il conteggio degli anni viene diminuito
        if (today.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        //restituisce l'età sotto forma numerica utile per calcolare l'età media dei partecipanti ad un evento
        return age;

    }
}
