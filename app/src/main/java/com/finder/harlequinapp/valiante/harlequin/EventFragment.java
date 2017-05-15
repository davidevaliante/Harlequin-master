package com.finder.harlequinapp.valiante.harlequin;

import android.app.AlarmManager;
import android.app.PendingIntent;
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

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class EventFragment extends Fragment {

    protected String[] my_ordering = {"date", "nLike", "eName"};
    private DatabaseReference myDatabase, mDatabaseLike, mDatabaseFavourites, eventLikeRef, dynamicEvents;
    private FirebaseUser currentUser;
    protected Integer orderingSelector = 1;
    protected FirebaseRecyclerAdapter<DynamicData, MyEventViewHolder> eventAdapter;
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

    protected String current_city = "Isernia";
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
    protected RecyclerView.ViewHolder myViewHolder;

    ViewTreeObserver.OnGlobalLayoutListener rvListener;



    protected LinearLayoutManager linearLayoutManager;

    private final String BUNDLE_RECYCLER_LAYOUT = "RECYCLERVIEW_STATE";


    //TODO pulsante like spammabile , spostare l'mProcess like nell' OnComplete della transaction


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.event_fragment_layout, container, false);



        //carica i dati dell'utente
        getUserData();
        userData = getActivity().getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE);
        snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), "LUL", Snackbar.LENGTH_SHORT);
        //per cambiare il background della snackbar
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryDark));
        editor = userData.edit();
        spinner = (CustomSpinner)getActivity().findViewById(R.id.spinner);

        //dichiarazione googleApiClient
        /*
        Context context = getActivity();
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .enableAutoManage(getActivity(), 1, null)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        FENCE_RECEIVER_ACTION =String.valueOf(Calendar.getInstance().get(Calendar.SECOND));
                        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
                        myPendingIntent =
                                PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

                        // The broadcast receiver that will receive intents when a fence is triggered.
                        myFenceReceiver = new FenceReceiver();
                        getActivity().registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
                        setupFences();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();


        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            AwarenessFence locationFence = LocationFence.entering(41.595820, 14.230730, 50);
        }

        */
        //UI
        eventLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child("Isernia");
        eventLikeRef.keepSynced(true);
        myDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes").child("Events").child(current_city);
        mDatabaseFavourites = FirebaseDatabase.getInstance().getReference().child("favList");
        dynamicEvents = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(citySelector);
        dynamicEvents.keepSynced(true);
        myDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //recyclerview
        linearLayoutManager = new LinearLayoutManager((getActivity()));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        //evita che la cardView "blinki"quando viene premuto il like
        recyclerView.getItemAnimator().setChangeDuration(0);




        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 1:
                        selector=0;
                        eventAdapter.cleanup();
                        recyclerView.setAdapter(null);
                        onStart();
                        break;
                    case 2:
                        selector=1;
                        eventAdapter.cleanup();
                        recyclerView.setAdapter(null);
                        onStart();
                        break;
                    case 3:
                        selector=2;
                        eventAdapter.cleanup();
                        recyclerView.setAdapter(null);
                        onStart();
                        break;

                    default:
                        selector=0;
                        eventAdapter.cleanup();
                        recyclerView.setAdapter(null);
                        onStart();
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        return recyclerView;
    }

    //contiene RecyclerView
    public void onStart() {
        super.onStart();

        if (recyclerView.getAdapter() == null) {

            eventAdapter = new FirebaseRecyclerAdapter<DynamicData, MyEventViewHolder>(
                    DynamicData.class,
                    R.layout.event_card,
                    MyEventViewHolder.class,
                    dynamicEvents.orderByChild(my_ordering[selector])
            ) {
                @Override
                protected void populateViewHolder(final MyEventViewHolder viewHolder, final DynamicData model, int position) {
                    ((MainUserPage)getActivity()).viewHolder = viewHolder;

                    final String post_key = getRef(position).getKey();


                    viewHolder.setEventName(model.geteName());
                    viewHolder.setEventImage(getActivity(), model.getiPath());
                    viewHolder.revealFabInfo(computeMiddleAge(model.getLike(), model.getAge()),  //etàmedia
                                                              model.getLike(),                  //like totali
                                                              model.getMaLike(),                //like maschili
                                                              model.getfLike());                //like femminili
                    viewHolder.setCardDate(fromMillisToStringDate(model.getDate()));
                    viewHolder.setCardTime(fromMillisToStringTime(model.getDate()));
                    viewHolder.setCardPrice(model.getPrice(), model.getFree());
                    viewHolder.setPlaceName(model.getpName());

                    ValueEventListener likeCheckerListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(post_key).hasChild(currentUser.getUid())) {
                                viewHolder.setThumbDown();
                                mDatabaseLike.removeEventListener(this);
                            } else {
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
                            goToEventPage.putExtra("USER_ISMALE",isMale);
                            goToEventPage.putExtra("USER_ISSINGLE",isSingle);
                            goToEventPage.putExtra("USER_AGE",userAge);

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
        }
        recyclerView.setAdapter(eventAdapter);
        if (rcState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(rcState);
        }




    }// END OnStart




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.setAdapter(eventAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        //recyclerView.setAdapter(null);
        //eventAdapter.cleanup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        eventAdapter.cleanup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        eventAdapter.cleanup();
        recyclerView.setAdapter(null);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logOutButton:
                logOut();
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
                recyclerView.setAdapter(null);
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

    //costruisce un identificativo unico da passare come Id all'alarm manager
    protected int buildAlarmId(String eventName, String creatorName, String eventDate, String eventTime) {
        int uniqueId = 0;
        int dateDifference = (int) getDateDifference(eventDate, eventTime);
        try {
            if (!eventName.isEmpty() && !creatorName.isEmpty()) {
                int nameLength = eventName.length();
                int creatorNameLength = creatorName.length();
                uniqueId = dateDifference + creatorNameLength + nameLength;
                Log.d("UniqueId = ", "" + uniqueId);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return uniqueId;
        }
    }

    //restituisce la data in formato millisecondi meno un ora
    protected long getDateDifference(String targetDate, String eventTime) {
        //il tempo da sottrarre rispetto all'inizio dell'evento in millisecondi
        long oneHourInMilliseconds = TimeUnit.HOURS.toMillis(1);
        Log.d("HourConversion", "1 hour = " + oneHourInMilliseconds);
        long timeInMilliseconds = 0;
        eventTime = eventTime + ":00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date endDate = dateFormat.parse(targetDate + " " + eventTime);
            Log.d("END_TIME**", "time" + endDate.getTime());
            timeInMilliseconds = endDate.getTime() - oneHourInMilliseconds;
            return timeInMilliseconds;
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            return timeInMilliseconds;
        }
    }

    protected void logOut() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Intent startingPage = new Intent(getActivity(), MainActivity.class);
        startActivity(startingPage);
        getActivity().finish();
    }

    protected Integer computeMiddleAge(Integer likes, Integer totalage) {
        int middleAge;
        if (likes == 0) {
            middleAge = totalage;
            return middleAge;
        } else {
            middleAge = (int) totalage / likes;
            return middleAge;
        }
    }

    //rende la data in formato numero + MeseInTreCaratteri
    protected String readableDate(String eventDate) {
        String[] splittedDate = eventDate.split("/");
        String eventDay = splittedDate[0];
        String eventMonth = new DateFormatSymbols().getMonths()[Integer.parseInt(splittedDate[1]) - 1];
        String date = eventDay + " " + eventMonth;
        return date;

    }


    //da millisecondi a data
    protected String fromMillisToStringDate(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
        String[] splittedDate = format.format(date).split("/");
        return splittedDate[0] + " " + splittedDate[1];
    }

    //da millisecondi ad orario
    protected String fromMillisToStringTime(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }


    protected void getUserData() {
        userData = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        userName = userData.getString("USER_NAME", "Name error");
        userAge = userData.getInt("USER_AGE", 25);
        isSingle = userData.getBoolean("IS_SINGLE", true);
        isMale = userData.getBoolean("IS_MALE", true);
        userId = userData.getString("USER_ID", "nope");


    }


    //imposta la notifica attraverso un delay
    protected void setPendingNotification(String eventId, String eventName, String userId, Long eventDate, String path) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID", eventId);
        notificationIntent.putExtra("INTENT_ID", alarmId(eventName, userId, eventDate));
        notificationIntent.putExtra("IMAGE_PATH", path);
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(getContext(), alarmId(eventName, userId, eventDate),
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, oneHourDifference(eventDate), broadcast);
    }

    //rimuove la notifica
    protected void removePendingNotification(String eventId, String eventName, String userId, Long eventDate) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID", eventId);
        notificationIntent.putExtra("INTENT_ID", alarmId(eventName, userId, eventDate));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(getContext(), alarmId(eventName, userId, eventDate),
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(broadcast);
    }

    protected int alarmId(String eventName, String userId, Long eventDate) {

        //TODO da migliorare
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(eventDate);
        int uniqueId = 0;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        try {
            if (!eventName.isEmpty() && !userId.isEmpty()) {

                int nameLength = eventName.length();
                int creatorNameLength = userId.length();
                if (nameLength % 2 == 0) {
                    uniqueId = nameLength + creatorNameLength + day + hour + minute;
                    Log.d("UniqueId = ", "" + uniqueId);
                } else {
                    uniqueId = nameLength * creatorNameLength + day + hour + minute;
                    Log.d("UniqueId = ", "" + uniqueId);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return uniqueId;
        }
    }

    protected Long oneHourDifference(Long eventDate) {
        return eventDate - TimeUnit.HOURS.toMillis(1);
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
                        removePendingNotification(eventId, model.geteName(), uid, model.getDate());
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
                        setPendingNotification(eventId, model.geteName(), uid, model.getDate(), model.getiPath());
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

    /*
    public class FenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.equals(FENCE_RECEIVER_ACTION, intent.getAction())) {
                Toast.makeText(getActivity(), "Problema con il codice du risposta", Toast.LENGTH_SHORT).show();
                return;
            }

            // The state information for the given fence is em
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        fenceStateStr = "true";
                        Toast.makeText(getActivity(), "Vero !", Toast.LENGTH_SHORT).show();
                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "false";
                        Toast.makeText(getActivity(), "Falso !", Toast.LENGTH_SHORT).show();
                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "unknown";
                        Toast.makeText(getActivity(), "Unknown !", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        fenceStateStr = "unknown value";
                        Toast.makeText(getActivity(), "Unknown !", Toast.LENGTH_SHORT).show();
                }

            }
        }

    }
    */



}
