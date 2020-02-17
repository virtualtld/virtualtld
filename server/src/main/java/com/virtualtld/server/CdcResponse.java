package com.virtualtld.server;

import com.protocol.cdc.Block;
import com.protocol.cdc.EncodedTxtRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.copyOfRange;

public class CdcResponse {

    private final static Logger LOGGER = LoggerFactory.getLogger(CdcResponse.class);
    private final Message input;
    private final Message nsResp;
    private final Map<String, Block> blocks;
    private final Name myName;

    public CdcResponse(Message input, Message nsResp, Map<String, Block> blocks) {
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
        Block block = blocks.get(digest);
        if (block == null) {
            LOGGER.warn("unknown block requested: " + digest);
            output.getHeader().setRcode(Rcode.NXDOMAIN);
            return output;
        }
        TXTRecord record = new EncodedTxtRecord(input.getQuestion().getName(), block).txtRecord();
        output.addRecord(record, Section.ANSWER);
        return output;
    }
}
