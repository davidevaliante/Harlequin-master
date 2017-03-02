package com.finder.harlequinapp.valiante.harlequin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;


public class AlarmReceiver extends BroadcastReceiver {
    private NotificationTarget notificationTarget;


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, EventPage.class);
        notificationIntent.putExtra("EVENT_ID", intent.getStringExtra("EVENT_ID"));

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(NotificationActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        long[] pattern = {500,200,100,200};

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.remoteview_notification);
        rv.setImageViewResource(R.id.rv_image,R.drawable.accent_clock_14);
        rv.setTextViewText(R.id.rv_title,intent.getCharSequenceExtra("EVENT_NAME"));


        PendingIntent pendingIntent = stackBuilder.getPendingIntent(intent.getIntExtra("INTENT_ID",0), PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                                                   .setSmallIcon(R.drawable.logo)
                                                                   .setVibrate(pattern);


        Notification notification = builder.setContentTitle(intent.getCharSequenceExtra("EVENT_NAME"))
                                           .setContentText("Sta per iniziare! Guarda i dati dell'evento")
                                           .setTicker("New Message Alert!")
                                           .setContentIntent(pendingIntent).build();


        // set big content view for newer androids
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            Integer NOTIFICATION_ID = intent.getIntExtra("INTENT_ID",0);
           Notification cool = builder.setCustomBigContentView(rv).setContentIntent(pendingIntent).build();

            notificationTarget = new NotificationTarget(
                    context,
                    rv,
                    R.id.rv_image,
                    cool,
                    NOTIFICATION_ID);

            Glide
                    .with( context.getApplicationContext() ) // safer!
                    .load( intent.getStringExtra("IMAGE_PATH"))
                    .asBitmap()
                    .into( notificationTarget );



            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, cool);

        }
            else {
            notification.sound = alarmSound;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(intent.getIntExtra("INTENT_ID", 0), notification);
        }
    }
}
