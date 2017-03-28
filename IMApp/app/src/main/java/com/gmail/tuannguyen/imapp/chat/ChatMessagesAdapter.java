package com.gmail.tuannguyen.imapp.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gmail.tuannguyen.imapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tuannguyen on 3/21/16.
 */
public class ChatMessagesAdapter extends BaseAdapter {
    private ArrayList<ChatMessage> chatMessages;
    private LayoutInflater inflater;

    ChatMessagesAdapter(Activity activity, ArrayList<ChatMessage> chatList) {
        chatMessages = chatList;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //View view = convertView;
        View view;
        ChatMessage chatMessage = chatMessages.get(position);

        //if (view == null){
        Date datetime = chatMessage.getDatetime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String datetimeStr = sdf.format(datetime);

        if (chatMessage.isMine()) {
            view = inflater.inflate(R.layout.chat_user2_item, null);
            //RelativeLayout bubbleParentLayout = (RelativeLayout) view.findViewById(R.id.bubble_parent);
            TextView timeTextView = (TextView) view.findViewById(R.id.time_text);
            timeTextView.setText(datetimeStr);

        } else {
            view = inflater.inflate(R.layout.chat_user1_item, null);
            //RelativeLayout bubbleParentLayout = (RelativeLayout) view.findViewById(R.id.bubble_parent);
            TextView timeTextView = (TextView) view.findViewById(R.id.time_text);
            TextView userTextView = (TextView) view.findViewById(R.id.incoming_user);
            timeTextView.setText(datetimeStr);
            userTextView.setText(chatMessage.getContact());
        }
        //bubbleParentLayout.setGravity(Gravity.LEFT);

        TextView textMsgView = (TextView) view.findViewById(R.id.message_text);
        textMsgView.setText(chatMessage.getBody());


        textMsgView.setTextColor(Color.BLACK);
        return view;
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }
}
