package com.gmail.tuannguyen.imapp.contact;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.connection.Connection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;

/**
 * Created by tuannguyen on 3/21/16.
 */
public class ContactAdapter extends BaseAdapter {
    private ArrayList<Contact> contacts;
    private LayoutInflater inflater;
    private ContactFragment contactFragment;

    ContactAdapter(Activity activity, ContactFragment contactFragment, ArrayList<Contact> contactList) {
        contacts = contactList;
        this.contactFragment = contactFragment;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Contact getContactItem(int position) {
        return contacts.get(position);
    }
    @Override
    public int getCount() {
        return contacts.size();
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
        View view;
        Contact contact = contacts.get(position);
        if (contact.getType() == Contact.Type.FRIEND_REQUEST_TO_ME) {
            view = inflateFriendRequestItem(contact);
        } else {
            view = inflater.inflate(R.layout.contact_item, null);
        }

        byte[] avatarBytes = contact.getAvatar(true);

        TextView textMsgView = (TextView) view.findViewById(R.id.contact_name);
        ImageView avatarView = (ImageView) view.findViewById(R.id.avatar);
        /*
        avatarView.getLayoutParams().height = 128;
        avatarView.getLayoutParams().width = 128;
        avatarView.requestLayout();
        */
        if (avatarBytes != null) {
            Bitmap avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
            avatarView.setImageBitmap(Bitmap.createScaledBitmap(avatar, 128, 128, false));
        }
        textMsgView.setText(contact.getNickname());
        if (contact.getType() == Contact.Type.FRIEND_REQUEST_FROM_ME) {
            view.setBackgroundColor(view.getResources().getColor(R.color.gray));
        }
        return view;
    }

    private View inflateFriendRequestItem(final Contact contact) {
        View view = inflater.inflate(R.layout.friend_request_item, null);
        Button acceptButton = (Button) view.findViewById(R.id.accept_button);
        Button denyButton = (Button) view.findViewById(R.id.deny_button);
        final String toContact = contact.getUsername();
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Connection.getInstance().sendPresencePacket(toContact, Presence.Type.subscribed);
                    contactFragment.refreshContactList();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Connection.getInstance().sendPresencePacket(toContact, Presence.Type.unsubscribed);
                    contactFragment.refreshContactList();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    public void add(Contact contact) {
        contacts.add(contact);
    }
}
