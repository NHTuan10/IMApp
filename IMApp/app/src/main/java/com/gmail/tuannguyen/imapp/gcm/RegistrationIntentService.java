package com.gmail.tuannguyen.imapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.connection.Connection;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "GCMRegistrationService";
    private static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i("GCM Token:", token);
            sendRegistrationToServer(token);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
        } catch (IOException e) {
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
            e.printStackTrace();
        }
    }

    /**
     * Send registration token to server
     *
     * @param token
     */
    private void sendRegistrationToServer(String token) {
        RegistrationIQ regIQ = new RegistrationIQ(token);
        regIQ.setTo(getString(R.string.server_address));
        Log.d(TAG, "SEND_IQ" + regIQ.toXML().toString());
        if (null != (Connection.getInstance())) {
            Connection.getInstance().sendPacket(regIQ);
        }
    }

}
