package com.app.rakubaru.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.app.rakubaru.R;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.main.HomeActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ForegroundService extends LifecycleService {
    Timer timer = null;
    public static int i = 0;

    private final String CHANNEL_ID = "EvergoalChannel";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannelIfNeeded();

        /// scheduler a new timer to start changing the contact event numbers
        if (timer != null) timer.cancel();
        timer = new Timer();
        Commons.homeActivity.startedTime = new Date().getTime();
        Commons.homeActivity.clearPolylines();
        Commons.homeActivity.totalDistance = 0;
        i = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                i++;
                Log.d("i+++", String.valueOf(i));
//                Commons.homeActivity.drawRoute(i);
//                Commons.homeActivity.drawRoute(Commons.homeActivity.myLatLng);

            }
            }, 0, 500);

        UUID newContactEventUUID = UUID.randomUUID();

        //// write date in DB

        return START_STICKY;
    }

    public void sendMessage(String title, String message){
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification =  new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setFullScreenIntent(pendingIntent, true)
                .build();
        startForeground(6, notification);
    }

    private void sendCancelableNotification(String title, String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "fcm_default_channel";//getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)//drawable.splash)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setFullScreenIntent(pendingIntent, true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        if(timer != null){
            timer.cancel();
            timer.purge();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}






























