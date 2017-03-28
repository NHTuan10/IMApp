package com.gmail.tuannguyen.imapp.recent;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.chat.ChatMessage;
import com.gmail.tuannguyen.imapp.contact.Contact;
import com.gmail.tuannguyen.imapp.util.CommonUtil;

import java.util.ArrayList;

/**
 * Created by tuannguyen on 4/19/16.
 */
public class RecentAdapter extends BaseAdapter {
    private ArrayList<RecentItem> recentConversations;
    private boolean highlight = false;
    private LayoutInflater inflater;
    private Activity _activity;
    RecentAdapter(Activity activity, ArrayList<RecentItem> recentList) {
        recentConversations = recentList;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this._activity = activity;
    }

    public RecentItem getRecentContactItem(int position) {
        return recentConversations.get(position);
    }

    @Override
    public int getCount() {
        return recentConversations.size();
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
        //if (view == null)
        View view = inflater.inflate(R.layout.recent_item, null);
        RecentItem recentItem = recentConversations.get(position);
        byte[] avatarBytes = recentItem.getContact().getAvatar(true);

        TextView textMsgView = (TextView) view.findViewById(R.id.contact_name);
        TextView latestMsgView = (TextView) view.findViewById(R.id.latest_message);
        ImageView avatarView = (ImageView) view.findViewById(R.id.avatar);
        int grayColorId = _activity.getResources().getColor(R.color.gray);
        if (!recentItem.isSeen()) {
            int colorId = _activity.getResources().getColor(R.color.black);
            latestMsgView.setTextColor(colorId);
            view.setBackgroundColor(grayColorId);
        } else {
            //view.setBackgroundColor(colorId);
            latestMsgView.setTextColor(grayColorId);
        }
        if (avatarBytes != null) {
            avatarView.setImageBitmap(CommonUtil.toBitmap(avatarBytes, 128, 128));
        }
        textMsgView.setText(recentItem.getContact().getNickname());
        latestMsgView.setText(recentItem.getLatestMessage().getBody());
        return view;
    }

    public void updateNewMessageForContact(String contact, ChatMessage chatMessage, boolean seen) {
        for (RecentItem recentConversation : recentConversations) {
            String contactName = recentConversation.getContact().getUserNameWithoutServerAddr();
            if (contact.equals(contactName)) {
                RecentItem copy = new RecentItem(recentConversation.getContact(), chatMessage, seen);
                recentConversations.add(0, copy);
                recentConversations.remove(recentConversation);
                return;
            }
        }
        String fullContactName = contact + "@" + _activity.getString(R.string.server_address);
        RecentItem newRecentItem = new RecentItem(new Contact(fullContactName, null, false),
                chatMessage, seen);
        recentConversations.add(0, newRecentItem);

    }

    public void add(RecentItem recent) {
        recentConversations.add(recent);
    }
}
