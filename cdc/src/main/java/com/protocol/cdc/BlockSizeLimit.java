package com.protocol.cdc;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class BlockSizeLimit {

    private final Name privateDomain;

    public BlockSizeLimit(Name privateDomain) {
        this.privateDomain = privateDomain;
    }

    public int limit() {
        try {
            return _limit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int _limit() throws Exception {
        int baseSize = baseSize();
        int limit = 508 - baseSize;
        if (limit > 255) {
            // 255 byte per string
            // need one byte to encode the remaining string
            limit -= 1;
        }
        return limit;
    }

    private int baseSize() throws TextParseException {
        Name name = new Name("3b42ab1c93bed68bd787356acf47c77655b4e55f", privateDomain);
        Message message = new Message();
        message.addRecord(Record.newRecord(name, Type.TXT, DClass.IN, 1024), Section.QUESTION);
        message.addRecord(Record.newRecord(name, Type.TXT, DClass.IN, 1024, new byte[]{0}), Section.ANSWER);
        return message.toWire().length;
    }
}
