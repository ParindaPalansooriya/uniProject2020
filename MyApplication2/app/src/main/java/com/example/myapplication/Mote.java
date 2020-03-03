package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Mote extends BroadcastReceiver{

    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        Toast.makeText(context, "Alarm worked.", Toast.LENGTH_LONG).show();
        int icon = R.drawable.ic_launcher_background;
        CharSequence tickerText = "Hello you have to take medicine I am Nitin Sharma";
        long when = System.currentTimeMillis();

        //Notification notification = new Notification(icon, tickerText,when );

        CharSequence contentTitle = "My notification";
        CharSequence contentText = "Hello World!";


        //notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        final int NOTIF_ID = 1234;
        NotificationManager notofManager = (NotificationManager)context. getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification note = new Notification(R.drawable.face,"NEW ACTIVITY", System.currentTimeMillis());
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,0, notificationIntent, 0);
        Notification notification = new Notification(icon, tickerText,when );
        //Notification notification1 = new Notification(R.drawable.icon, "Wake up alarm", System.currentTimeMillis());
        //notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notification.flags = Notification.FLAG_INSISTENT;
        notification.defaults |= Notification.DEFAULT_SOUND;
        //notification.setLatestEventInfo(context, "My Activity", "This will runs on button click", contentIntent);
        notofManager.notify(NOTIF_ID,notification);

        //PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        //notification.setLatestEventInfo(context, "Context Title", "Context text", contentIntent);
        //notification.flags = Notification.FLAG_INSISTENT;
    }



}
