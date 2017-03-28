package com.gmail.tuannguyen.imapp.chat;

import com.gmail.tuannguyen.imapp.util.Common;

import org.jivesoftware.smack.packet.ExtensionElement;

/**
 * Created by tuannguyen on 5/8/16.
 */
public class SecretSessionExtensionELement implements ExtensionElement {
    private boolean value;

    /**
     * Returns the root element XML namespace.
     *
     * @return the namespace.
     */
    @Override
    public String getNamespace() {
        return Common.SECRET_SESSION_NAMESPACE;
    }

    /**
     * Returns the root element name.
     *
     * @return the element name.
     */
    @Override
    public String getElementName() {
        return Common.SECRET_SESSION_NAME;
    }

    public SecretSessionExtensionELement(boolean secretSession) {
        this.value = secretSession;
    }

    /**
     * Returns the XML representation of this Element.
     *
     * @return the stanza(/packet) extension as XML.
     */
    @Override
    public CharSequence toXML() {

        return "<" + getElementName() + " "
                + " xmlns=\"" + getNamespace() + "\">"
                + String.valueOf(value)
                + "</" + getElementName() + ">";
    }
}
