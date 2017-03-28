package com.gmail.tuannguyen.imapp.contact;

import com.gmail.tuannguyen.imapp.util.CommonUtil;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.ByteArrayInputStream;

/**
 * Created by tuannguyen on 4/1/16.
 */

public class Contact {

    /**
     * username has form user@email/resource
     */
    private String username;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private String nickname;
    private String status;
    private boolean available;
    private byte[] avatar = null;

    public static class Type {
        public static String FRIEND = "FRIEND";
        public static String FRIEND_REQUEST_TO_ME = "FRIEND_REQUEST_TO_ME";
        public static String FRIEND_REQUEST_FROM_ME = "FRIEND_REQUEST_FROM_ME";
    }

    private String type = Type.FRIEND;

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUsername() {
        return username;
    }

    public String getUserNameWithoutServerAddr() {
        return CommonUtil.extractUserName(username);
    }

    public Contact(String uname, String nickname, boolean available) {
        username = uname;
        if (nickname == null) {
            this.nickname = CommonUtil.extractUserName(username);
        } else {
            this.nickname = nickname;
        }
        this.available = available;
    }

    public Contact(String uname, String type) {
        username = uname;
        this.nickname = CommonUtil.extractUserName(username);
        this.type = type;
    }

    public byte[] getAvatar(boolean isUsingCache) {
        if (isUsingCache && avatar != null) {
            return avatar;
        }
        // To load VCard:
        VCard vCard = Contacts.loadVCard(CommonUtil.extractUserNameExcludeResource(getUsername()));
        byte[] bytes = vCard.getAvatar();
        if (bytes != null) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            byte[] avatarBytes = CommonUtil.readAllBytesFromInputStream(inputStream);
            avatar = avatarBytes;
            return avatarBytes;
        }
        return null;
    }



}

