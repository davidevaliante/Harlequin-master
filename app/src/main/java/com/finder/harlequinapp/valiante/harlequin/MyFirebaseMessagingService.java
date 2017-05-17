package com.finder.harlequinapp.valiante.harlequin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

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
                notifySubscribers(remoteMessage.getData().get("liker"),remoteMessage.getData().get("liked_event_id"));
            }
            if(Integer.valueOf(remoteMessage.getData().get("my_message_id"))==0){
                UbiquoUtils.showPendingRequest(remoteMessage.getData().get("request_sender"),remoteMessage.getData().get("request_receiver")
                ,remoteMessage.getData().get("respond_token"),getApplication());
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





}
