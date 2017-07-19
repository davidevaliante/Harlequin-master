package com.finder.harlequinapp.valiante.harlequin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.piotrek.customspinner.CustomSpinner;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class EventFragment extends Fragment {

    protected final String[] my_ordering = {"date", "nLike", "eName"};
    private DatabaseReference myDatabase, mDatabaseLike, mDatabaseFavourites, eventLikeRef, dynamicEvents;
    private FirebaseUser currentUser;
    protected Integer orderingSelector = 1;
    protected  FirebaseRecyclerAdapter<DynamicData, MyEventViewHolder> eventAdapter;
    private boolean mProcessLike = false;
    private RecyclerView recyclerView;

    private Snackbar snackBar;
    private ValueEventListener likeListener;

    private SharedPreferences userData;
    protected String userName, userId;
    private Integer userAge;
    private boolean isMale, isSingle;
    protected String citySelector = "Isernia";
    protected ValueEventListener likeUpdater;
    private Float pos = 0.0f;
    private Integer firstItem = 0;
    private Parcelable savedRecyclerViewState, rcState;

    protected String current_city;
    private PendingIntent myPendingIntent;
    //private FenceReceiver myFenceReceiver;
    private GoogleApiClient mApiClient;

    // The intent action which will be fired when your fence is triggered.

    //private static final int MY_PERMISSION_LOCATION = 1;
    private final String FENCE_KEY = "fence_key";
    private String FENCE_RECEIVER_ACTION;
    private SharedPreferences.Editor editor;
    private CustomSpinner spinner;
    protected  Integer selector = 0;
    private Integer rcPosition;




    protected LinearLayoutManager linearLayoutManager;

    private final String BUNDLE_RECYCLER_LAYOUT = "RECYCLERVIEW_STATE";




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        current_city = ((MainUserPage)getActivity()).current_city;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.event_fragment_layout, container, false);
        recyclerView = (RecyclerView) viewGroup.findViewById(R.id.fragment_event_list);

        if(current_city==null){
            current_city = getActivity().getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE).getString("USER_CITY","NA");
        }

        //carica i dati dell'utente
        getUserData();
        userData = getActivity().getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE);
        snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), "LUL", Snackbar.LENGTH_SHORT);
        //per cambiare il background della snackbar
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryDark));
        editor = userData.edit();
        spinner = (CustomSpinner)getActivity().findViewById(R.id.spinner);

        //UI
        eventLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child(current_city);
        eventLikeRef.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child(current_city);
        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("favList");
        dynamicEvents = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(current_city);
        dynamicEvents.keepSynced(true);
        myDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //recyclerview
        linearLayoutManager = new LinearLayoutManager((getActivity()));
        recyclerView.setLayoutManager(linearLayoutManager);
        //evita che la cardView "blinki"quando viene premuto il like
        recyclerView.getItemAnimator().setChangeDuration(0);

        eventAdapter=newAdapter();
        recyclerView.setAdapter(eventAdapter);



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 1:
                        selector=0;
                        if(eventAdapter != null) {
                            eventAdapter.cleanup();
                        }
                        recyclerView.setAdapter(null);
                        eventAdapter = newAdapter();
                        recyclerView.setAdapter(eventAdapter);
                        break;
                    case 2:
                        selector=1;
                        if(eventAdapter != null) {
                            eventAdapter.cleanup();
                        }
                        recyclerView.setAdapter(null);
                        eventAdapter = newAdapter();
                        recyclerView.setAdapter(eventAdapter);
                        break;
                    case 3:
                        selector=2;
                        if(eventAdapter != null) {
                            eventAdapter.cleanup();
                        }
                        recyclerView.setAdapter(null);
                        eventAdapter = newAdapter();
                        recyclerView.setAdapter(eventAdapter);

                        break;


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        final Handler tutorialHandler = new Handler();
        tutorialHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //refresha il token
                showTutorial();
            }
        },1000);


        return viewGroup;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    //contiene RecyclerView
    public void onStart() {
        super.onStart();
        //eventAdapter = newAdapter();
        //recyclerView.setAdapter(eventAdapter);


    }// END OnStart

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        //recyclerView.setAdapter(eventAdapter);
        if(getActivity().getIntent().getStringExtra("EVENT_ID") != null){
            Intent toEventPage = new Intent(getActivity(),EventPage.class);
            toEventPage.putExtra("EVENT_ID",getActivity().getIntent().getStringExtra("EVENT_ID"));

            startActivity(toEventPage);
        }
        if (rcState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(rcState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        rcPosition = linearLayoutManager.findFirstVisibleItemPosition();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(eventAdapter != null) {
            eventAdapter.cleanup();
        }
        recyclerView.setAdapter(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(eventAdapter != null) {
            eventAdapter.cleanup();
        }
        recyclerView.setAdapter(null);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logOutButton:
                UbiquoUtils.logOut(getActivity());
                getActivity().finish();
                break;
            case R.id.orderByJoiners:
                orderingSelector = 0;
                recyclerView.setAdapter(null);
                onStart();

                break;
            case R.id.orderByTime:
                orderingSelector = 1;
                recyclerView.setAdapter(null);
                onStart();

                break;
            case R.id.alphabetic:
                orderingSelector = 2;
                //recyclerView.setAdapter(null);
                onStart();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    //funziona
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //salva lo stato del recyclerView nel bundle
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, linearLayoutManager.onSaveInstanceState());
        rcState = outState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //rimuove i listener per le funzionalità like
    protected void removeLikeListener(DatabaseReference myReference, ValueEventListener myListener) {
        myReference.removeEventListener(myListener);

    }

    protected void getUserData() {
        userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        userName = userData.getString("USER_NAME", "Name error");
        userAge = userData.getInt("USER_AGE", 25);
        isSingle = userData.getBoolean("IS_SINGLE", true);
        isMale = userData.getBoolean("IS_MALE", true);
        userId = userData.getString("USER_ID", FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    protected void likeProcess(final String eventId, final DynamicData model) {

        if (mProcessLike) {
            final String uid = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getString("USER_ID", userId);
            final Boolean male = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getBoolean("IS_MALE", isMale);
            final Boolean single = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getBoolean("IS_SINGLE", isSingle);
            final Integer age = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE)
                    .getInt("USER_AGE", userAge);
            likeUpdater = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //like già presente
                    if (dataSnapshot.child(eventId).hasChild(uid)) {
                        //vanno rimosse le cose e diminuiti i contatori
                        eventLikeRef.child(eventId).child(uid).removeValue();
                        UbiquoUtils.removePendingNotification(eventId, model.geteName(), uid, model.getDate(),getActivity(),getContext());
                        //rimuove l'id dell'utente dai like dell'utente
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Likes")
                                .child("Events")
                                .child(current_city)
                                .child(eventId)
                                .child(uid).removeValue();
                        //rimuove id evento dai like dell'utente
                        FirebaseDatabase.getInstance().getReference()
                                .child("Likes")
                                .child("Users")
                                .child(userId)
                                .child(eventId)
                                .removeValue();


                        //aggiona i like della mappa
                        FirebaseDatabase.getInstance().getReference()
                                .child("MapData")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                MapInfo map = mutableData.getValue(MapInfo.class);
                                if (map == null) {
                                    return Transaction.success(mutableData);
                                }
                                map.setLikes(map.getLikes() - 1);
                                map.setTotalAge(map.getTotalAge()-age);
                                mutableData.setValue(map);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                //nientedi speciale
                            }
                        });

                        FirebaseDatabase.getInstance().getReference()
                                .child("Events")
                                .child("Dynamic")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                DynamicData data = mutableData.getValue(DynamicData.class);
                                if (data == null) {
                                    return Transaction.success(mutableData);
                                }
                                //update incondizionali
                                data.setLike(data.getLike()-1);
                                data.setnLike(data.getnLike() + 1);
                                data.setAge(data.getAge() - age);

                                //se è uomo
                                if (male) {
                                    data.setMaLike(data.getMaLike() - 1);
                                    if (single) {
                                        data.setsLike(data.getsLike() - 1);
                                    } else {
                                        data.seteLike(data.geteLike() - 1);
                                    }
                                }
                                //se è donna
                                else {
                                    data.setfLike(data.getfLike() - 1);
                                    if (single) {
                                        data.setsLike(data.getsLike() - 1);
                                    } else {
                                        data.seteLike(data.geteLike() - 1);
                                    }
                                }

                                mutableData.setValue(data);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                removeLikeListener(eventLikeRef, likeUpdater);
                                snackBar.setText("Evento rimosso dai preferiti");
                                snackBar.show();
                                mProcessLike = false;
                            }
                        });
                        mProcessLike = false;
                    } else {
                        eventLikeRef.child(eventId).child(uid).setValue(true);
                        UbiquoUtils.setPendingNotification(eventId, model.geteName(), uid, model.getDate(), model.getiPath(),getActivity(),getContext());
                        //aggiunge id utente ai like dell'evento
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Likes")
                                .child("Events")
                                .child(current_city)
                                .child(eventId)
                                .child(uid).setValue(true);
                        //aggiunge id evento ai like dell'utente
                        FirebaseDatabase.getInstance().getReference()
                                .child("Likes")
                                .child("Users")
                                .child(userId)
                                .child(eventId)
                                .setValue(true);

                        FirebaseDatabase.getInstance().getReference()
                                .child("MapData")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                MapInfo map = mutableData.getValue(MapInfo.class);
                                if (map == null) {
                                    return Transaction.success(mutableData);
                                }
                                map.setLikes(map.getLikes() + 1);
                                map.setTotalAge(map.getTotalAge()+age);
                                mutableData.setValue(map);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                //nientedi speciale
                            }
                        });

                        //Transaction primaria
                        FirebaseDatabase.getInstance().getReference()
                                .child("Events")
                                .child("Dynamic")
                                .child(current_city)
                                .child(eventId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                DynamicData data = mutableData.getValue(DynamicData.class);
                                if (data == null) {
                                    return Transaction.success(mutableData);
                                }
                                //update incondizionali
                                data.setLike(data.getLike() + 1);
                                data.setnLike(data.getnLike() - 1);
                                data.setAge(data.getAge() + age);

                                //se è uomo
                                if (male) {
                                    data.setMaLike(data.getMaLike() + 1);
                                    if (single) {
                                        data.setsLike(data.getsLike() + 1);
                                    } else {
                                        data.seteLike(data.geteLike() + 1);
                                    }
                                }
                                //se è donna
                                else {
                                    data.setfLike(data.getfLike() + 1);
                                    if (single) {
                                        data.setsLike(data.getsLike() + 1);
                                    } else {
                                        data.seteLike(data.geteLike() + 1);
                                    }
                                }

                                mutableData.setValue(data);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                removeLikeListener(eventLikeRef, likeUpdater);
                                snackBar.setText("Evento aggiunto ai preferiti");
                                snackBar.show();
                                mProcessLike = false;
                            }
                        });


                        mProcessLike = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            eventLikeRef.addListenerForSingleValueEvent(likeUpdater);
        }
    }

    private void setupFences() {

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        AwarenessFence locationFence = LocationFence.in(41.595795, 14.230733, 5000, 5000);

            // Register the fence to receive callbacks.
            Awareness.FenceApi.updateFences(
                    mApiClient,
                    new FenceUpdateRequest.Builder()
                            .addFence(FENCE_KEY, locationFence, myPendingIntent)
                            .build())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if(status.isSuccess()) {
                                Log.i("Fence Status", "Fence was successfully registered.");
                            } else {
                                Log.e("Fence Status", "Fence could not be registered: " + status);
                            }
                        }
                    });
            return;
        }else{
            AwarenessFence locationFence = LocationFence.in(41.595795, 14.230733, 5000, 5000);
            // Register the fence to receive callbacks.
            Awareness.FenceApi.updateFences(
                    mApiClient,
                    new FenceUpdateRequest.Builder()
                            .addFence(FENCE_KEY, locationFence, myPendingIntent)
                            .build())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if(status.isSuccess()) {
                                Log.i("Fence Status", "Fence was successfully registered.");
                            } else {
                                Log.e("Fence Status", "Fence could not be registered: " + status);
                            }
                        }
                    });
        }

    }

    private FirebaseRecyclerAdapter<DynamicData, MyEventViewHolder> newAdapter(){
        FirebaseRecyclerAdapter<DynamicData, MyEventViewHolder> eventAdapter = new FirebaseRecyclerAdapter<DynamicData, MyEventViewHolder>(
                DynamicData.class,
                R.layout.event_card,
                MyEventViewHolder.class,
                dynamicEvents.orderByChild(my_ordering[selector])
        ) {
            @Override
            protected void populateViewHolder(final MyEventViewHolder viewHolder, final DynamicData model, int position) {
                ((MainUserPage) getActivity()).viewHolder = viewHolder;

                final String post_key = getRef(position).getKey();


                viewHolder.setEventName(model.geteName());
                viewHolder.setEventImage(getActivity(), model.getiPath());
                viewHolder.revealFabInfo(UbiquoUtils.computeMiddleAge(model.getLike(), model.getAge()),  //etàmedia
                        model.getLike(),                  //like totali
                        model.getMaLike(),                //like maschili
                        model.getfLike());                //like femminili
                viewHolder.setCardDate(UbiquoUtils.fromMillisToStringDate(model.getDate()));
                viewHolder.setCardTime(UbiquoUtils.fromMillisToStringTime(model.getDate()));
                viewHolder.setCardPrice(model.getPrice(), model.getFree());
                viewHolder.setPlaceName(model.getpName());

                ValueEventListener likeCheckerListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (currentUser.getUid() != null) {
                            if (dataSnapshot.child(post_key).hasChild(currentUser.getUid())) {
                                viewHolder.setThumbDown();
                                mDatabaseLike.removeEventListener(this);
                            } else {
                                viewHolder.setThumbUp();
                                mDatabaseLike.removeEventListener(this);
                            }
                            mDatabaseLike.removeEventListener(this);
                        } else {
                            Toasty.error(getActivity(), "Errore di connessione, gli eventi già preferiti potrebbero non essere correttamente segnati", Toast.LENGTH_SHORT, true).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mDatabaseLike.addListenerForSingleValueEvent(likeCheckerListener);

                viewHolder.chiudi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewHolder.mFABRevealLayout.revealMainView();
                    }
                });

                //Onclick per il view generalizzato
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent goToEventPage = new Intent(getContext(), EventPage.class);
                        goToEventPage.putExtra("EVENT_ID", post_key);
                        goToEventPage.putExtra("USER_ID", userId);
                        goToEventPage.putExtra("USER_ISMALE", isMale);
                        goToEventPage.putExtra("USER_ISSINGLE", isSingle);
                        goToEventPage.putExtra("USER_AGE", userAge);

                        startActivity(goToEventPage);
                    }
                });// END onclick view generalizzato

                viewHolder.fabLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;
                        likeProcess(post_key, model);
                    }
                });


            }
        };
        return eventAdapter;

    }


    protected void showTutorial(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        config.setShapePadding(12);
        config.setMaskColor(Color.parseColor("#512DA8"));



        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity());
        sequence.setConfig(config);
        sequence.singleUse("intro");

        //su alcuni device il recyclerView non è pronto a questo punto
        if(recyclerView != null) {
            sequence.addSequenceItem(new MaterialShowcaseView.Builder(getActivity())
                    .setTarget(recyclerView)
                    .setDismissText("OK")
                    .setTitleText("Eventi")
                    .setContentText("Quando un evento ti interessa puoi aggiungerlo ai preferiti premendo sulla stellina. Per leggere le informazioni in dettaglio, premi sull'immagine")
                    .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                    .setMaskColour(Color.parseColor("#512DA8"))
                    .build());
        }



        sequence
                .addSequenceItem(((MainUserPage)getActivity()).userDateButton,
                        "Aree","Puoi cambiare area geografica con questo pulsante","Ok");
        sequence
                .addSequenceItem(((ViewGroup)((MainUserPage)getActivity()).tabs.getChildAt(0)).getChildAt(1),
                        "Proposte","Proponi e vota le proposte per i nuovi eventi, le più popolari potranno essere organizzate dai locali","Ho capito !");
        sequence
                .addSequenceItem(((ViewGroup)((MainUserPage)getActivity()).tabs.getChildAt(0)).getChildAt(2),
                        "Mappa","Utilizza la mappa per visualizzare gli eventi in zona","Ok !");

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(getActivity())
                .setTarget(((MainUserPage)getActivity()).collapseProfile)
                .setDismissText("Ho capito !")
                .setTitleText("Social")
                .setContentText("Gestisci il tuo profilo, segui i tuoi amici e condividi con loro ciò che ti interessa attraverso il following !")
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .setShapePadding(80)// provide a unique ID used to ensure it is only shown once
                .setMaskColour(Color.parseColor("#512DA8"))
                .build());


        sequence.start();
    }


}
