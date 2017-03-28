package com.gmail.tuannguyen.imapp.security;

import com.gmail.tuannguyen.imapp.util.Common;

import org.jivesoftware.smack.packet.IQ;
import static com.gmail.tuannguyen.imapp.util.Common.*;
/**
 * Created by tuannguyen on 5/9/16.
 */
public class MessageArchiveIQ extends IQ {
    private String type;

    public MessageArchiveIQ(String type) {
        super(MESSAGE_ARCHIVE_IQ_ELE_NAME,MESSAGE_ARCHIVE_IQ_NAMESPACE);
        setType(Type.set);
        this.type = type;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute(MESSAGE_ARCHIVE_IQ_ATTR_NAME,type);
        xml.rightAngleBracket();
        //xml.element(Common.GCM_KEY_TOKEN, token);
        return xml;
    }
}