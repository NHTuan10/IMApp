package com.gmail.tuannguyen.imapp.recent;

import com.gmail.tuannguyen.imapp.chat.ChatMessage;
import com.gmail.tuannguyen.imapp.contact.Contact;

/**
 * Created by tuannguyen on 4/20/16.
 */
public class RecentItem {
    private Contact contact;
    private ChatMessage latestMessage;
    /**
     * highlight = true for new unseen message
     */
    private boolean seen = true;
    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public ChatMessage getLatestMessage() {
        return latestMessage;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setLatestMessage(ChatMessage chatMessage) {
        this.latestMessage = chatMessage;
    }

    public RecentItem(Contact contact, ChatMessage chatMessage, boolean seen) {
        this.contact = contact;
        this.latestMessage = chatMessage;
        this.seen = seen;
    }


    public RecentItem() {

    }
}
