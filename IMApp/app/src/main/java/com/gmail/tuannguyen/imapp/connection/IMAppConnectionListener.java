package com.gmail.tuannguyen.imapp.connection;

import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;

/**
 * Created by tuannguyen on 4/3/16.
 */
public class IMAppConnectionListener implements ConnectionListener {

    @Override
    public void connected(XMPPConnection connection) {
        Log.d("ConnectionListener", "Connected");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        ChatManagerListenerImpl chatManagerListener = new ChatManagerListenerImpl();
        ChatManager.getInstanceFor(connection).addChatListener(chatManagerListener);
    }

    @Override
    public void connectionClosed() {
        Log.d("ConnectionListener", "Connection is closed");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.i("ConnectionListener", "Connection is closed on error");
        e.printStackTrace();
        //Log.i("ConnectionListener", "Reconnecting");
        //Connection.getInstance().connect("ConnectionListener:connectionClosedOnError");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d("ConnectionListener", "Reconnection successful");
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.d("ConnectionListener", "Reconnecting");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d("ConnectionListener", "Reconnection failed");
    }
}

