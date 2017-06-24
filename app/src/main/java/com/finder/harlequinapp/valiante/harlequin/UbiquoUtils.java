package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.facebook.login.LoginManager;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.internal.constants.NotificationChannel;
import com.google.android.gms.games.social.Social;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

/**
 * Created by akain on 16/05/2017.
 */

public class UbiquoUtils {

    protected final static Integer CITY_LEVEL_ZOOM = 12;
    protected final static Integer ADRESS_LEVEL_ZOOM = 18;
    private static SharedPreferences userData;
    private static NotificationTarget senderImageView;
    private static long[] vibrationPatter = {500,200,100,200};


    public UbiquoUtils(){

    };

    /**
     * Manda una notifica di following al requestReceiver attraverso trigger del database
     */
    public static void pendingNotificationTrigger(String senderId, String targetId, PendingFollowingRequest newRequest){
        DatabaseReference pendingRequest = FirebaseDatabase.getInstance().getReference().child("PendingRequest");
        DatabaseReference userFollowingReference = FirebaseDatabase.getInstance().getReference().child("Following");

            pendingRequest.child(targetId).child(senderId).setValue(newRequest);
            userFollowingReference.child(senderId).child(targetId).setValue(false);

    }

    public static void goToProfile(String userId, Boolean ownProfile, Activity activity){
        Intent userProfile = new Intent(activity,UserProfile.class);
        userProfile.putExtra("USER_ID",userId);
        userProfile.putExtra("OWN_PROFILE",ownProfile);
        activity.startActivity(userProfile);
    }

     /**
    *rimuove il following rimuovendo i dati necessari al triggering delle cloud functions
    */
    public static void removeFollowInteractions(String currentUser, String targetUser){
        DatabaseReference pendingRequest = FirebaseDatabase.getInstance().getReference().child("PendingRequest");
        DatabaseReference userFollowingReference = FirebaseDatabase.getInstance().getReference().child("Following");
        DatabaseReference userFollowersReference = FirebaseDatabase.getInstance().getReference().child("Followers");
        DatabaseReference followersToNotify = FirebaseDatabase.getInstance().getReference().child("FollowersToNotify")
                .child(targetUser).child(currentUser);

        //se gia segue si può rimuovere il following ed applicare l'unsbscribe
        pendingRequest.child(targetUser).child(currentUser).removeValue();
        userFollowingReference.child(currentUser).child(targetUser).removeValue();
        userFollowersReference.child(targetUser).child(currentUser).removeValue();
        followersToNotify.removeValue();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(targetUser);
        FirebaseDatabase.getInstance().getReference().child("Subscribes")
                .child(currentUser)
                .child(targetUser).removeValue();
    }

    public static void refreshCurrentUserToken(Context context){

        //se l'auth non è null
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            userData = context.getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE);
            final String userToken = FirebaseInstanceId.getInstance().getToken();
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //solo se userToken esiste ed utente ancora loggato
            if (!userId.isEmpty() && !userToken.isEmpty()) {
                final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(userId);
                DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference().child("Token").child(userId).child("user_token");

                //aggiorna shared preferences
                userData.edit().putString("USER_TOKEN", userToken);
                //aggiorna nodo del database
                tokenReference.setValue(userToken);

                //aggiorna token nel profilo utente
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        user.setUserToken(userToken);
                        userReference.setValue(user);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    /**
     * Effettua il logut sia da Firebase che da Facebook e rimanda alla pagina iniziale
     * NOTA: bisogno ancora richiamare finish() immediatamente dopo per evitare
     */
    public static void logOut(Context context){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Intent startingPage = new Intent(context, MainActivity.class);
        context.startActivity(startingPage);
    }

    /*
    *
    * */
    @NonNull
    public static Integer getAgeIntegerFromString(String birthdate){
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

    /*
     * da millisecondi a data
     * */
    public static String fromMillisToStringDate(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
        String[] splittedDate = format.format(date).split("/");
        return splittedDate[0] + " " + splittedDate[1];
    }

    /*
     * da millisecondi ad orario
     * */
    public static String fromMillisToStringTime(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    /*
     * imposta la notifica attraverso un delay
     * */
    public static void setPendingNotification(String eventId, String eventName, String userId, Long eventDate, String path, Activity activity , Context context) {


        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID", eventId);
        notificationIntent.putExtra("INTENT_ID", alarmId(eventName, userId, eventDate));
        notificationIntent.putExtra("IMAGE_PATH", path);
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(context, alarmId(eventName, userId, eventDate),
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, oneHourDifference(eventDate), broadcast);
    }

    /*
    *   rimuove la notifica
    */
    public static void removePendingNotification(String eventId, String eventName, String userId, Long eventDate, Activity activity, Context context) {
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.putExtra("EVENT_NAME", eventName);
        notificationIntent.putExtra("EVENT_ID", eventId);
        notificationIntent.putExtra("INTENT_ID", alarmId(eventName, userId, eventDate));
        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(context, alarmId(eventName, userId, eventDate),
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(broadcast);
    }

    public static int alarmId(String eventName, String userId, Long eventDate) {

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

    @NonNull
    public static Long oneHourDifference(Long eventDate) {
        return eventDate - TimeUnit.HOURS.toMillis(1);
    }

    /*
    *Restituisce l'età media
     */
    public static Integer computeMiddleAge(Integer likes, Integer totalage) {
        int middleAge;
        if (likes == 0) {
            middleAge = totalage;
            return middleAge;
        } else {
            middleAge = (int) totalage / likes;
            return middleAge;
        }
    }

    public static Bitmap textAsBitmap(Context context, String messageText, float textSize, int textColor,String fontName){

        Typeface font=Typeface.createFromAsset(context.getAssets(),fontName);
        Paint paint=new Paint();
        paint.setTextSize(textSize);
        paint.setTypeface(font);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);

        float baseline=-paint.ascent(); // ascent() is negative
        int width=(int)(paint.measureText(messageText)+0.5f); // round
        int height=(int)(baseline+paint.descent()+0.5f);
        Bitmap image=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(image);
        canvas.drawText(messageText,0,baseline,paint);
        return image;
    }

    /*
    * rende la data in formato numero + MeseInTreCaratteri
    * */
    public static String readableDate(String eventDate) {
        String[] splittedDate = eventDate.split("/");
        String eventDay = splittedDate[0];
        String eventMonth = new DateFormatSymbols().getMonths()[Integer.parseInt(splittedDate[1]) - 1];
        String date = eventDay + " " + eventMonth;
        return date;

    }

    public static void inboxNotification(Context ctx){


        Intent resultIntent = new Intent(ctx, MainUserPage.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent piResult = PendingIntent.getActivity(ctx, 0, resultIntent, 0);

        Notification.Builder builder=new Notification.Builder(ctx)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("contentTitle")
                .setContentText("contentText")
                .setContentIntent(piResult);
    }

    public static void showPendingRequest(final String sender, final String receiver, final String token, final Context ctx){

        final DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference().child("Users").child(sender);
        ValueEventListener senderListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //dati necessari a gestire la richiesta
                String sender_id = sender;
                String receiver_id = receiver;
                //Carica il nome utente dalle sharedPreferences
                SharedPreferences userData = ctx.getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
                String receiver_name = userData.getString("USER_NAME", "Name error");

                long[] pattern = {500,200,100,200};
                int num = (int) System.currentTimeMillis();
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                User sendingUser = dataSnapshot.getValue(User.class);
                String userName = sendingUser.getUserName()+" "+sendingUser.getUserSurname();
                String profileImage = sendingUser.getProfileImage();





               /* //gestione RemoteView personalizzata
                RemoteViews pendingNotification = new RemoteViews(ctx.getPackageName(),R.layout.follow_request_notification);
                pendingNotification.setViewPadding(0,0,0,0,0);
               // pendingNotification.setTextViewText(R.id.pending_username,userName+ " vorrebbe seguirti");

                Bitmap nameBitmap = textAsBitmap(ctx,userName,80, Color.parseColor("#673AB7"),"hero.otf");
                Bitmap followMessage = textAsBitmap(ctx,"vorrebbe seguirti",50,R.color.colorAccent,"hero.otf");
                Bitmap acceptFollowButton = textAsBitmap(ctx,"Accetta",60,Color.parseColor("#336dcc"),"hero.otf");
                Bitmap declineFollowButton = textAsBitmap(ctx,"Rifiuta",60,Color.parseColor("#f44842"),"hero.otf");

                pendingNotification.setImageViewBitmap(R.id.pending_name, nameBitmap);
                pendingNotification.setImageViewBitmap(R.id.follow_message,followMessage);
                pendingNotification.setImageViewBitmap(R.id.accept_follow_button,acceptFollowButton);
                pendingNotification.setImageViewBitmap(R.id.decline_follow_button,declineFollowButton);
                pendingNotification.setImageViewResource(R.id.follow_icon,R.drawable.follow_icon_colorprimary_20);
                pendingNotification.setImageViewResource(R.id.accept_check,R.drawable.matte_blue_checked_12);
                pendingNotification.setImageViewResource(R.id.decline_check,R.drawable.red_decline_12);*/

                /*Codice per l'handling della richiesta accettata*/
                //Intent per eseguire codice senza aprire l'app
                Intent HandleAcceptRequest = new Intent(ctx,FollowRequestHandler.class);
                //extra che permette di avere un ID di riferimento alla notifica da cancellare
                HandleAcceptRequest.putExtra("NOTIFICATION_ID",num);
                //extras per scrivere correttamente nel database
                HandleAcceptRequest.putExtra("SENDER_ID",sender);
                HandleAcceptRequest.putExtra("RECEIVER_ID",receiver);
                HandleAcceptRequest.putExtra("SENDER_TOKEN",token);
                HandleAcceptRequest.putExtra("RECEIVER_NAME",receiver_name);
                //questo intent ha un azione custom specificata nel follow RequestHandler
                HandleAcceptRequest.setAction(FollowRequestHandler.FOLLOW_HANDLER_ACCEPT);
                PendingIntent HandleFollowingProcess = PendingIntent.getBroadcast(ctx,num,HandleAcceptRequest,PendingIntent.FLAG_UPDATE_CURRENT);
                //OnClick Listener che fa partire la nostra action personalizzata
                //pendingNotification.setOnClickPendingIntent(R.id.accept_layout,HandleFollowingProcess);


                //Handler per la richiesta rifiutata
                Intent HandleDeclineRequest = new Intent(ctx,FollowRequestHandler.class);
                HandleDeclineRequest.putExtra("NOTIFICATION_ID",num);
                HandleDeclineRequest.putExtra("SENDER_ID",sender);
                HandleDeclineRequest.putExtra("RECEIVER_ID",receiver);
                HandleDeclineRequest.putExtra("SENDER_TOKEN",token);
                HandleDeclineRequest.putExtra("RECEIVER_NAME",receiver_name);
                HandleDeclineRequest.setAction(FollowRequestHandler.FOLLOW_HANDLER_DECLINE);
                PendingIntent HandleDeclineProcess = PendingIntent.getBroadcast(ctx,num,HandleDeclineRequest,PendingIntent.FLAG_UPDATE_CURRENT);

                Intent HandleAcceptAndFollowBack = new Intent(ctx,FollowRequestHandler.class);
                HandleAcceptAndFollowBack.putExtra("NOTIFICATION_ID",num);
                HandleAcceptAndFollowBack.putExtra("SENDER_ID",sender);
                HandleAcceptAndFollowBack.putExtra("RECEIVER_ID",receiver);
                HandleAcceptAndFollowBack.putExtra("SENDER_TOKEN",token);
                HandleAcceptAndFollowBack.putExtra("RECEIVER_NAME",receiver_name);
                HandleAcceptAndFollowBack.setAction(FollowRequestHandler.FOLLOW_HANDLER_FOLLOW_BACK);
                PendingIntent HandleFollowbackProcess = PendingIntent.getBroadcast(ctx,num,HandleAcceptAndFollowBack,PendingIntent.FLAG_UPDATE_CURRENT);

                Intent toPendingPage = new Intent(ctx,UserProfile.class);
                toPendingPage.putExtra("NOTIFICATION_ID",num);
                toPendingPage.putExtra("SENDER_ID",sender);
                toPendingPage.putExtra("RECEIVER_ID",receiver);
                toPendingPage.putExtra("SENDER_TOKEN",token);
                toPendingPage.putExtra("RECEIVER_NAME",receiver_name);
                toPendingPage.putExtra("USER_ID",FirebaseAuth.getInstance().getCurrentUser().getUid());
                toPendingPage.putExtra("OWN_PROFILE",true);
                PendingIntent ToPendingPage = PendingIntent.getActivity(ctx,num,toPendingPage,PendingIntent.FLAG_UPDATE_CURRENT);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
                builder.setSmallIcon(R.drawable.logo);
                builder.setAutoCancel(true);
                builder.setContentIntent(ToPendingPage);
                /*try {
                    Bitmap profilePic = Glide.with(ctx).load(profileImage).asBitmap().into(80,80).get();
                    builder.setLargeIcon(profilePic);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }*/
                builder.setVibrate(vibrationPatter);
                //builder.addAction(R.drawable.logo,"Accetta e Segui",HandleFollowbackProcess);
                builder.addAction(R.drawable.logo,"Rifiuta",HandleDeclineProcess);
                builder.addAction(R.drawable.logo,"Accetta",HandleFollowingProcess);
                builder.setContentTitle(userName);
                builder.setContentText("Vorrebbe seguirti !");

                Notification notification = builder.build();
                NotificationManagerCompat.from(ctx).notify(num,notification);



                /*Codice per l'handling della richiesta rifiutata*/

                /*Codice per l'handling della richiesta ancora in attesa di moderazione*/

/*
                //condizionale necessario a targettare l'image per utilizzare glide
                if (android.os.Build.VERSION.SDK_INT >= 16){
                    //inizio boilerPlate custom Notifications
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.follow_request_icon);
                    //TODO Modificare
                    builder.setCustomHeadsUpContentView(pendingNotification);
                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    builder.setVibrate(pattern);
                    builder.setGroup("PENDING_REQUEST");



                    Notification notification = builder
                            .setContentInfo("UbiQuo")
                            .setContentTitle(userName)
                            .setContentText("Vorrebbe seguirti")
                            .setTicker("New Message Alert!").build();

                    Notification cool = builder.setCustomBigContentView(pendingNotification).build();

                    senderImageView = new NotificationTarget(ctx,pendingNotification,
                            R.id.pending_notification_profile,
                            cool,
                            num);
                    Glide.with(ctx) // safer!
                            .load(profileImage)
                            .asBitmap()
                            .into(senderImageView);

                }else{
                    //inizio boilerPlate custom Notifications
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.logo);
                    Notification notification = builder
                            .setContentInfo("Ubiquo")
                            .setContentTitle(userName)
                            .setContentText("Vorrebbe seguirti")
                            .setTicker("New Message Alert!").build();
                    //.setContentIntent(pendingIntent).build();
                    notification.sound = alarmSound;
                    NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(num, notification);
                }*/


                senderRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        senderRef.addValueEventListener(senderListener);

    }

    public static void requestAcceptedAndsubscribeToUser(String userId, String userName, Context ctx){
        int num = (int) System.currentTimeMillis();
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx,num, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx)
                .setContentTitle(userName+" ha accettato la tua richiesta")
                .setSmallIcon(R.drawable.follow_request_icon)
                .setContentText("Riceverai notifiche degli eventi ai quali parteciperà")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(num /* ID of notification */, notificationBuilder.build());


    }

    public static void acceptRequestAction(Intent intent, Context context){
        int num = (int) System.currentTimeMillis();
        //recupera l'id di riferimento per la notifica per poterla cancellare
        Integer notification_id = intent.getIntExtra("NOTIFICATION_ID",0);
        String receiver_id = intent.getStringExtra("RECEIVER_ID");
        String sender_id = intent.getStringExtra("SENDER_ID");
        String token = intent.getStringExtra("SENDER_TOKEN");
        String receiver_name = intent.getStringExtra("RECEIVER_NAME");
        DatabaseReference followersReference = FirebaseDatabase.getInstance().getReference().child("Followers");
        DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference().child("Following");
        DatabaseReference pendingReference = FirebaseDatabase.getInstance().getReference().child("PendingRequest");

        //Scrive nel database la relazione di following
        followersReference.child(receiver_id).child(sender_id).setValue(true);
        followingReference.child(sender_id).child(receiver_id).setValue(true);
        //la richiesta non è più pending e quindi va rimossa
        pendingReference.child(receiver_id).child(sender_id).removeValue();


        //trigger per la funzione che iscrive il sender al topic del ricevitore
        DatabaseReference subscribeReference = FirebaseDatabase.getInstance().getReference().child("Subscribes");
        DatabaseReference notifyReference = subscribeReference.child(sender_id).child(receiver_id);
        DatabaseReference followersToNotify = FirebaseDatabase.getInstance().getReference().child("FollowersToNotify")
                                                                            .child(receiver_id).child(sender_id);


        notifyReference.child("topic_id").setValue(receiver_id);
        notifyReference.child("token").setValue(token);
        notifyReference.child("topic_name").setValue(receiver_name);
        followersToNotify.setValue(token);





        //cancella la notifica appena gestita
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notification_id);
        Toasty.success(context,"Richiesta accettata !",Toast.LENGTH_SHORT,true).show();
    }

    public static void declineRequestAction(Intent intent,Context context){
        DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference().child("PendingRequest");
        Integer notification_id = intent.getIntExtra("NOTIFICATION_ID",0);
        String receiver_id = intent.getStringExtra("RECEIVER_ID");
        String sender_id = intent.getStringExtra("SENDER_ID");
        followingReference.child(receiver_id).child(sender_id).removeValue();

        //cancella la notifica con il corrispondente Id
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notification_id);

    }

    public static void followBackAction(Intent intent,Context context){
        acceptRequestAction(intent,context);
        Integer notification_id = intent.getIntExtra("NOTIFICATION_ID",0);
        String receiver_id = intent.getStringExtra("RECEIVER_ID");
        String sender_id = intent.getStringExtra("SENDER_ID");
        String token = intent.getStringExtra("SENDER_TOKEN");
        String receiver_name = intent.getStringExtra("RECEIVER_NAME");
        FirebaseDatabase.getInstance().getReference().child("Following").child(receiver_id).child(sender_id).setValue(true);
        FirebaseDatabase.getInstance().getReference().child("Followers").child(sender_id).child(receiver_id).setValue(true);
    }

    public static void notifySubscribers(String liker_id, final String liked_event_id, final Application application){

        final DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference().child("Users").child(liker_id);
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int num = (int) System.currentTimeMillis();
                User user = dataSnapshot.getValue(User.class);
                String user_name = user.getUserName()+" "+ user.getUserSurname();

                Intent goToEvent = new Intent(application, EventPage.class);
                goToEvent.putExtra("EVENT_ID", liked_event_id);
                goToEvent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(application,num, goToEvent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(application)
                        .setContentTitle("A "+user_name+" interessa un evento")
                        .setSmallIcon(R.drawable.logo)
                        .setContentText("Visualizza maggiori informazioni")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
                NotificationManager notificationManager =
                        (NotificationManager)application.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(num, notificationBuilder.build());

                user_reference.removeEventListener(this);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        user_reference.addValueEventListener(userListener);
    }

    public static void notifyProposalInterested(Context ctx, String event_id, String organizer_id){
        int num = (int) System.currentTimeMillis();
        Intent goToEvent = new Intent(ctx, EventPage.class);
        goToEvent.putExtra("EVENT_ID", event_id);
        goToEvent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx,num, goToEvent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx)
                .setContentTitle("Una proposta alla quale eri interessato è stata trasformata in evento")
                .setSmallIcon(R.drawable.vector_right_arrow_18)
                .setContentText("Clicca per avere più informazioni")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(num /* ID of notification */, notificationBuilder.build());

    }

    public static void loginCheck(Context ctx){
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent toLogin = new Intent(ctx,MainActivity.class);
            ctx.startActivity(toLogin);

        }
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent toUserPage = new Intent(ctx,MainUserPage.class);
            ctx.startActivity(toUserPage);

        }
    }


    public static void acceptRequestMethod(String sender, String sender_token,String name, Context context){
        int num = (int) System.currentTimeMillis();
        //recupera l'id di riferimento per la notifica per poterla cancellare
        String receiver_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sender_id = sender;
        String token = sender_token;
        String receiver_name = name;
        DatabaseReference followersReference = FirebaseDatabase.getInstance().getReference().child("Followers");
        DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference().child("Following");
        DatabaseReference pendingReference = FirebaseDatabase.getInstance().getReference().child("PendingRequest");

        //Scrive nel database la relazione di following
        followersReference.child(receiver_id).child(sender_id).setValue(true);
        followingReference.child(sender_id).child(receiver_id).setValue(true);
        //la richiesta non è più pending e quindi va rimossa
        pendingReference.child(receiver_id).child(sender_id).removeValue();


        //trigger per la funzione che iscrive il sender al topic del ricevitore
        DatabaseReference subscribeReference = FirebaseDatabase.getInstance().getReference().child("Subscribes");
        DatabaseReference notifyReference = subscribeReference.child(sender_id).child(receiver_id);
        DatabaseReference followersToNotify = FirebaseDatabase.getInstance().getReference().child("FollowersToNotify")
                .child(receiver_id).child(sender_id);


        notifyReference.child("topic_id").setValue(receiver_id);
        notifyReference.child("token").setValue(token);
        notifyReference.child("topic_name").setValue(receiver_name);
        followersToNotify.setValue(token);





        //cancella la notifica appena gestita
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Toasty.success(context,"Richiesta accettata !",Toast.LENGTH_SHORT,true).show();
    }

    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static void printPreferences(SharedPreferences preferences){
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }

    }

    public static ProgressDialog defaultProgressBar(String message, Context ctx){
        ProgressDialog newProgressBar = new ProgressDialog(ctx,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        newProgressBar.setMessage(message);
        return newProgressBar;
    }





}
