package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Calendar;

public class DialogProfile extends DialogFragment {

    TextView name,city,age,relationship;
    CircularImageView avatar;
    DatabaseReference userReference,eventReference, userFollowingReference, userFollowersReference,topicReference,pendingRequest;
    ImageButton facebook;
    PackageManager mPackageManager;
    private FirebaseRecyclerAdapter dialogAdapter;
    private String uid,token;
    private String current_city;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RelativeLayout rLayout;
    private RelativeLayout subButton;
    private String sender_uid;
    private Boolean isAlreadyFollowing;
    private ImageView button_icon;
    private TextView button_text;
    private String user_only_name;
    ValueEventListener followingListener;



    public DialogProfile (){

    }

    public static DialogProfile newInstance(String userId,String token){
        DialogProfile frag = new DialogProfile();
        Bundle args = new Bundle();

        args.putString("USER_ID",userId);
        args.putString("TOKEN",token);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_dialog_profile,container);
        userFollowersReference = FirebaseDatabase.getInstance().getReference().child("Followers");
        userFollowingReference = FirebaseDatabase.getInstance().getReference().child("Following");
        topicReference = FirebaseDatabase.getInstance().getReference().child("Topics");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        eventReference = FirebaseDatabase.getInstance().getReference().child("Likes").child("Users");
        pendingRequest = FirebaseDatabase.getInstance().getReference().child("PendingRequest");
        pendingRequest.keepSynced(true);
        userFollowingReference.keepSynced(true);
        userFollowersReference.keepSynced(true);
        topicReference.keepSynced(true);
        userReference.keepSynced(true);
        eventReference.keepSynced(true);
        mPackageManager = getContext().getPackageManager();
        uid = getArguments().getString("USER_ID","some userID");
        token = getArguments().getString("TOKEN","SOME TOKEN");
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.rw);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        current_city = getActivity().getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE).getString("USER_CITY","NA");


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
        subButton = (RelativeLayout) view.findViewById(R.id.sub_button);
        button_icon = (ImageView)view.findViewById(R.id.dialog_button_icon);
        button_text = (TextView)view.findViewById(R.id.sub_buttonText);
        sender_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final User myuser = dataSnapshot.getValue(User.class);
                name.setText(myuser.getUserName()+ " "+myuser.getUserSurname());
                user_only_name = myuser.getUserName();
                Glide.with(getContext())
                        .load(myuser.getProfileImage())
                        .asBitmap()
                        .placeholder(ContextCompat.getDrawable(getActivity(),R.drawable.loading_placeholder)) //da cambiare
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(ContextCompat.getDrawable(getActivity(),R.drawable.ic_error))
                        .into(avatar);
                final String facebookProfile = myuser.getFacebookProfile();
                String userCity = myuser.getUserCity();
                String userAge = getAge(myuser.getUserAge())+" anni";
                String relation = myuser.getUserRelationship();
                String userGender = myuser.getUserGender();

                relationship.setText(relation);
                city.setText(userCity);
                age.setText(userAge);
                if(userGender.equalsIgnoreCase("Donna")){
                    subButton.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.shaded_female));
                    name.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.bottom_female_line));
                }

                facebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!facebookProfile.equalsIgnoreCase("NA")||!facebookProfile.equalsIgnoreCase("default@facebook.com")) {
                            Intent fb = newFacebookIntent(mPackageManager, facebookProfile);
                            startActivity(fb);
                        }else{
                            Toast.makeText(getContext(), "Questo utente non ha specificato il suo account Facebook", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            UbiquoUtils.goToProfile(dataSnapshot.getKey(), true, getActivity());
                        }else{
                            UbiquoUtils.goToProfile(dataSnapshot.getKey(), false, getActivity());

                        }
                    }
                });

                userReference.child(uid).removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userReference.child(uid).addValueEventListener(userListener);

        //legge in following/{current_user}/{target_user}
        //se l'tente viene già seguito cambia colore del pulsante e setta un boolean
        followingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //aggiorna il token dell'utente che invia la richiesta
                UbiquoUtils.refreshCurrentUserToken(getActivity());
                //se l'utente viene già seguito e la richiesta è stata accettata
                if(dataSnapshot.hasChild(uid)){
                    subButton.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.positive_accent));
                    button_text.setText("Segui già "+user_only_name);
                    Drawable check = ContextCompat.getDrawable(getActivity(),R.drawable.vector_white_check_18);
                    button_icon.setImageDrawable(check);
                    isAlreadyFollowing = true;
                    //se l'utente è in following ma la richiesta è ancora Pending
                    if(!dataSnapshot.child(uid).getValue(Boolean.class)){
                        Drawable pending_icon = ContextCompat.getDrawable(getActivity(),R.drawable.vector_white_clock_18);
                        button_icon.setImageDrawable(pending_icon);
                        subButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.blackish));
                        button_text.setText("Richiesta inviata");
                        isAlreadyFollowing = false;
                    }

                }else {
                    Drawable send_arrow = ContextCompat.getDrawable(getActivity(),R.drawable.vector_right_arrow_18);
                    button_icon.setImageDrawable(send_arrow);
                    subButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.matte_blue));
                    button_text.setText("Segui "+user_only_name);
                    isAlreadyFollowing = false;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("Following").child(sender_uid).addValueEventListener(followingListener);


        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //utente non ancora seguito, si manda una richiesta
                if(!isAlreadyFollowing){
                    //dati necessari
                    String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String targetId = uid;
                    SharedPreferences userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
                    String senderToken = FirebaseInstanceId.getInstance().getToken();
                    String targetToken = token;
                    //costruttore Pending Notification
                    Long request_time = System.currentTimeMillis();
                    PendingFollowingRequest newPendingRequest = new PendingFollowingRequest(senderToken,senderId,targetToken,targetId,request_time);

                    //trigger della pending notification
                    UbiquoUtils.pendingNotificationTrigger(senderId,targetId,newPendingRequest);
                    isAlreadyFollowing = true;
                }else{
                    //dati necessari
                    String sender_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String receiver_uid = uid;
                    //rimozione delle interazioni social
                    UbiquoUtils.removeFollowInteractions(sender_uid,receiver_uid);

                    isAlreadyFollowing = false;
                }

            }
        });



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
            public void populateViewHolder(final DialogEventViewHolder viewHolder, Boolean model, final int position) {

                final String posy_key = getRef(position).getKey();
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                DynamicData data = dataSnapshot.getValue(DynamicData.class);
                                viewHolder.setTitle(data.geteName());
                                viewHolder.setAvatar(getContext(), data.getiPath());

                                viewHolder.square_title.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent toEvent = new Intent(getContext(), EventPage.class);
                                        toEvent.putExtra("EVENT_ID", dataSnapshot.getKey());
                                        startActivity(toEvent);
                                    }
                                });

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Events")
                                        .child("Dynamic")
                                        .child(current_city)
                                        .child(posy_key)
                                        .removeEventListener(this);




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                FirebaseDatabase.getInstance().getReference()
                                              .child("Events")
                                              .child("Dynamic")
                                              .child(current_city)
                        .child(posy_key)
                                              .addValueEventListener(eventListener);
            }
        };
        recyclerView.setAdapter(dialogAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance().getReference().child("Following").child(sender_uid).removeEventListener(followingListener);
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
