package com.gmail.tuannguyen.imapp.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class IMInstanceIDListenerService extends InstanceIDListenerService {
    private static final String TAG = "IMInstanceIDListenerService";

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
