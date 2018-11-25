package edu.psu.slparker.assignment_maps_samanthaparker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

public class MapBroadcastReceiver extends BroadcastReceiver
{
    private Integer CHANNEL_ID = 1;
    private String CHANNEL_NAME = "Maps";
    private String CHANNEL_DESCRIPTION = "Map Notifications";
    private static final String TAG = "MapBroadcastReceiver";

    public static final String NEW_MAP_BROADCAST = "edu.psu.slparker.assignment_maps_samanthaparker.action.NEW_MAP_BROADCAST";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Get data from intent
        Double latitude = intent.getDoubleExtra("LATITUDE", 0);
        Double longitude = intent.getDoubleExtra("LONGITUDE", 0);
        String location = intent.getStringExtra("LOCATION");
        String description = intent.getStringExtra("DESCRIPTION");
        Integer channel_id = intent.getIntExtra("ID", 0);

        Log.d(TAG, "here");
        //instantiate notification manager
        NotificationManager notificationManager =  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder =  new Notification.Builder(context, CHANNEL_NAME).setSmallIcon(R.drawable.mapmarker).setContentTitle(location).setContentText("New location located here: " + latitude + ", " + longitude);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_NAME, CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setShowBadge(true);
        notificationChannel.setDescription(CHANNEL_DESCRIPTION);
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);

        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(channel_id, builder.build());
        Log.d(TAG, "Notification id: " + channel_id + "for this loc: " + location);

    }
}
