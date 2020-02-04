package com.virtualtld.server;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.util.function.Function;

public class HandleDnsQuery implements Function<Message, Message> {

    private final Message nsResp;
    private Name myName;

    public HandleDnsQuery(Message nsResp) {
        this.nsResp = nsResp;
    }

    @Override
    public Message apply(Message input) {
        try {
            return _apply(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Message _apply(Message input) throws Exception {
        myName = this.nsResp.getSectionArray(Section.ANSWER)[0].getName();
        Record question = input.getQuestion();
        if (question.getName().equals(myName) && question.getDClass() == DClass.IN && question.getType() == Type.NS) {
            Message output = (Message) nsResp.clone();
            output.getHeader().setID(input.getHeader().getID());
            return output;
        }
        Message output = new Message();
        output.getHeader().setID(input.getHeader().getID());
        return output;
    }
}
