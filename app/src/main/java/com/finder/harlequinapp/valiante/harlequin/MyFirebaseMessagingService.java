package com.finder.harlequinapp.valiante.harlequin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.R.attr.author;
import static android.R.attr.data;

/**
 * Created by akain on 12/05/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationTarget senderImageView,notificationTitle;
    private final static String acceptMessage = "requestAccepted";
    private Integer pendingRequestCount = 0;

    //gestisce e smista tutti i messaggi ricevuti
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            if(Integer.valueOf(remoteMessage.getData().get("my_message_id"))==1) {
                showNotification(remoteMessage.getData().get("sender"), remoteMessage.getData().get("receiver"));
            }
            if(Integer.valueOf(remoteMessage.getData().get("my_message_id"))==2){
                UbiquoUtils.notifySubscribers(remoteMessage.getData().get("liker"),remoteMessage.getData().get("liked_event_id"),getApplication());
            }

            //all'arrivo della notifica costruisce la logica per mostrare la richiesta pendente e l'azione da eseguire
            if(Integer.valueOf(remoteMessage.getData().get("my_message_id"))==0){
                UbiquoUtils.showPendingRequest(remoteMessage.getData().get("request_sender"),remoteMessage.getData().get("request_receiver")
                ,remoteMessage.getData().get("token_sender"),getApplication());
            }
            if(Integer.valueOf(remoteMessage.getData().get("my_message_id"))==99){
                UbiquoUtils.requestAcceptedAndsubscribeToUser(remoteMessage.getData().get("topic"),
                                                  remoteMessage.getData().get("accepter_name"),getApplication());
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

        }
    }



    private void showNotification(String sender, String receiver) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Richiesta da: " + sender)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("per :" + receiver)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void notifySubscribers(String liker_id, final String liked_event_id){

        final DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference().child("Users").child(liker_id);
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String user_name = user.getUserName()+" "+ user.getUserSurname();

                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplication())
                        .setContentTitle(user_name+" parteciperà a questo evento :")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(liked_event_id)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());

                user_reference.removeEventListener(this);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        user_reference.addValueEventListener(userListener);




    }

    private void showPendingRequest(final String sender, final String receiver, final String token, final Context ctx){

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

                //gestione RemoteView personalizzata
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
                pendingNotification.setImageViewResource(R.id.decline_check,R.drawable.red_decline_12);
                Toast.makeText(ctx, sender, Toast.LENGTH_SHORT).show();
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
                pendingNotification.setOnClickPendingIntent(R.id.accept_layout,HandleFollowingProcess);


                /*Codice per l'handling della richiesta rifiutata*/

                /*Codice per l'handling della richiesta ancora in attesa di moderazione*/


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
                }


                senderRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        senderRef.addValueEventListener(senderListener);

    }


    private Bitmap textAsBitmap(Context context, String messageText, float textSize, int textColor,String fontName){

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



}
