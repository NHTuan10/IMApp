package com.gmail.tuannguyen.imapp.util;

/**
 * Created by tuannguyen on 4/22/16.
 */
public class Common {
    public static String BROADCAST_CHAT_MESSAGE =
            "com.gmail.tuannguyen.imapp.NEW_CHAT_MESSAGE";
    public static String BROADCAST_FRIEND_REQUEST = "com.gmail.tuannguyen.imapp.FRIEND_REQUEST";
    public static String FRIEND_REQUEST_FROM = "FRIEND_REQUEST_FROM";

    public static String CHAT_MESSAGE = "ChatMessage";
    public static String CHAT_MESSAGE_TYPE = "ChatMessageType";
    public static String SECRET_SESSION_PREFIX = "SECRET_SESSION_";
    public static final int KEYGUARD_REQ_CODE = 1;

    public static class MessageType {
        public static String UNSEEN_INCOMING_MESSAGE_TYPE = "UnSeenIncomingMessage";
        public static String INCOMING_MESSAGE_TYPE = "IncomingMessage";
        public static String OUTGOING_MESSAGE_TYPE = "OutgoingMessage";
    }

    public static final String KEY_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_NEW_ACCOUNT";

    public static final String ITEM_ACCOUNT_INFORMATION = "Account Information";
    public static final String ITEM_SECURITY_PRIVACY = "Security & Privacy";
    public static final String ITEM_HELP = "Help";
    public static final String ITEM_LOG_OUT = "Log Out";

    public static final String GCM_IQ_NAMESPACE = "https://gcm-http.googleapis.com/gcm";
    public static final String GCM_KEY_TOKEN = "token";

    public static final String MESSAGE_ARCHIVE_IQ_NAMESPACE = "urn:xmpp:mam:1";
    public static final String MESSAGE_ARCHIVE_IQ_ELE_NAME = "prefs";
    public static final String MESSAGE_ARCHIVE_IQ_ATTR_NAME = "default";

    public static class MessageArchiveType {
        public static String NEVER = "never";
        public static String ALWAYS = "always";
    }
    public static final String SECRET_SESSION_NAME = "secret_session";
    public static final String SECRET_SESSION_NAMESPACE = "secret_session";
}
