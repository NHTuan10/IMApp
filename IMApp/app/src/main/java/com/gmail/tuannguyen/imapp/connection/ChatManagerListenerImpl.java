package com.gmail.tuannguyen.imapp.connection;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;

/**
 * Created by tuannguyen on 3/19/16.
 */
public class ChatManagerListenerImpl implements ChatManagerListener {

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        //if (!createdLocally) {
            chat.addMessageListener(Connection.getRecvMsgListener());
        //}
    }
}