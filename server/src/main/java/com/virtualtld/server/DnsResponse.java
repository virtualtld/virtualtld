package com.virtualtld.server;

import com.protocol.cdc.Block;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
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
    private final Map<String, Block> blocks;
    private final Name myName;

    public DnsResponse(Message input, Message nsResp, Map<String, Block> blocks) {
        this.input = input;
        this.nsResp = nsResp;
        this.blocks = blocks;
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
        output.getHeader().setFlag(Flags.QR);
        output.getHeader().setFlag(Flags.AA);
        output.addRecord(input.getQuestion(), Section.QUESTION);
        String digest = input.getQuestion().getName().getLabelString(0);
        byte[] block = blocks.get(digest).data();
        if (block == null) {
            output.getHeader().setRcode(Rcode.NXDOMAIN);
            return output;
        }
        byte[] record1Bytes = new byte[block.length + 1];
        record1Bytes[0] = (byte) block.length;
        System.arraycopy(block, 0, record1Bytes, 1, block.length);
        TXTRecord record1 = (TXTRecord) Record.newRecord(
                input.getQuestion().getName(), Type.TXT, DClass.IN, 172800, record1Bytes);
        output.addRecord(record1, Section.ANSWER);
        return output;
    }
}
