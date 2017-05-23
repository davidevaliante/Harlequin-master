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

import es.dmoral.toasty.Toasty;

/**
 * Created by akain on 15/05/2017.
 */

public class FollowRequestHandler extends BroadcastReceiver {

    public static final String FOLLOW_HANDLER_ACCEPT = "com.finder.harlequinapp.valiante.harlequin.FOLLOW_HANDLER_ACCEPT";
    public static final String FOLLOW_HANDLER_DECLINE = "com.finder.harlequinapp.valiante.harlequin.FOLLOW_HANDLER_DECLINE";
    public static final String FOLLOW_HANDLER_FOLLOW_BACK = "com.finder.harlequinapp.valiante.harlequin.FOLLOW_HANDLER_FOLLOW_BACK";



    @Override
    public void onReceive(Context context, Intent intent) {

        //richiesta accettata
        if(intent.getAction().equalsIgnoreCase(FOLLOW_HANDLER_ACCEPT)){
            UbiquoUtils.acceptRequestAction(intent,context);
        }
        //richiesta rifiutata
        if(intent.getAction().equalsIgnoreCase(FOLLOW_HANDLER_DECLINE)){
            UbiquoUtils.declineRequestAction(intent,context);
        }

        if(intent.getAction().equalsIgnoreCase(FOLLOW_HANDLER_FOLLOW_BACK)){
            UbiquoUtils.followBackAction(intent,context);
        }


    }
}
