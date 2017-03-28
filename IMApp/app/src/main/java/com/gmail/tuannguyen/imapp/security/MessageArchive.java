package com.gmail.tuannguyen.imapp.security;

import android.content.Context;
import android.util.Log;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.connection.Connection;

import static com.gmail.tuannguyen.imapp.util.Common.*;

/**
 * Created by tuannguyen on 5/9/16.
 */
public class MessageArchive {
    public static final String TAG= "MessageArchive";

    public  static  void sendMessageArchiveIQ (Context context,boolean isArchived){
        String type = MessageArchiveType.NEVER;
        if (isArchived){
            type = MessageArchiveType.ALWAYS;
        }
        MessageArchiveIQ messageArchiveIQ = new MessageArchiveIQ(type);
        messageArchiveIQ .setTo(context.getString(R.string.server_address));
        Log.d(TAG, "SEND_MAM_IQ" + messageArchiveIQ.toXML().toString());
        if (null != (Connection.getInstance())) {
            Connection.getInstance().sendPacket(messageArchiveIQ);
        }
    }
}
