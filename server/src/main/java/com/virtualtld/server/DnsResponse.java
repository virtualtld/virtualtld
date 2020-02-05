package com.virtualtld.server;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.util.Map;

public class DnsResponse {

    private final Message input;
    private final Message nsResp;
    private final Map<String, byte[]> chunks;
    private final Name myName;

    public DnsResponse(Message input, Message nsResp, Map<String, byte[]> chunks) {
        this.input = input;
        this.nsResp = nsResp;
        this.chunks = chunks;
        if (nsResp != null) {
            myName = nsResp.getSectionArray(Section.ANSWER)[0].getName();
        } else {
            myName = null;
        }
    }

    public Message dnsResponse() {
        Record question = input.getQuestion();
        if (question.getName().equals(myName) && question.getDClass() == DClass.IN && question.getType() == Type.NS) {
            Message output = (Message) nsResp.clone();
            output.getHeader().setID(input.getHeader().getID());
            return output;
        }
        if (question.getDClass() == DClass.IN && question.getType() == Type.TXT) {
            return queryTxt(input);
        }
        Message err = new Message(input.getHeader().getID());
        err.getHeader().setRcode(Rcode.NXDOMAIN);
        return err;
    }

    private Message queryTxt(Message input) {
        Message output = new Message(input.getHeader().getID());
        String digest = input.getQuestion().getName().getLabelString(0);
        byte[] chunk = chunks.get(digest);
        if (chunk == null) {
            output.getHeader().setRcode(Rcode.NXDOMAIN);
            return output;
        }
        byte[] record1Bytes = new byte[258];
        record1Bytes[0] = (byte) 255;
        record1Bytes[256] = 1;
        record1Bytes[257] = 11;
//        byte[] record1Bytes = new byte[chunk.length + 1];
//        record1Bytes[0] = (byte) chunk.length;
//        System.arraycopy(chunk, 0, record1Bytes, 1, chunk.length);
        TXTRecord record1 = (TXTRecord) Record.newRecord(input.getQuestion().getName(), Type.TXT, DClass.IN, 172800, record1Bytes);
        output.addRecord(record1, Section.ANSWER);
        return output;
    }
}
