package com.finder.harlequinapp.valiante.harlequin;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.login.LoginManager;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by akain on 16/05/2017.
 */

public class UbiquoUtils {

    private static SharedPreferences userData;

    private UbiquoUtils(){

    };

    /**
     * Manda una notifica di following al requestReceiver attraverso trigger del database
     */
    public static void pendingNotificationTrigger(String requestReceiver, String requestSender, String sendToken, String responseToken){
        DatabaseReference pendingRequest = FirebaseDatabase.getInstance().getReference().child("PendingRequest");
        DatabaseReference userFollowingReference = FirebaseDatabase.getInstance().getReference().child("Following");

            pendingRequest.child(requestReceiver).child(requestSender).child("sendTo_token").setValue(sendToken);
            pendingRequest.child(requestReceiver).child(requestSender).child("respondTo_token").setValue(responseToken);
            userFollowingReference.child(requestSender).child(requestReceiver).setValue(false);

    }

     /**
    *rimuove il following rimuovendo i dati necessari al triggering delle cloud functions
    */
    public static void removeFollowInteractions(String currentUser, String targetUser){
        DatabaseReference pendingRequest = FirebaseDatabase.getInstance().getReference().child("PendingRequest");
        DatabaseReference userFollowingReference = FirebaseDatabase.getInstance().getReference().child("Following");
        DatabaseReference userFollowersReference = FirebaseDatabase.getInstance().getReference().child("Followers");

        //se gia segue si può rimuovere il following ed applicare l'unsbscribe
        pendingRequest.child(targetUser).child(currentUser).removeValue();
        userFollowingReference.child(currentUser).child(targetUser).removeValue();
        userFollowersReference.child(targetUser).child(currentUser).removeValue();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(targetUser);
        FirebaseDatabase.getInstance().getReference().child("Subscribes")
                .child(currentUser)
                .child(targetUser).removeValue();
    }

    public static void refreshCurrentUserToken(Context context){
        userData = context.getSharedPreferences("HARLEE_USER_DATA",Context.MODE_PRIVATE);
        String token = userData.getString("USER_TOKEN","nope");
        FirebaseDatabase.getInstance().getReference().child("Token")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("user_token").setValue(token);

    }

    /**
     * Effettua il logut sia da Firebase che da Facebook e rimanda alla pagina iniziale
     * NOTA: bisogno ancora richiamare finish() immediatamente dopo per evitare
     */
    public void logOut(Context context){
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

    //da millisecondi a data
    public static String fromMillisToStringDate(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
        String[] splittedDate = format.format(date).split("/");
        return splittedDate[0] + " " + splittedDate[1];
    }

    //da millisecondi ad orario
    public static String fromMillisToStringTime(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    //imposta la notifica attraverso un delay
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

    //rimuove la notifica
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


}
