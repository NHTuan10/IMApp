package com.gmail.tuannguyen.imapp.chat;

import java.util.ArrayList;

/**
 * Created by tuannguyen on 4/19/16.
 */
public class TemporaryMessageStore {
    private ArrayList<ChatMessage> tempChatMessages;
    private static TemporaryMessageStore _instance;

    public static TemporaryMessageStore getInstance() {
        if (_instance == null) {
            _instance = new TemporaryMessageStore();
        }
        return _instance;
    }

    private TemporaryMessageStore() {
        tempChatMessages = new ArrayList<>();
    }

    public synchronized void addTempMessage(ChatMessage chatMessage) {
        tempChatMessages.add(chatMessage);
    }

    public synchronized ArrayList<ChatMessage> getTempMessages(String contact) {

        ArrayList<ChatMessage> result = new ArrayList<>();
        ArrayList<ChatMessage> copyList = new ArrayList<>(tempChatMessages);

        for (ChatMessage tempChatMessage : tempChatMessages) {
            if (contact.equals(tempChatMessage.getContact())) {
                result.add(tempChatMessage);
                copyList.remove(tempChatMessage);
            }
        }
        tempChatMessages = copyList;
        return result;

    }
}
