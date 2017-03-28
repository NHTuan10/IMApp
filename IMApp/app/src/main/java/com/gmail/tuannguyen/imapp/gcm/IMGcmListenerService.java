package com.gmail.tuannguyen.imapp.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gmail.tuannguyen.imapp.MainActivity;
import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.util.CommonUtil;
import com.google.android.gms.gcm.GcmListenerService;

public class IMGcmListenerService extends GcmListenerService {
    private static final String TAG = "IMGcmListenerService";

    /**
     * Called when message is received
     *
     * @param from
     * @param data
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String fromUser = CommonUtil.extractUserName(data.getString("source"));
        Log.d("From", fromUser);
        Log.d("Message", message);
        showNotification(fromUser, message);
    }

    /**
     * Show notification containing the message to user
     *
     * @param message
     */
    public void showNotification(String from, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        //        PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Message from " + from)
                .setContentText(message)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationCompatBuilder.build());
    }
}
