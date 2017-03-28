package com.gmail.tuannguyen.imapp.gcm;

import com.gmail.tuannguyen.imapp.util.Common;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by tuannguyen on 5/7/16.
 */
public class RegistrationIQ extends IQ {
    private String token;

    public RegistrationIQ(String token) {
        super("register", Common.GCM_IQ_NAMESPACE);
        setType(Type.set);
        this.token = token;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.element(Common.GCM_KEY_TOKEN, token);
        return xml;
    }
}
