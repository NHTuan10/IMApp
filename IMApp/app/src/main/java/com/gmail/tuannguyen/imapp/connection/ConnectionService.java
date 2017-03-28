package com.gmail.tuannguyen.imapp.connection;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.gmail.tuannguyen.imapp.LoginActivity;
import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.security.AccountAuthenticator;
import com.gmail.tuannguyen.imapp.security.Accounts;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_PASSWORD;

public class ConnectionService extends Service {
    private Connection connection = null;
    private final IBinder binder = new LocalBinder();
    private AccountAuthenticator accountAuthenticator;
    private static final String TAG = "ConnectionService";

    public ConnectionService() {
    }

    public class LocalBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.connectServer();
        //accountAuthenticator = new AccountAuthenticator(this);
        this.authenticate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //connection.logOut();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
        //return accountAuthenticator.getIBinder();
    }

    public void connectServer() {
        String serverAddress = getString(R.string.server_address);
        int serverPort = Integer.parseInt(getString(R.string.port));
        connection = Connection.getInstance(
                getApplicationContext(), serverAddress, serverPort);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!connection.connect(TAG)) {
                    Log.w(TAG, "Cannot connect to server " +
                            getString(R.string.server_address) + ":" + getString(R.string.port));
                    /*
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Connection Error")
                            .setMessage("Cannot connect to server")
                            .setCancelable(true)
                            .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    connectServer();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                            */
                }
                return null;
            }
        }.execute();
    }

    public void authenticate() {
        Bundle accountBundle = Accounts.getAccount(getApplicationContext());
        if (accountBundle != null) {
            Intent intent = accountBundle.getParcelable(Accounts.KEY_ACCOUNT);
            String username = intent.getStringExtra(KEY_ACCOUNT_NAME);
            String hashedPassword = intent.getStringExtra(KEY_PASSWORD);
            connection.loginWithHashedPassword(username, hashedPassword);
        }
        if (!connection.getXMPPTCPConnection().isAuthenticated()) {
            //Go to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    public void logOut() {
        connection = Connection.getInstance();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                connection.logOut();
                Accounts.removeAllAccounts(getApplicationContext());
                return null;
            }
        }.execute();
    }
}
