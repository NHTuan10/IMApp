package com.gmail.tuannguyen.imapp.contact;

import android.util.Log;

import com.gmail.tuannguyen.imapp.connection.Connection;
import com.gmail.tuannguyen.imapp.util.CommonUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tuannguyen on 4/1/16.
 */
public class Contacts {

    public static ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (Connection.getInstance() == null) {
            return contacts;
        }
        XMPPTCPConnection xmpptcpConnection = Connection.getInstance().getXMPPTCPConnection();
        if (xmpptcpConnection == null) {
            Log.d("Connection Error", "getContacts: Error connect to server");
            return contacts;
        }
        Roster roster = Roster.getInstanceFor(xmpptcpConnection);
        Collection<RosterEntry> rosterEntries = roster.getEntries();

        for (RosterEntry entry : rosterEntries) {
            Presence presence = roster.getPresence(entry.getUser());
            boolean onlineStatus = presence.isAvailable();
            Log.d("Roster presence", presence.toString());
            String nickname = entry.getName();
            if (nickname == null || nickname.isEmpty()) {
                nickname = CommonUtil.extractUserName(entry.getUser());
            }
            Contact contact = new Contact(entry.getUser(), nickname, onlineStatus);
            contacts.add(contact);
        }
        return contacts;
    }

/*
    public static boolean checkInContacts(final String username) {
        XMPPTCPConnection xmpptcpConnection = Connection.getInstance().getXMPPTCPConnection();
        Roster roster = Roster.getInstanceFor(xmpptcpConnection);
        Collection<RosterEntry> rosterEntries = roster.getEntries();

        for (RosterEntry entry : rosterEntries) {
            if (username.equals(CommonUtil.extractUserName(entry.getUser()))) {
                return true;
            }
        }
        return false;
    }
*/

    /**
     * Contact has form user@server
     *
     * @param contact
     */
    public static void addContact(String contact) {
        XMPPTCPConnection xmpptcpConnection = Connection.getInstance().getXMPPTCPConnection();
        if (xmpptcpConnection == null) {
            Log.d("Connection Error", "getContacts: Error connect to server");
            return;
        }
        String name = CommonUtil.extractUserName(contact);
        try {
            Connection.getInstance().sendPresencePacket(contact, Presence.Type.subscribed);
            Roster roster = Roster.getInstanceFor(xmpptcpConnection);
            roster.createEntry(contact, name, null);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param username
     * @return
     */
    public static VCard loadVCard(String username) {
        // To load VCard:
        XMPPTCPConnection connection = Connection.getInstance().getXMPPTCPConnection();
        VCard vCard = new VCard();
        VCardManager vCardManager = VCardManager.getInstanceFor(connection);
        //
        try {
            if (username == null) {
                vCard = vCardManager.loadVCard(); // load own VCard
            } else {
                vCard = vCardManager.loadVCard(username); // load someone's VCard
            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return vCard;
    }

    /**
     * Need modifying to add real VCard information
     */
    public static void saveVCard(String firstname, String lastname) {
        XMPPTCPConnection connection = Connection.getInstance().getXMPPTCPConnection();
        VCard vCard = new VCard();
        vCard.setFirstName(firstname);
        vCard.setLastName(lastname);
        vCard.setNickName(firstname);
        /*
        vCard.setEmailHome("foo@fee.bar");
        vCard.setJabberId("jabber@id.org");
        vCard.setOrganization("Jetbrains, s.r.o");
        vCard.setField("TITLE", "Mr");
        vCard.setAddressFieldHome("STREET", "Some street");
        vCard.setAddressFieldWork("CTRY", "US");
        vCard.setPhoneWork("FAX", "3443233");
        */
        VCardManager vCardManager = VCardManager.getInstanceFor(connection);
        try {
            vCardManager.saveVCard(vCard);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }
}
