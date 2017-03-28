package com.gmail.tuannguyen.imapp.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.chat.ChatMessage;
import com.gmail.tuannguyen.imapp.connection.Connection;
import com.google.gson.Gson;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.gmail.tuannguyen.imapp.util.Common.BROADCAST_CHAT_MESSAGE;
import static com.gmail.tuannguyen.imapp.util.Common.CHAT_MESSAGE;
import static com.gmail.tuannguyen.imapp.util.Common.CHAT_MESSAGE_TYPE;
import static com.gmail.tuannguyen.imapp.util.Common.SECRET_SESSION_PREFIX;

/**
 * Created by tuannguyen on 3/19/16.
 */
public class CommonUtil {
    private static SecureRandom random = new SecureRandom();
    private static SharedPreferences preferences;

    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    public static String getCurrentTime() {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    /**
     * @param datetime
     * @return
     */
    public static String toDateString(Date datetime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //TimeZone timeZone = Calendar.getInstance().getTimeZone();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strDatetime = sdf.format(datetime);
        return strDatetime;
    }

    /**
     * Extract only username@servername from username@server/resource or username@server
     *
     * @param from absoulute ID has from username@server/resource
     * @return
     */
    public static String extractUserNameExcludeResource(String from) {
        return from.split("/")[0];
    }

    /**
     * Extract only username from username@server/resource or username@server
     *
     * @param from absoulute ID has from username@server/resource
     * @return
     */
    public static String extractUserName(String from) {
        if (from == null)
            return null;
        String[] strs = from.split("@");
        String string = "";
        for (int i = 0; i < strs.length - 1; i++) {
            string += strs[i];
        }
        return string;
    }

    /**
     * @param inputStream
     * @return
     */
    public static byte[] readAllBytesFromInputStream(InputStream inputStream) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data;
        try {
            data = new byte[1024 * 1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return
     */
    public static String randomAlphaNumeric() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * @param includeServerName
     * @return
     */
    public static String getAuthenticatedUserName(boolean includeServerName) {
        XMPPTCPConnection xmpptcpConnection = Connection.getInstance().getXMPPTCPConnection();
        if (!includeServerName) {
            return CommonUtil.extractUserName(xmpptcpConnection.getUser());
        } else {
            return extractUserNameExcludeResource(xmpptcpConnection.getUser());
        }
    }

    /**
     * @param text
     * @return
     */
    public static String hash(String text) {
        String hashedText = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = messageDigest.digest(text.getBytes("UTF-8"));
            //Convert array of bytes to hexadecimal
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hashedBytes) {
                stringBuilder.append(String.format("%02X", b));
            }
            hashedText = stringBuilder.toString();

            //hashedText =  Base64.encodeToString(hashedBytes,Base64.DEFAULT);
            //hashedText = hashedText.substring(0,hashedText.length() -2);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hashedText;
    }

    /**
     * @param avatarBytes
     * @param width
     * @param height
     * @return
     */
    public static Bitmap toBitmap(byte[] avatarBytes, int width, int height) {
        Bitmap avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
        return Bitmap.createScaledBitmap(avatar, width, height, false);
    }


    /**
     * @param context
     * @param chatMessage
     * @param type
     */
    public static void notifyUiNewMessage(Context context, ChatMessage chatMessage, String type) {
        String jsonChatMessage = (new Gson()).toJson(chatMessage);
        Intent intent = new Intent(BROADCAST_CHAT_MESSAGE);
        intent.putExtra(CHAT_MESSAGE, jsonChatMessage);
        intent.putExtra(CHAT_MESSAGE_TYPE, type);
        context.sendBroadcast(intent);
    }

    public static boolean isSecretSession(Context context, String contact) {
        String userName = CommonUtil.getAuthenticatedUserName(false);
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        String key = getSecretSessionPreferenceKey(userName, contact);
        return preferences.getBoolean(key, false);
    }

    public static boolean isLockScreenEnabled(Context context){
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        boolean lockScreenEnabled = preferences.getBoolean(
                context.getString(R.string.pref_lock_screen),true);

        return lockScreenEnabled;
    }
    public static String getSecretSessionPreferenceKey(String userName, String contact) {
        return SECRET_SESSION_PREFIX + userName + "_" + contact;
    }

    public static void showIncomingMessageNotification(Context context, String from, String message) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("IMAPP New message from " + from)
                .setContentText(message)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationCompatBuilder.build());
    }
}