package com.gmail.tuannguyen.imapp.connection;

/**
 * Created by tuannguyen on 3/18/16.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.chat.ChatMessage;
import com.gmail.tuannguyen.imapp.db.MessageDBHelper;
import com.gmail.tuannguyen.imapp.security.Accounts;
import com.gmail.tuannguyen.imapp.util.Common;
import com.gmail.tuannguyen.imapp.util.CommonUtil;
import com.google.gson.Gson;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;

import static com.gmail.tuannguyen.imapp.util.Common.BROADCAST_FRIEND_REQUEST;
import static com.gmail.tuannguyen.imapp.util.Common.FRIEND_REQUEST_FROM;

public class Connection {
    private String serverAddress;
    private int serverPort;
    private XMPPTCPConnection connection;
    private Context context;
    private static Connection connectionInstance = null;
    private static ReceivedMessageListener recvMsgListener = null;
    private static String resource = null;
    private XMPPTCPConnectionConfiguration.Builder config;
    private static final String TAG = "Connection";
    private Handler mainLooperHandler;

    //private SSLContext sslContext = null;

    public String getUser() {
        return connection.getUser().split("@")[0];
    }

    /**
     * @param context
     * @param serverAddress
     * @param serverPort
     * @return
     */

    public static Connection getInstance(Context context, String serverAddress, int serverPort) {
        if (connectionInstance == null) {
            connectionInstance = new Connection(context, serverAddress, serverPort);
        }
        return connectionInstance;
    }

    public static Connection getInstance() {
        return connectionInstance;
    }

    /**
     * @return
     */
    public XMPPTCPConnection getXMPPTCPConnection() {
        return connection;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * @param context
     * @param serverAddress
     * @param serverPort
     */
    private Connection(Context context, String serverAddress, int serverPort) {
        this.context = context;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        mainLooperHandler = new Handler(Looper.getMainLooper());
        this.initialiseConnection();
    }

    /**
     *
     */
    private void initialiseConnection() {
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

        config = XMPPTCPConnectionConfiguration
                .builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
        config.setSendPresence(true);
        //config.setSocketFactory(new DummySSLSocketFactory());
        config.setServiceName(serverAddress);
        config.setHost(serverAddress);
        config.setPort(serverPort);
        if (resource == null) {
            resource = CommonUtil.randomAlphaNumeric();
        }
        config.setResource(resource);
        config.setDebuggerEnabled(true);

        XMPPTCPConnection.setUseStreamManagementResumptionDefault(false);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        configSSLConnection();

        connection = new XMPPTCPConnection(config.build());
        recvMsgListener = new ReceivedMessageListener(context);
        IMAppConnectionListener connectionListener = new IMAppConnectionListener();
        connection.addConnectionListener(connectionListener);

    }

    public void loadSSLKeystore() {
        try {
            SSLContext sslContext = createSSLContextFromKeyStore();
            config.setCustomSSLContext(sslContext);
            config.setSocketFactory(sslContext.getSocketFactory());

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public SSLContext createSSLContextFromKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, KeyManagementException {
        KeyStore keyStore;
        InputStream inputStream;
        keyStore = KeyStore.getInstance("BKS");
        inputStream = context.getResources().openRawResource(R.raw.imapp_server);
        keyStore.load(inputStream, "keypass".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    public void configSSLConnection() {

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            MemorizingTrustManager mtm = new MemorizingTrustManager(context);
            sslContext.init(null, new X509TrustManager[]{mtm}, new java.security.SecureRandom());
            config.setCustomSSLContext(sslContext);
            config.setHostnameVerifier(
                    mtm.wrapHostnameVerifier(new org.apache.http.conn.ssl.StrictHostnameVerifier()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

    }

    public static ReceivedMessageListener getRecvMsgListener() {
        return recvMsgListener;
    }

    /**
     * @param caller
     * @return
     */
    public boolean connect(final String caller) {

        if (connection.isConnected())
            return true;

        showMessage("(" + caller + ")" + " Connecting ...");

        Log.d(caller, "Connection.connect:Connecting...");
        try

        {

            connection.connect();
            DeliveryReceiptManager drm = DeliveryReceiptManager.getInstanceFor(connection);
            drm.setAutoReceiptMode(AutoReceiptMode.always);
            drm.addReceiptReceivedListener(new ReceiptReceivedListener() {
                @Override
                public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {

                }
            });
            return true;
        } catch (IOException e) {
            //showMessage("(" + caller + ")" + " IOException");
            Log.d(caller, "Connection.connect: IOException: " + e.getMessage());
            e.printStackTrace();

        } catch (XMPPException e) {
            //showMessage("(" + caller + ")" + " XMPPException");
            Log.d(caller, "Connection.connect: XMPPException: " + e.getMessage());
            e.printStackTrace();
        } catch (SmackException e) {
            //showMessage("(" + caller + ")" + " SmackException");
            Log.d(caller, "Connection.connect: SmackException: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    public void addPacketListener() {
        StanzaListener stanzaListener = new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Presence presence = (Presence) packet;
                if (presence.getType() == Presence.Type.subscribe) {
                    String requestFrom = presence.getFrom();
                    Log.d(TAG, "Friend Request from " + requestFrom);
                    notifyFriendRequest(requestFrom);
                }
            }
        };
        connection.addSyncStanzaListener(stanzaListener, new StanzaTypeFilter(Presence.class));
    }

    public boolean login(String userName, String password) {
        String encryptedPassword = CommonUtil.hash(password);
        return loginWithHashedPassword(userName, encryptedPassword);
    }
    /**
     *
     */
    public boolean loginWithHashedPassword(String userName, String encryptedPassword) {
        try {
            if (!connection.isConnected()) {
                connect("XMPP Login");
            }
            if (!connection.isAuthenticated() ||
                    !userName.equals(CommonUtil.extractUserName(connection.getUser()))) {

                connection.login(userName, encryptedPassword);

            }

            /*
            Presence presence = new Presence(Presence.Type.available);
            presence.setMode(Presence.Mode.available);
            try {
                connection.sendStanza(presence);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            */
            sendPresencePacket(serverAddress, Presence.Type.available);
            addPacketListener();
            Accounts.addAccount(context, userName, encryptedPassword);
            this.showMessage(" Login Successful ...");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            this.showMessage("Login Fail");

        }
        return false;
    }

    public void logOut() {
        try {
            connection.disconnect(new Presence(Presence.Type.unavailable));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        //connection.disconnect();
    }

    public boolean createAccount(String username, String password, Map<String, String> accountInfo) {
        if (connection != null) {
            connection.disconnect();
        }
        //connect("Create Account");
        try {
            initialiseConnection();
            connect("Create Account");

            AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.sensitiveOperationOverInsecureConnection(true);
            String encryptedPassword = CommonUtil.hash(password);
            accountManager.createAccount(username, encryptedPassword, accountInfo);
            //return true;
            return login(username, password);

        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param message
     */
    public void showMessage(final String message) {

        mainLooperHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void sendMessage(ChatMessage chatMessage) {
        Gson gson = new Gson();
        String contact = chatMessage.getContact();
        Chat chat = ChatManager.getInstanceFor(connection).createChat(
                contact + "@" + serverAddress,
                recvMsgListener
        );


        Message message = new Message();
        //Change chatMessage object into JSON, and set it as body of message
        //String body = gson.toJson(chatMessage);
        message.setBody(chatMessage.getBody());
        String msgID = String.format("%04d", new Random().nextInt(10000));
        message.setStanzaId(msgID);
        message.setType(Message.Type.chat);
        try {
            if (connection.isAuthenticated()) {
                if (CommonUtil.isSecretSession(context, contact)) {
                    //message.addBody("secret_session","true");
                    DefaultExtensionElement secretSessionExtEle = new DefaultExtensionElement(
                            Common.SECRET_SESSION_NAME, Common.SECRET_SESSION_NAMESPACE);
                    secretSessionExtEle.setValue(Common.SECRET_SESSION_NAME, "true");
                    message.addExtension(secretSessionExtEle);
                }
                chat.sendMessage(message);
                CommonUtil.notifyUiNewMessage(context, chatMessage, Common.MessageType.OUTGOING_MESSAGE_TYPE);
                //Store to local database if it's not secret session
                if (!CommonUtil.isSecretSession(context, contact)) {
                    MessageDBHelper.getInstance(context, CommonUtil.getAuthenticatedUserName(false))
                            .insertMessage(chatMessage);
                }
            } else {
                connection.login();
            }

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            Log.e("sendMessage(): ", "XMPP Message is not sent, not connected");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("sendMessage(): ", "Message is not sent, Exception " + e.getMessage());
        }
    }

    /**
     * @param requestId
     */
    public void notifyFriendRequest(final String requestId) {
        Intent intent = new Intent(BROADCAST_FRIEND_REQUEST);
        intent.putExtra(FRIEND_REQUEST_FROM, requestId);
        context.sendBroadcast(intent);
    }

    public void sendPresencePacket(String to, Presence.Type type) throws SmackException.NotConnectedException {
        Presence response = new Presence(type);
        response.setTo(to);
        connection.sendStanza(response);

    }

    public Message buildSecretSessionMessage(Message message) {

        return message;
    }

    public void sendPacket(Stanza stanza) {
        if (!connection.isConnected()) {
            connect("Send Custom Packet");
        }
        try {
            connection.sendStanza(stanza);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void receiveFiles() {
        final FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);
        fileTransferManager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {
                IncomingFileTransfer incomingFileTransfer = request.accept();
                String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String filename = "file_" + (int) Math.random() * 1000;
                try {
                    File file = new File(directoryPath, filename);
                    incomingFileTransfer.recieveFile(file);
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
