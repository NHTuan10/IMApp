package com.gmail.tuannguyen.imapp.chat;

import java.util.Date;

/**
 * Created by tuannguyen on 3/19/16.
 */
public class ChatMessage {


    private String body;
    private String sender;
    private String receiver;
    private String contact;
    private Date datetime;
    private boolean isMine;// true if I send the message.
    private boolean secretSession = false;

    public String getReceiver() {
        return receiver;
    }


    public String getSender() {
        return sender;
    }

    public boolean isSecretSession() {
        return secretSession;
    }

    public void setSecretSession(boolean secretSession) {
        this.secretSession = secretSession;
    }

    public Date getDatetime() {
        return datetime;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public String getBody() {
        return body;

    }

    public String getContact() {
        return contact;
    }

    /**
     * @param Sender
     * @param Receiver
     * @param messageString
     * @param isMINE
     * @param datetime
     */
    public ChatMessage(String Sender, String Receiver, String messageString,
                       boolean isMINE, Date datetime) {
        body = messageString;
        isMine = isMINE;
        sender = Sender;
        receiver = Receiver;
        this.datetime = datetime;
    }

    public ChatMessage(String contact, String messageString,
                       boolean isMINE, Date datetime) {
        body = messageString;
        isMine = isMINE;
        this.contact = contact;
        this.datetime = datetime;
    }
}
