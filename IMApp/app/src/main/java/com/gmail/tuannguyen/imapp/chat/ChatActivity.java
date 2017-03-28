package com.gmail.tuannguyen.imapp.chat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.connection.Connection;
import com.gmail.tuannguyen.imapp.db.MessageDBHelper;
import com.gmail.tuannguyen.imapp.util.ActivityTracker;
import com.gmail.tuannguyen.imapp.util.Common;
import com.gmail.tuannguyen.imapp.util.CommonUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    private static String contact = null;
    private static ChatMessagesAdapter chatMessagesAdapter = null;
    private static boolean active = false;
    Connection connection = null;
    private SharedPreferences sharedPreferences;
    private String user = null;
    private ListView listView;
    private static final String TAG = "ChatActivity";

    public static boolean isActive() {
        return active;
    }

    public static String getContact() {
        return contact;
    }

    public static ChatMessagesAdapter getChatMessagesAdapter() {
        return chatMessagesAdapter;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        contact = intent.getStringExtra("CONTACT");
        setTitle(contact);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        listView = (ListView) findViewById(R.id.chat_list_view);
        connection = Connection.getInstance();
        user = connection.getUser();


        //Get all message from database
        MessageDBHelper msgDb = MessageDBHelper.getInstance(getApplicationContext(),
                CommonUtil.getAuthenticatedUserName(false));
        ArrayList<ChatMessage> chatList = msgDb.getAllMessagesForContact(user, contact);
        //Set adapter for chat list view
        chatMessagesAdapter = new ChatMessagesAdapter(this, chatList);
        listView.setAdapter(chatMessagesAdapter);

        if (CommonUtil.isSecretSession(getApplicationContext(), contact)) {
            changeUIForSecretSession();
            getNewMessageInSecretSession();
            chatMessagesAdapter.notifyDataSetChanged();
        }
        //Set auto-scroll when a new message come
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);

        Button sendMsgButton = (Button) findViewById(R.id.send_message_button);
        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Reload the listView when config change (change orientation, hidden keyboard , )
        System.out.println("Config changed");
        chatMessagesAdapter.notifyDataSetChanged();
    }

    public void getNewMessageInSecretSession() {
        ArrayList<ChatMessage> chatMessages = TemporaryMessageStore.getInstance()
                .getTempMessages(contact);
        for (ChatMessage chatMessage : chatMessages) {
            chatMessagesAdapter.add(chatMessage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        MenuItem menuItem = menu.findItem(R.id.action_secret_session);
        if (CommonUtil.isSecretSession(getApplicationContext(), contact)) {
            menuItem.setTitle(R.string.stop_secret_session);
        } else {
            menuItem.setTitle(R.string.stop_secret_session);
        }

        return true;
    }

    public void changeUIForSecretSession() {
        listView.setBackgroundColor(getResources().getColor(R.color.gray));
    }

    public void changeUIForNonSecretSession() {
        listView.setBackgroundColor(getResources().getColor(R.color.white));
    }

    public void startSecretSession() {
        //Start secret session
        String key = CommonUtil.getSecretSessionPreferenceKey(user, contact);
        sharedPreferences.edit().putBoolean(key, true).apply();
        changeUIForSecretSession();
    }

    public void stopSecretSession() {
        //Stop secret session
        String key = CommonUtil.getSecretSessionPreferenceKey(user, contact);
        sharedPreferences.edit().putBoolean(key, false).apply();
        changeUIForNonSecretSession();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_secret_session:

                if (!CommonUtil.isSecretSession(getApplicationContext(), contact)) {
                    startSecretSession();
                } else {
                    stopSecretSession();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     *
     */
    public void sendTextMessage() {

        EditText msgEditText = (EditText) findViewById(R.id.message_edit_text);
        String msgText = msgEditText.getText().toString();

        if (!msgText.isEmpty()) {
            ///Add message to list
            String user = connection.getUser();
            ChatMessage chatMsg = new ChatMessage(contact, msgText, true,
                    Calendar.getInstance().getTime());
            if (chatMessagesAdapter != null) {
                chatMessagesAdapter.add(chatMsg);
                chatMessagesAdapter.notifyDataSetChanged();
            }
            //TODO: Add send message
            connection.sendMessage(chatMsg);
            msgEditText.setText("");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        ActivityTracker.activityStopped();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        ActivityTracker.activityStarted();

        if (!ActivityTracker.lockScreenRequired || !CommonUtil.isLockScreenEnabled(getApplicationContext()))
            return;
        KeyguardManager km = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        Intent intent = km.createConfirmDeviceCredentialIntent("IMApp Authentication", "Unlock to continue");
        startActivityForResult(intent, Common.KEYGUARD_REQ_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.KEYGUARD_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Successful done the lock screen authentication");
                ActivityTracker.lockScreenRequired = false;
            }
        }

    }
}
