package com.finder.harlequinapp.valiante.harlequin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;



public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, EventPage.class);
        notificationIntent.putExtra("EVENT_ID", intent.getStringExtra("EVENT_ID"));

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(NotificationActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        long[] pattern = {500,200,100,200};


        PendingIntent pendingIntent = stackBuilder.getPendingIntent(intent.getIntExtra("INTENT_ID",0), PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                                                   .setSmallIcon(R.drawable.logo)
                                                                   .setVibrate(pattern);

        Notification notification = builder.setContentTitle(intent.getCharSequenceExtra("EVENT_NAME"))
                                           .setContentText("Sta per iniziare! Guarda i dati dell'evento")
                                           .setTicker("New Message Alert!")
                                           .setContentIntent(pendingIntent).build();

        notification.sound = alarmSound;


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(intent.getIntExtra("INTENT_ID",0), notification);
    }
}
