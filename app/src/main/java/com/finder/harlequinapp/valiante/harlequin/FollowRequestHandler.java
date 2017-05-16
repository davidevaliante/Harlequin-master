package com.finder.harlequinapp.valiante.harlequin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by akain on 15/05/2017.
 */

public class FollowRequestHandler extends BroadcastReceiver {

    public static final String FOLLOW_HANDLER = "com.finder.harlequinapp.valiante.harlequin.FOLLOW_HANDLER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(FOLLOW_HANDLER)){
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
            DatabaseReference subscribeReference = FirebaseDatabase.getInstance().getReference().child("Subscribes");
            //Scrive nel database la relazione di following
            followersReference.child(receiver_id).child(sender_id).setValue(true);
            followingReference.child(sender_id).child(receiver_id).setValue(true);
            //la richiesta non è più pending e quindi va rimossa
            pendingReference.child(receiver_id).child(sender_id).removeValue();

            //Iscrive l'utente sender al topic dell'utente seguito che ha accettato la richiesta
            DatabaseReference notifyReference = subscribeReference.child(sender_id).child(receiver_id);

            notifyReference.child("topic_id").setValue(receiver_id);
            notifyReference.child("token").setValue(token);
            notifyReference.child("topic_name").setValue(receiver_name);



            //cancella la notifica appena gestita
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notification_id);
            Toast.makeText(context, "Richiesta accettata !", Toast.LENGTH_SHORT).show();
        }


    }
}
