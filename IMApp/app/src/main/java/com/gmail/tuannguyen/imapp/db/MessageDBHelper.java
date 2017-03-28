package com.gmail.tuannguyen.imapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.chat.ChatMessage;
import com.gmail.tuannguyen.imapp.contact.Contact;
import com.gmail.tuannguyen.imapp.recent.RecentItem;
import com.gmail.tuannguyen.imapp.security.KeyManager;
import com.gmail.tuannguyen.imapp.util.CommonUtil;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by tuannguyen on 4/2/16.
 * Message DB Helper
 */
public class MessageDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "IMApp.db";
    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_DATE_TIME = "date_time";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECEIVER = "receiver";
    public static final String COLUMN_CONTACT = "contact";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_IS_MINE = "isMine";

    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA = ",";
    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
            + " (" + COLUMN_ID + " INTEGER PRIMARY KEY" + COMMA
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + COMMA
            + COLUMN_DATE_TIME + " DATETIME" + COMMA
            + COLUMN_CONTACT + TEXT_TYPE + COMMA
            /*
            + COLUMN_SENDER + TEXT_TYPE + COMMA
            + COLUMN_RECEIVER + TEXT_TYPE + COMMA
            */
            + COLUMN_BODY + TEXT_TYPE + COMMA
            + COLUMN_IS_MINE + " INTEGER )";
    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static MessageDBHelper _instance = null;
    private static final String TAG = "MessageDbHelper";
    private static final String dbAliasSuffix = "_SECRET_KEY_IMAPP_DB";
    private String username;
    private String alias;
    private KeyManager keyManager;
    private SharedPreferences sharedPreferences;
    private Context context;
    private String key;

    public String getUsername() {
        return username;
    }

    public static MessageDBHelper getInstance(Context context, String username) {
        if (username == null)
            return null;
        if (_instance == null) {
            _instance = new MessageDBHelper(context, username);
        }
        /*
        else if(_instance.getUsername() == null){
            _instance = new MessageDBHelper(context,username + DB_NAME);
        }
        */
        else if (!username.equals(_instance.getUsername())) {
            _instance = new MessageDBHelper(context, username + DB_NAME);
        }
        return _instance;
    }

    private MessageDBHelper(Context context, String username) {
        super(context, username + DB_NAME, null, 1);
        Log.d(TAG, SQL_CREATE_TABLE);
        this.username = username;
        alias = this.username + dbAliasSuffix;
        this.context = context;
        //Store encrypted to store preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        keyManager = new KeyManager(context);
        SQLiteDatabase.loadLibs(context);

        if (context.getDatabasePath(username + DB_NAME).exists()) {
            key = getKey();
            return;
        }

        keyManager.createNewKey(alias);
        key = CommonUtil.randomAlphaNumeric();
        Log.d(TAG, "Key : " + key);
        String encryptedKey = keyManager.encryptString(alias, key);
        Log.d(TAG, "Encrypted Key:" + encryptedKey);
        if (encryptedKey == null) {
            Log.e("Create DB encrytedKey", "Error when create encryted key for user " + username);
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(alias, encryptedKey);
        editor.commit();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
    }

    public boolean insertMessage(ChatMessage chatMessage) {
        return this.insertMessage(
                chatMessage.getContact(),
                chatMessage.getBody(),
                chatMessage.getDatetime(),
                chatMessage.isMine()
        );
    }

    /**
     * Insert message to database
     *
     * @param contact
     * @param body
     * @param datetime
     * @param isMine
     * @return
     */
    public boolean insertMessage(String contact, String body, Date datetime, boolean isMine) {

        SQLiteDatabase db = this.getWritableDatabase(key);
        //SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        /*
        contentValues.put(COLUMN_SENDER, sender);
        contentValues.put(COLUMN_RECEIVER, receiver);
        */

        contentValues.put(COLUMN_BODY, body);
        //String strDatetime = CommonUtil.toDateString(datetime);
        long longDatetime = datetime.getTime();
        contentValues.put(COLUMN_DATE_TIME, longDatetime);
        /*
        if (isMine) {
            contentValues.put(COLUMN_IS_MINE, 1);
            contact = receiver;
        } else {
            contentValues.put(COLUMN_IS_MINE, 0);
            contact = sender;
        }
        */
        int intIsMine = isMine ? 1 : 0;
        contentValues.put(COLUMN_IS_MINE, intIsMine);
        contentValues.put(COLUMN_CONTACT, contact);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    /**
     * @param user
     * @param contact
     * @return
     */
    public ArrayList<ChatMessage> getAllMessagesForContact(String user, String contact) {

        SQLiteDatabase db = this.getReadableDatabase(key);
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();
        /*
        String whereClause = "(" + COLUMN_SENDER + " = ? AND " + COLUMN_RECEIVER + " = ? )";
        whereClause += " OR (" + COLUMN_SENDER + "  = ? AND " + COLUMN_RECEIVER + " = ? )";
        */
        String whereClause = "(" + COLUMN_CONTACT + " = ?)";
        String[] whereValues = new String[]{contact};
        String orderBy = COLUMN_ID + " ASC";

        Cursor c = db.query(TABLE_NAME, null, whereClause, whereValues, null, null, orderBy);
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            //Mapping from cusors to ChatMessage objects
            boolean isMine = c.getInt(c.getColumnIndexOrThrow(COLUMN_IS_MINE)) == 1;
            long dateMillisec = c.getLong(c.getColumnIndexOrThrow(COLUMN_DATE_TIME));
            //String contactCol = c.getString(c.getColumnIndexOrThrow(COLUMN_CONTACT));
            /*
            String sender, receiver;
            if (isMine) {
                receiver = contact;
                sender = user;
            } else {
                sender = contact;
                receiver = user;
            }
            */

            ChatMessage chatMessage = new ChatMessage(
                    contact,
                    c.getString(c.getColumnIndexOrThrow(COLUMN_BODY)), isMine, new Date(dateMillisec)
            );
            chatMessages.add(chatMessage);
            c.moveToNext();
        }
        c.close();
        db.close();
        return chatMessages;
    }

    /*
        public String getLatestMessageForContact(String user,String contact){
            SQLiteDatabase db = this.getReadableDatabase(getKey());
            String latestMessage;
            String whereClause = "(" + COLUMN_SENDER + " = ? AND " + COLUMN_RECEIVER + " = ? )";
            whereClause += " OR (" + COLUMN_SENDER + "  = ? AND " + COLUMN_RECEIVER + " = ? )";
            String[] whereValues = new String[]{user, contact, contact, user};
            String orderBy = COLUMN_ID + " DESC";

            Cursor c = db.query(TABLE_NAME, null, whereClause, whereValues, null, null, orderBy,"1");
            c.moveToFirst();
            latestMessage = c.getString(c.getColumnIndexOrThrow(COLUMN_BODY));
            c.close();
            db.close();
            return latestMessage;
        }

    public ArrayList<RecentItem> getRecentContacts() {
        SQLiteDatabase db = this.getReadableDatabase(getKey());
        ArrayList<RecentItem> recentItems = new ArrayList<>();
        boolean distinct = true;

        String whereClause = "(" + COLUMN_SENDER + " = ? )";
        whereClause += " OR (" + COLUMN_RECEIVER + " = ? )";
        String[] whereValues = new String[]{username, username};
        String orderBy = COLUMN_ID + " DESC";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * FROM " + TABLE_NAME);
        stringBuilder.append("SELECT * FROM " + TABLE_NAME);

        Cursor c = db.query(distinct, TABLE_NAME, null, whereClause, whereValues, COLUMN_SENDER, null, orderBy, null);
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            RecentItem recentItem = new RecentItem();

            //Mapping from cusors to ChatMessage objects
            String sender = c.getString(c.getColumnIndexOrThrow(COLUMN_SENDER));
            String receiver = c.getString(c.getColumnIndexOrThrow(COLUMN_RECEIVER));
            String body = c.getString(c.getColumnIndexOrThrow(COLUMN_BODY));
            boolean isMine = c.getInt(c.getColumnIndexOrThrow(COLUMN_IS_MINE)) == 1;
            long dateMillisec = c.getLong(c.getColumnIndexOrThrow(COLUMN_CREATED_AT));

            if (sender != username) {
                recentItem.setContact(new Contact(sender + "@" + context.getString(R.string.server_address)
                        , null, false));

            } else if (receiver != username) {
                recentItem.setContact(new Contact(receiver + "@" + context.getString(R.string.server_address)
                        , null, false));
            } else {
                Log.e(TAG, "Error when get recent contacts: query get messages not belonging to user");
                continue;
            }
            recentItem.setLatestMessage(new ChatMessage(sender, receiver, body, isMine, new Date(dateMillisec)));
            recentItems.add(recentItem);
            c.moveToNext();
        }
        c.close();
        db.close();
        return recentItems;
    }
    */

    public ArrayList<RecentItem> getRecentContacts() {
        SQLiteDatabase db = this.getReadableDatabase(key);
        ArrayList<RecentItem> recentItems = new ArrayList<>();
        boolean distinct = true;

        String query = "SELECT * FROM " + TABLE_NAME +
                " JOIN (\n" +
                String.format("SELECT %s, max(%s) as last_time\n", COLUMN_CONTACT, COLUMN_DATE_TIME) +
                " from " + TABLE_NAME +
                " group by " + COLUMN_CONTACT + " ) as recent \n" +
                String.format(" where %s.%s = recent.last_time", TABLE_NAME, COLUMN_DATE_TIME) +
                String.format(" order by %s.%s DESC", TABLE_NAME, COLUMN_DATE_TIME);

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            RecentItem recentItem = new RecentItem();

            //Mapping from cusors to ChatMessage objects
            String contact = c.getString(c.getColumnIndexOrThrow(COLUMN_CONTACT));
            recentItem.setContact(new Contact(contact + "@" + context.getString(R.string.server_address)
                    , null, false));

            String body = c.getString(c.getColumnIndexOrThrow(COLUMN_BODY));
            boolean isMine = c.getInt(c.getColumnIndexOrThrow(COLUMN_IS_MINE)) == 1;
            long dateMillisec = c.getLong(c.getColumnIndexOrThrow(COLUMN_DATE_TIME));
            /*
            String sender, receiver;
            if (isMine) {
                receiver = contact;
                sender = this.username;
            } else {
                sender = contact;
                receiver = this.username;
            }
            */
            recentItem.setLatestMessage(new ChatMessage(contact, body, isMine, new Date(dateMillisec)));
            recentItems.add(recentItem);
            c.moveToNext();
        }
        //TODO: should have exception when closing database
        c.close();
        db.close();
        return recentItems;
    }

    private String getKey() {
        String encryptedKey = sharedPreferences.getString(alias, null);
        String key;
        if (encryptedKey != null) {
            Log.d(TAG, "Get ecrypted Key:" + encryptedKey);
            key = keyManager.decryptString(alias, encryptedKey);

            Log.d(TAG, "Decrypted Key:" + key);
            if (key == null) {
                key = "THE_SECRET_KEY";
            }
        } else {
            key = "THE_SECRET_KEY";
        }
        return key;
    }

}
