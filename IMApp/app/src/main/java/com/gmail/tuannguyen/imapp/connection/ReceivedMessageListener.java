package com.gmail.tuannguyen.imapp.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gmail.tuannguyen.imapp.chat.ChatActivity;
import com.gmail.tuannguyen.imapp.chat.ChatMessage;
import com.gmail.tuannguyen.imapp.chat.TemporaryMessageStore;
import com.gmail.tuannguyen.imapp.db.MessageDBHelper;
import com.gmail.tuannguyen.imapp.util.Common;
import com.gmail.tuannguyen.imapp.util.CommonUtil;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import java.util.Calendar;
import java.util.Date;

import static com.gmail.tuannguyen.imapp.util.Common.MessageType;

/**
 * Created by tuannguyen on 3/19/16.
 */
public class ReceivedMessageListener implements ChatMessageListener {
    private Context context;


    ReceivedMessageListener(Context context) {
        this.context = context;
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        Log.d("ReceivedMessageListener", "New Message");
        if (message.getType() == Message.Type.chat && message.getBody() != null) {
            //String sender = CommonUtil.extractUserName(message.getFrom());
            //String receiver = CommonUtil.getAuthenticatedUserName(false);
            String contact = CommonUtil.extractUserName(message.getFrom());
            String body = message.getBody();

            Date datetime = null;
            try {
                DelayInformation delay = message.getExtension("x", "urn:xmpp:delay");
                if (delay != null) {
                    datetime = delay.getStamp();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (datetime == null) {
                datetime = Calendar.getInstance().getTime();
            }
            ChatMessage chatMessage = new ChatMessage(contact, body, false, datetime);

            DefaultExtensionElement secretSessionEle = message.getExtension(
                    Common.SECRET_SESSION_NAME,
                    Common.SECRET_SESSION_NAMESPACE
            );
            if (null != secretSessionEle) {
                String value = secretSessionEle.getValue(Common.SECRET_SESSION_NAME);
                if ("true".equals(value)) {
                    chatMessage.setSecretSession(true);
                }

            }

            processMessage(chatMessage);
        }
    }

    public void processMessage(final ChatMessage chatMessage) {
        chatMessage.setIsMine(false);
        //update Adapter and listView UI on the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String contact = chatMessage.getContact();
                boolean isSecretSession = CommonUtil.isSecretSession(context, chatMessage.getContact());
                //boolean isSecretMessage =
                if (!isSecretSession && !chatMessage.isSecretSession()) {
                    MessageDBHelper.getInstance(context, CommonUtil.getAuthenticatedUserName(false))
                            .insertMessage(chatMessage);
                }

                if (ChatActivity.isActive() &&
                        contact.equals(ChatActivity.getContact())) {
                    ChatActivity.getChatMessagesAdapter().add(chatMessage);
                    ChatActivity.getChatMessagesAdapter().notifyDataSetChanged();
                    CommonUtil.notifyUiNewMessage(context, chatMessage, MessageType.INCOMING_MESSAGE_TYPE);

                } else {
                    TemporaryMessageStore tmpMessageStore = TemporaryMessageStore.getInstance();
                    tmpMessageStore.addTempMessage(chatMessage);
                    CommonUtil.notifyUiNewMessage(context, chatMessage, MessageType.UNSEEN_INCOMING_MESSAGE_TYPE);
                    CommonUtil.showIncomingMessageNotification(context, contact, chatMessage.getBody());
                }

            }
        });

    }

}
